package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.spider.model.queueObject.PaperObject;
import com.buaa.academic.spider.model.queueObject.ResearcherSet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
public class PaperParser {

    private PaperObject paperCraw;

    private StatusCtrl statusCtrl;

    private RemoteWebDriver driver;

    public void wanFangSpider() {
        String threadName = Thread.currentThread().getName();
        try {
            Paper paper = statusCtrl.existenceService.findPaperById(paperCraw.getPaperId());
            // 已经爬完了
            boolean withOutAuthorsId = true;
            if (paper.isCrawled()) {
                for (Paper.Author author : paper.getAuthors()) {
                    if (author.getId() != null) {
                        withOutAuthorsId = false;
                        break;
                    }
                }
            }
            if (paper.isCrawled() && !withOutAuthorsId) {
                return;
            }

            driver.get(this.paperCraw.getUrl());
            Thread.sleep(2000);
            // 处理url非法
            String curUrl = driver.getCurrentUrl();
            if (curUrl.startsWith("https://s.wanfangdata.com.cn/")) {
                return;
            }

            // 获取当前页面url，添加外链
            if (!paper.isCrawled()) {

                List<Paper.Source> sources = paper.getSources();
                if (sources == null)
                    sources = new ArrayList<>();
                List<String> sourceText = new ArrayList<>();
                sources.forEach(source -> sourceText.add(source.getWebsite()));
                if (!sourceText.contains("万方")) {
                    Paper.Source source = new Paper.Source("万方", this.paperCraw.getUrl());
                    sources.add(source);
                    paper.setSources(sources);
                    statusCtrl.template.save(paper);
                }

                paper.setCrawled(true);
                driver.get(this.paperCraw.getUrl());
                Thread.sleep(2000);


                // 获取标题
                List<WebElement> titleElement = driver.findElementsByXPath("//span[@class=\"detailTitleCN\"]");
                if (titleElement.size() != 0) {
                    String title = titleElement.get(0).getText();
                    paper.setTitle(title);
                }

                statusCtrl.changeRunningStatusTo(threadName, "Get main info of the paper with title: " + paper.getTitle());

                // 获取摘要
                List<WebElement> summaryElement = driver.findElementsByXPath("//div[@class=\"summary\"]");
                if (summaryElement.size() != 0) {
                    WebElement summary = summaryElement.get(0);
                    List<WebElement> noMoreElement = summary.findElements(By.xpath(".//span[@class=\"getMore\" and @style=\"display: none;\"]"));
                    List<WebElement> moreElement = summary.findElements(By.xpath(".//span[@class=\"getMore\"]"));
                    String allSummary = null;
                    if (noMoreElement.size() != 0) {
                        allSummary = summary.getText();
                        allSummary = allSummary.replace("摘要：", "");
                        allSummary = allSummary.replace("查看全部>>", "");
                        allSummary = allSummary.replace("\n", "");
                    } else if (moreElement.size() != 0) {
                        WebElement more = moreElement.get(0);
                        Actions actions = new Actions(driver);
                        actions.click(more).perform();
                        allSummary = summary.getText();
                        allSummary = allSummary.replace("摘要：", "");
                        allSummary = allSummary.replace("收起∧", "");
                        allSummary = allSummary.replace("\n", "");
                    }
                    paper.setPaperAbstract(allSummary);
                }
                // 获取DOI
                List<WebElement> DOIElement = driver.findElementsByXPath("//div[@class=\"doi list\"]//div[@class=\"itemUrl\"]//a");
                if (DOIElement.size() != 0) {
                    String doi = DOIElement.get(0).getText();
                    paper.setDoi(doi);
                }
                // 获取关键词
                List<WebElement> keywordElement = driver.findElementsByXPath("//div[@class=\"keyword list\"]//div[@class=\"itemUrl\"]//a");
                if (keywordElement.size() != 0) {
                    List<String> keywords = new ArrayList<>();
                    for (WebElement keyword : keywordElement) {
                        String word = keyword.getText();
                        keywords.add(word);
                    }
                    paper.setKeywords(keywords);
                }

                // 获取参与机构
                List<WebElement> instElement = new ArrayList<>();
                if (paper.getType().equals("期刊论文")) {
                    instElement = driver.findElementsByXPath("//div[@class=\"organization list\"]//div[@class=\"itemUrl\"]//a");
                } else if (paper.getType().equals("学位论文")) {
                    instElement = driver.findElementsByXPath("//div[@class=\"thesisOrganization list\"]//div[@class=\"itemUrl\"]//a");
                }
                if (instElement.size() != 0) {
                    ArrayList<Paper.Institution> institutions = new ArrayList<>();
                    for (WebElement institution : instElement) {
                        String instNameText = institution.getText();
                        String[] instNames = instNameText.split("[；;]");
                        for (String instName : instNames) {
                            Paper.Institution inst = new Paper.Institution();
                            instName = StringUtil.rmPlaceNameAndCode(instName);
                            statusCtrl.changeRunningStatusTo(threadName, "Get info of the institution with name: " + instName);
                            // find inst by name
                            Institution foundInst = statusCtrl.existenceService.findInstByName(instName);
                            if (foundInst != null) {
                                inst.setId(foundInst.getId());
                                inst.setName(instName);
                            } else {
                                Institution newInst = new Institution();
                                newInst.setName(instName);
                                statusCtrl.institutionRepository.save(newInst);
                                inst.setId(newInst.getId());
                                inst.setName(instName);
                            }

                            boolean insert = true;
                            for (Paper.Institution institutionInList : institutions) {
                                if (institutionInList.getName().equals(instName)) {
                                    insert = false;
                                    break;
                                }
                            }
                            if (insert)
                                institutions.add(inst);
                        }
                    }
                    paper.setInstitutions(institutions);
                }
                // 期刊论文获取期刊内容，学位论文只获取学位授予年份（出版年份）
                if (paper.getType().equals("期刊论文")) {
                    boolean crawlNewJournal = false;
                    Paper.Journal journal = new Paper.Journal();
                    List<WebElement> journalElement = driver.findElementsByXPath("//div[@class=\"serialTitle list\"]//div[@class=\"itemUrl\"]//a");
                    if (journalElement.size() != 0) {
                        String journalName = journalElement.get(0).getText();
                        journal.setTitle(journalName);
                        // find journal by name
                        Journal foundJournal = statusCtrl.existenceService.findJournalByName(journalName);
                        if (foundJournal != null) {
                            journal.setId(foundJournal.getId());
                        } else {
                            foundJournal = new Journal();
                            foundJournal.setTitle(journalName);
                            statusCtrl.template.save(foundJournal);
                            journal.setId(foundJournal.getId());
                            crawlNewJournal = true;
                        }
                    }
                    // 获取年份、期号、卷号
                    List<WebElement> yearAndVolumeElement = driver.findElementsByXPath("//div[@class=\"getYear list\"]//div[@class=\"itemUrl\"]//a");
                    if (yearAndVolumeElement.size() != 0) {
                        WebElement yearAndVolume = yearAndVolumeElement.get(0);
                        String all = yearAndVolume.getText();
                        String volume = yearAndVolume.findElement(By.xpath(".//span")).getText();
                        String year = all.replace(volume, "");
                        year = year.replace("\n", "");
                        volume = volume.replace(",", "");
                        String issue;
                        if (volume.contains("(") && volume.contains(")")) {
                            issue = volume.substring(volume.indexOf("(") + 1, volume.indexOf(")"));
                        } else {
                            issue = null;
                        }

                        volume = volume.replaceAll("\\([0-9]*\\)", "");
                        paper.setYear(Integer.valueOf(year));
                        journal.setVolume(volume);
                        journal.setIssue(issue);
                    }
                    // 获取起始页
                    List<WebElement> pageNumElement = driver.findElementsByXPath("//div[@class=\"pageNum list\"]//div[@class=\"itemUrl\"]");
                    if (pageNumElement.size() != 0) {
                        String all = pageNumElement.get(0).getText();
                        String pattern = "[0-9]*-[0-9]*";
                        Pattern r = Pattern.compile(pattern);
                        Matcher m = r.matcher(all);
                        if (m.matches()) {
                            String start = all.substring(0, all.indexOf("-"));
                            String end = all.substring(all.indexOf("-") + 1);
                            journal.setStartPage(Integer.valueOf(start));
                            journal.setEndPage(Integer.valueOf(end));
                        }
                    }
                    paper.setJournal(journal);
                    if (crawlNewJournal) {
                        String journalUrl = journalElement.get(0).getAttribute("href");
                        if (journalUrl.startsWith("https://s.wanfangdata.com.cn/")) {
                            journalUrl = null;
                        }
                        StatusCtrl.journalUrls.add(journalUrl);
                    }
                } else if (paper.getType().equals("学位论文")) {
                    // 获取学位授予年份
                    List<WebElement> yearElement = driver.findElementsByXPath("//div[@class=\"thesisYear list\"]//div[@class=\"itemUrl\"]//span");
                    if (yearElement.size() != 0) {
                        String year = yearElement.get(0).getText();
                        paper.setYear(Integer.valueOf(year));
                    }
                }
                // 获取发表日期
                List<WebElement> publishElement = driver.findElementsByXPath("//div[@class=\"publish list\"]//div[@class=\"itemUrl\"]");
                if (publishElement.size() != 0) {
                    String date = publishElement.get(0).getText();
                    date = date.replace("（万方平台首次上网日期，不代表论文的发表时间）", "");
                    date = date.replaceAll("\\s+", "");
                    paper.setDate(date);
                } else {
                    if (paper.getYear() != null) {
                        String date = paper.getYear() + "-01-01";
                        paper.setDate(date);
                    }
                }

                // 获取参考文献
                List<String> referenceID = new ArrayList<>();
                List<WebElement> referenceElement;
                List<WebElement> ableElement;
                //toCrawPaperList = new ArrayList<>();
                int flag;
                do {
                    flag = 0;
                    referenceElement = driver.findElementsByXPath("//div[@id=\"reference\"]//div[@class=\"contentInfo\"]//td[@class=\"title\"]//a[contains(@href,\"/\") and @class=\"title\"]");
                    if (referenceElement.size() != 0) {
                        for (WebElement reference : referenceElement) {
                            String referTitle = reference.getText();
                            List<WebElement> tmp = reference.findElements(By.xpath("..//span[@style=\"vertical-align: middle;\"]"));
                            String type = tmp.get(1).getText();
                            String referUrl = reference.getAttribute("href");
                            statusCtrl.changeRunningStatusTo(threadName, "Get info of a reference paper with title: " + referTitle);
                            List<WebElement> referAuthorElement = reference.findElements(By.xpath("..//span[@class=\"author\"]//span//a"));
                            List<Paper.Author> referAuthorList = new ArrayList<>();
                            if (referAuthorElement.size() != 0) {
                                for (WebElement referAuthor : referAuthorElement) {
                                    Paper.Author referPaperAuthor = new Paper.Author();
                                    String referName = referAuthor.getText();
                                    referPaperAuthor.setName(referName);
                                    referAuthorList.add(referPaperAuthor);
                                }
                            }
                            // 期刊论文：J 学位论文：D
                            if (type.startsWith("[J]")) {
                                // find paper by referTitle and referAuthorName
                                Paper foundReferPaper = statusCtrl.existenceService.findPaperByTileAndAuthors(referTitle, referAuthorList);
                                if (foundReferPaper == null) {
                                    foundReferPaper = new Paper();
                                    foundReferPaper.setCrawled(false);
                                    foundReferPaper.setTitle(referTitle);
                                    foundReferPaper.setAuthors(referAuthorList);
                                    foundReferPaper.setCitationNum(1);
                                    foundReferPaper.setType("期刊论文");
                                    // 插入数据库
                                } else {
                                    foundReferPaper.setCitationNum(foundReferPaper.getCitationNum() + 1);
                                }
                                statusCtrl.paperRepository.save(foundReferPaper);
                                // 把url塞进队列
                                PaperObject paperObject = new PaperObject(referUrl, foundReferPaper.getId());
                                StatusCtrl.paperObjectQueue.add(paperObject);
                                referenceID.add(foundReferPaper.getId());
                            } else if (type.startsWith("[D]")) {
                                Paper foundReferPaper = statusCtrl.existenceService.findPaperByTileAndAuthors(referTitle, referAuthorList);
                                if (foundReferPaper == null) {
                                    foundReferPaper = new Paper();
                                    foundReferPaper.setCrawled(false);
                                    foundReferPaper.setTitle(referTitle);
                                    foundReferPaper.setAuthors(referAuthorList);
                                    foundReferPaper.setCitationNum(1);
                                    foundReferPaper.setType("学位论文");
                                } else {
                                    foundReferPaper.setCitationNum(foundReferPaper.getCitationNum() + 1);
                                }
                                statusCtrl.paperRepository.save(foundReferPaper);
                                PaperObject paperObject = new PaperObject(referUrl, foundReferPaper.getId());
                                StatusCtrl.paperObjectQueue.add(paperObject);
                                referenceID.add(foundReferPaper.getId());
                            }
                        }
                    }
                    ableElement = driver.findElementsByXPath("//div[@id=\"reference\"]//div[@class=\"pagination_page\"]");
                    if (ableElement.size() != 0) {
                        for (WebElement able : ableElement) {
                            String ableName = able.getText();
                            if (ableName.equals("下一页")) {
                                flag = 1;
                                Actions actions = new Actions(driver);
                                actions.click(able).perform();
                                break;
                            }
                        }
                    }
                    referenceElement.clear();
                    ableElement.clear();
                    Thread.sleep(3000);
                } while (flag == 1);
                paper.setReferences(referenceID);
                Paper paperSource = statusCtrl.template.get(paper.getId(), Paper.class);
                assert paperSource != null;
                if (paperSource.getSources().size() != paper.getSources().size()) {
                    paper.setSources(paperSource.getSources());
                }
                statusCtrl.paperRepository.save(paper);
            }

            // 获取作者
            List<WebElement> authorElement = driver.findElementsByXPath("//div[@class=\"author list\"]//div[@class=\"itemUrl\"]//a");
            if (authorElement.size() != 0) {
                ArrayList<ResearcherSet.ResearcherObject> researcherObjects = new ArrayList<>();
                for (WebElement author : authorElement) {
                    String authorName = author.getText();
                    String authorUrl = author.getAttribute("href");
                    if (!authorUrl.startsWith("https://trend.wanfangdata.com.cn/scholarsBootPage")) {
                        authorUrl = null;
                    }
                    ResearcherSet.ResearcherObject researcherObject = new ResearcherSet.ResearcherObject(authorUrl, authorName);
                    researcherObjects.add(researcherObject);
                }
                StatusCtrl.researcherQueue.add(new ResearcherSet(paper.getId(), researcherObjects));
            }

        } catch (Exception e) {
            StatusCtrl.errorHandler.report(e);
        }
    }

    //获取文章的topic、subject
    public void zhiWangSpider() {
        String threadName = Thread.currentThread().getName();
        try {
            driver.get(this.paperCraw.getUrl());
            Thread.sleep(2000);
            Paper paper = statusCtrl.existenceService.findPaperById(paperCraw.getPaperId());
            String title = paper.getTitle();
            statusCtrl.changeRunningStatusTo(threadName, "Get subjects of paper: " + title);
            List<Paper.Author> paperAuthors = paper.getAuthors();
            List<String> authors = new ArrayList<>();
            for (Paper.Author paperAuthor : paperAuthors) {
                authors.add(paperAuthor.getName());
            }
            WebElement curSearchType = driver.findElementByXPath("//div[@class=\"search-box\"]//div[@class=\"sort-default\"]");
            Actions actions = new Actions(driver);
            actions.click(curSearchType).perform();
            List<WebElement> searchTypes = driver.findElementsByXPath("//div[@class=\"sort-list\"]//li");
            if (searchTypes.size() != 0) {
                for (WebElement searchType : searchTypes) {
                    String type = searchType.getAttribute("data-val");
                    if (type.equals("TI")) {
                        actions.clickAndHold(searchType).perform();
                        break;
                    }
                }
            }
            // 搜索
            List<WebElement> searchTextElement = driver.findElementsByXPath("//input[@id=\"txt_search\"]");
            if (searchTextElement.size() != 0) {
                WebElement searchText = searchTextElement.get(0);
                searchText.sendKeys(paper.getTitle());
            }

            WebElement searchButton = driver.findElementByXPath("//input[@class=\"search-btn\"]");
            actions.click(searchButton).perform();
            Thread.sleep(2000);
            //选择论文类型
            List<WebElement> typeElement = driver.findElementsByXPath("//ul[@class=\"doctype-menus keji\"]/li");
            if (typeElement.size() != 0) {
                WebElement journal = null;
                WebElement degree = null;
                for (WebElement types : typeElement) {
                    if (types.getAttribute("data-id").equals("xsqk")) {
                        journal = types;
                    } else if (types.getAttribute("data-id").equals("xwlw")) {
                        degree = types;
                    }
                }
                if (paper.getType().equals("期刊论文")) {
                    if (journal != null) {
                        actions.click(journal).perform();
                        Thread.sleep(2000);
                    }
                } else if (paper.getType().equals("学位论文")) {
                    if (degree != null) {
                        actions.click(degree).perform();
                        Thread.sleep(2000);
                    }
                }
            }
            WebElement target = null;
            int flag = 0;
            List<WebElement> matchElement = driver.findElementsByXPath("//table[@class=\"result-table-list\"]//tbody//tr");
            if (matchElement.size() == 0) {
                return;
            }
            for (WebElement match : matchElement) {
                WebElement matchTitle = match.findElement(By.xpath(".//td[@class=\"name\"]//a"));
                String matchTitleText = matchTitle.getText();
                if (!matchTitleText.equals(title)) {
                    continue;
                }
                List<WebElement> matchAuthors = match.findElements(By.xpath(".//td[@class=\"author\"]//a"));
                List<String> matchNames = new ArrayList<>();
                if (matchAuthors.size() != 0) {
                    for (WebElement matchAuthor : matchAuthors) {
                        matchNames.add(matchAuthor.getText());
                    }
                }
                for (String matchName : matchNames) {
                    if (authors.contains(matchName)) {
                        target = matchTitle;
                        flag = 1;
                        break;
                    }
                }
                if (flag == 1) {
                    break;
                }
            }
            if (flag == 0) {
                target = matchElement.get(0).findElement(By.xpath(".//td[@class=\"name\"]//a"));
            }
            if (target == null) {
                return;
            }
            // 切换页面
            String originalHandle = driver.getWindowHandle();
            actions.click(target).perform();
            Thread.sleep(1000);
            Set<String> allHandles = driver.getWindowHandles();
            allHandles.remove(originalHandle);
            if (allHandles.size() == 1)
                return;
            driver.switchTo().window((String) allHandles.toArray()[0]);
            // 添加外链
            String zhiWangUrl = driver.getCurrentUrl();
            paper = Objects.requireNonNull(statusCtrl.template.get(paper.getId(), Paper.class));
            List<Paper.Source> sources = paper.getSources();
            if (sources == null)
                sources = new ArrayList<>();
            List<String> sourcesText = new ArrayList<>();
            for (Paper.Source source : sources) {
                sourcesText.add(source.getWebsite());
            }
            if (!sourcesText.contains("知网")) {
                sources.add(new Paper.Source("知网", zhiWangUrl));
                paper.setSources(sources);
            }
            paper = statusCtrl.template.save(paper);
            sources.add(new Paper.Source("知网", zhiWangUrl));
            paper.setSources(sources);

            // 获取学科
            List<WebElement> subjectAndTopicElement = driver.findElementsByXPath("//li[@class=\"top-space\"]");
            if (subjectAndTopicElement.size() != 0) {
                for (WebElement subjectAndTopic : subjectAndTopicElement) {
                    String type = subjectAndTopic.findElement(By.xpath(".//span")).getText();
                    if (type.equals("专题：")) {
                        String allSubject = subjectAndTopic.findElement(By.xpath(".//p")).getText();
                        allSubject = allSubject.replaceAll(" ", "");
                        List<String> subjects = Arrays.asList(allSubject.split(";"));
                        // modify paper's subjects by this.paperCraw.getPaper().id
                        paper.setSubjects(subjects);
                        break;
                    }
                }
            }
            boolean withoutAuthorsId = true;
            for (Paper.Author author: paper.getAuthors()) {
                if (author.getId() != null) {
                    withoutAuthorsId = false;
                    break;
                }
            }
            if (withoutAuthorsId) {
                Paper paperInES = statusCtrl.template.get(paper.getId(), Paper.class);
                assert paperInES != null;
                paper.setAuthors(paperInES.getAuthors());
            }
            statusCtrl.paperRepository.save(paper);
            driver.close();
            driver.switchTo().window(originalHandle);
        } catch (Exception e) {
            StatusCtrl.errorHandler.report(e);
        }
    }

    // 获取外链
    // url格式： https://xueshu.baidu.com/s?wd= + 要搜索的title + &sc_hit=2&tn=SE_baiduxueshu_c1gjeupa&ie=utf-8
    public void baiduSpider() {
        String threadName = Thread.currentThread().getName();
        try {
            driver.get(this.paperCraw.getUrl());
            Thread.sleep(2000);
            Paper paper = statusCtrl.existenceService.findPaperById(paperCraw.getPaperId());
            String title = paper.getTitle();
            statusCtrl.changeRunningStatusTo(threadName, "Get sources of paper: " + title);
            List<Paper.Author> paperAuthors = paper.getAuthors();
            Set<String> authors = new HashSet<>();
            for (Paper.Author paperAuthor : paperAuthors) {
                authors.add(paperAuthor.getName());
            }
            WebElement target = null;
            boolean found = false;
            // 获取匹配元素
            List<WebElement> matchElement = driver.findElementsByXPath("//div[@class=\"result sc_default_result xpath-log\"]");
            if (matchElement.size() == 0) {
                return;
            }
            for (WebElement match : matchElement) {
                // 匹配标题
                WebElement matchTitle = match.findElement(By.xpath(".//div[@class=\"sc_content\"]//h3[@class=\"t c_font\"]//a"));
                String matchTitleText = matchTitle.getText();
                if (!matchTitleText.equals(title)) {
                    continue;
                }
                // 匹配作者
                List<WebElement> matchAuthors = match.findElements(By.xpath(".//div[@class=\"sc_info\"]//span//a[@data-click=\"{'button_tp':'author'}\"]"));
                List<String> matchNames = new ArrayList<>();
                if (matchAuthors.size() != 0) {
                    for (WebElement matchAuthor : matchAuthors) {
                        matchNames.add(matchAuthor.getText());
                    }
                }
                for (String matchName : matchNames) {
                    if (authors.contains(matchName)) {
                        target = match;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
                // 添加外链
                List<WebElement> sourceElement = target.findElements(By.xpath(".//div[@class=\"c_allversion\"]//span[contains(@class,\"v_item_span\")]//a[@class=\"v_source\"]"));
                List<Paper.Source> sources = paper.getSources();
                if (sources == null)
                    sources = new ArrayList<>();
                List<String> sourcesText = new ArrayList<>();
                for (Paper.Source source : sources) {
                    sourcesText.add(source.getWebsite());
                }
                if (sourceElement.size() != 0) {
                    for (WebElement source : sourceElement) {
                        String webName = source.getAttribute("title");
                        String webUrl = source.getAttribute("href");
                        if (!sourcesText.contains(webName)) {
                            Paper.Source newSource = new Paper.Source(webName, webUrl);
                            sources.add(newSource);
                            sourcesText.add(webName);
                        }
                    }
                    if (!sourcesText.contains("百度学术")) {
                        String baiduUrl = matchTitle.getAttribute("href");
                        Paper.Source newSource = new Paper.Source("百度学术", baiduUrl);
                        sources.add(newSource);
                        sourcesText.add("百度学术");
                    }
                    paper.setSources(sources);
                    statusCtrl.paperRepository.save(paper);
                }
            }
        } catch (Exception e) {
            StatusCtrl.errorHandler.report(e);
        }
    }
}
