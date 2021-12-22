package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.spider.model.queueObject.PaperObject;
import com.buaa.academic.spider.model.queueObject.ResearcherSet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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

    private PaperObject paperCrawl;

    private StatusCtrl statusCtrl;

    private RemoteWebDriver driver;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class AuthorAndUrl{
        private String name;
        private String url;
    }

    public void wanFangSpider() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        Paper paper = statusCtrl.esUtil.findPaperById(paperCrawl.getPaperId());
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

        driver.get(this.paperCrawl.getUrl());
        ParserUtil.randomSleep(2000);
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
                Paper.Source source = new Paper.Source("万方", this.paperCrawl.getUrl());
                sources.add(source);
                paper.setSources(sources);
            }

            paper.setCrawled(true);
            statusCtrl.template.save(paper);

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
                    String[] terms = keyword.getText().split("[,，]+");
                    for (String term : terms) {
                        if (!term.isBlank())
                            keywords.add(term.strip());
                    }
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
                    String instNameText = institution.getAttribute("textContent");
                    String[] instNames = instNameText.split("[；;]");
                    for (String instName : instNames) {
                        Paper.Institution inst = new Paper.Institution();
                        instName = StringUtil.rmPlaceNameAndCode(instName);
                        statusCtrl.changeRunningStatusTo(threadName, "Get info of the institution with name: " + instName);
                        // find inst by name
                        Institution foundInst = statusCtrl.esUtil.findInstByName(instName);
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
                    Journal foundJournal = statusCtrl.esUtil.findJournalByName(journalName);
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
                    String volume = yearAndVolume.findElement(By.xpath(".//span")).getAttribute("textContent");
                    String year = all.replace(volume, "");
                    year = year.replace("\n", "");
                    volume = volume.replace(",", "");
                    String issue;
                    if (volume.contains("(") && volume.contains(")")) {
                        issue = volume.substring(volume.indexOf("(") + 1, volume.indexOf(")"));
                    } else {
                        issue = null;
                    }

                    volume = volume.replaceAll("\\([0-9]*\\)", "").strip();
                    paper.setYear(Integer.valueOf(year));
                    if (!volume.isBlank())
                        journal.setVolume(volume.strip());
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
                    if (!journalUrl.startsWith("https://s.wanfangdata.com.cn/")) {
                        StatusCtrl.journalUrls.add(journalUrl);
                    }
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
            statusCtrl.paperRepository.save(paper);

            // 获取参考文献
            List<String> referenceID = new ArrayList<>();
            List<WebElement> referenceElement;
            List<WebElement> ableElement;
            //toCrawPaperList = new ArrayList<>();
            int flag;
            do {
                flag = 0;
                referenceElement = driver.findElementsByXPath("//div[@id=\"reference\"]//div[@class=\"contentInfo\"]//td[@class=\"title\"]//a[@class=\"title\"]");
                if (referenceElement.size() != 0) {
                    for (WebElement reference : referenceElement) {
                        String referUrl = reference.getAttribute("href");
                        if (referUrl == null) {
                            String spaceHolder = reference.getText();
                            referenceID.add(spaceHolder);
                            continue;
                        }
                        String referTitle = reference.getText();
                        List<WebElement> tmp = reference.findElements(By.xpath("..//span[@style=\"vertical-align: middle;\"]"));
                        String type = tmp.get(1).getText();
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
                        if (!type.startsWith("[J]") && !type.startsWith("[D]")) {
                            referenceID.add(reference.getText());
                            continue;
                        }
                        String refType = type.startsWith("[J]") ? "期刊论文" : "学位论文";
                        // find paper by referTitle and referAuthorName
                        Paper foundReferPaper = statusCtrl.esUtil.findPaperByTileAndAuthors(referTitle, referAuthorList);
                        if (foundReferPaper == null) {
                            foundReferPaper = new Paper();
                            foundReferPaper.setCrawled(false);
                            foundReferPaper.setTitle(referTitle);
                            foundReferPaper.setAuthors(referAuthorList);
                            foundReferPaper.setCitationNum(1);
                            foundReferPaper.setType(refType);
                        }
                        else {
                            foundReferPaper.setCitationNum(foundReferPaper.getCitationNum() + 1);
                        }
                        // 插入数据库
                        statusCtrl.paperRepository.save(foundReferPaper);
                        referenceID.add(foundReferPaper.getId());
                        if (!foundReferPaper.isCrawled()) {
                            // 把url塞进队列
                            PaperObject paperObject = new PaperObject(referUrl, foundReferPaper.getId(), paperCrawl.getDepth() - 1);
                            StatusCtrl.paperObjectQueue.add(paperObject);
                            StatusCtrl.sourceQueue.add(paperObject);
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
                ParserUtil.randomSleep(3000);
            } while (flag == 1);

            paper = statusCtrl.template.get(paper.getId(), Paper.class);
            assert paper != null;
            paper.setReferences(referenceID);
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
    }

    //获取文章的topic、subject
    public void zhiWangSpider() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        driver.get(this.paperCrawl.getUrl());
        ParserUtil.randomSleep(2000);
        Paper paper = statusCtrl.esUtil.findPaperById(paperCrawl.getPaperId());
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
        ParserUtil.randomSleep(2000);
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
                    ParserUtil.randomSleep(2000);
                }
            } else if (paper.getType().equals("学位论文")) {
                if (degree != null) {
                    actions.click(degree).perform();
                    ParserUtil.randomSleep(2000);
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
        ParserUtil.randomSleep(1000);
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
        for (Paper.Author author : paper.getAuthors()) {
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
    }

    // 获取外链
    // url格式： https://xueshu.baidu.com/s?wd= + 要搜索的title + &sc_hit=2&tn=SE_baiduxueshu_c1gjeupa&ie=utf-8
    public void baiduSpider() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        driver.get(this.paperCrawl.getUrl());
        ParserUtil.randomSleep(2000);
        Paper paper = statusCtrl.esUtil.findPaperById(paperCrawl.getPaperId());
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
            Paper paperSource = statusCtrl.template.get(paper.getId(), Paper.class);
            assert paperSource != null;
            List<Paper.Source> sources = paperSource.getSources();
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
    }

    public void baiduPaperSpider() throws InterruptedException{
        // todo 线程相关（paperCrawl）
        driver.get(this.paperCrawl.getUrl());
        ParserUtil.randomSleep(2000);
        String curUrl=driver.getCurrentUrl();
        if(!curUrl.startsWith("https://kns.cnki.net/kcms/detail")){
            driver.close();
            driver.quit();
            return;
        }
        // 判断类型
        String paperType;
        if(curUrl.contains("CJFD")){
            paperType="J";
        }
        else if(curUrl.contains("CDFD")){
            paperType="D";
        }
        else{
            driver.close();
            driver.quit();
            return;
        }
        String title=null;
        List<Paper.Author> authors=new ArrayList<>();
        // 标题
        List<WebElement> titleElement=driver.findElementsByXPath("//div[@class=\"wx-tit\"]//h1");
        if (titleElement.size() != 0) {
            title=titleElement.get(0).getAttribute("textContent");
        }
        else{
            driver.close();
            driver.quit();
            return;
        }
        // 作者
        List<WebElement> authorAndInst=driver.findElementsByXPath("//div[@class=\"wx-tit\"]//h3");
        if(authorAndInst.size()==0){
            driver.close();
            driver.quit();
            return;
        }
        WebElement allAuthor=authorAndInst.get(0);
        List<WebElement> authorElement;
        authorElement = allAuthor.findElements(By.xpath(".//span//a"));
        List<AuthorAndUrl> authorUrlList=new ArrayList<>();
        // 作者及其url
        if(authorElement.size()!=0){
            for(WebElement authorEl:authorElement){
                Paper.Author paperAuthor=new Paper.Author();
                String nameText=authorEl.getText();
                List<WebElement> numElement=authorEl.findElements(By.xpath(".//sup"));
                if(numElement.size()!=0){
                    String num=numElement.get(0).getText();
                    nameText=nameText.replace(num,"");
                }
                paperAuthor.setName(nameText);
                authors.add(paperAuthor);
                // 拼接url
                String getUrl=authorEl.getAttribute("onclick");
                if(getUrl==null){
                    AuthorAndUrl authorAndUrl=new AuthorAndUrl(nameText,null);
                    authorUrlList.add(authorAndUrl);
                    continue;
                }
                getUrl=getUrl.strip().replace("TurnPageToKnetV","").replace("(","").replace(");","").replace("'","");
                String[] all=getUrl.split(",");
                if(all.length!=4){
                    AuthorAndUrl authorAndUrl=new AuthorAndUrl(nameText,null);
                    authorUrlList.add(authorAndUrl);
                    continue;
                }
                String sfield=all[0];
                String skey=all[1];
                String code=all[2];
                String v=all[3];
                String todoUrl="https://kns.cnki.net/kcms/detail/knetsearch.aspx?sfield="+sfield+"&skey="+skey+"&code="+code+"&v="+v;
                AuthorAndUrl authorAndUrl=new AuthorAndUrl(nameText,todoUrl);
                authorUrlList.add(authorAndUrl);
            }
        }
        else{
            driver.close();
            driver.quit();
            return;
        }
        Paper paper = statusCtrl.esUtil.findPaperByTileAndAuthors(title, authors);
        if(paper!=null){
            driver.close();
            driver.quit();
            return;
        }
        paper=new Paper();
        paper.setType(paperType);
        paper.setTitle(title);
        paper.setAuthors(authors);
        // 机构
        List<WebElement> instElement=driver.findElementsByXPath("//div[@class=\"wx-tit\"]//h3");
        if(instElement.size()==2){
            WebElement allInst=instElement.get(1);
            List<WebElement> insts=allInst.findElements(By.xpath(".//a"));
            if(insts.size()!=0){
                ArrayList<Paper.Institution> institutions = new ArrayList<>();
                for (WebElement institution : insts) {
                    String instNameText = institution.getAttribute("textContent");
                    String[] instNames = instNameText.split("[；;]");
                    for (String instName : instNames) {
                        Paper.Institution inst = new Paper.Institution();
                        instName = StringUtil.rmPlaceNameAndCode(instName);
                        instName=instName.replaceAll("\\d+. ","");
                        // find inst by name
                        Institution foundInst = statusCtrl.esUtil.findInstByName(instName);
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
        }
        // 摘要、关键词
        List<WebElement> briefElement=driver.findElementsByXPath("//div[@class=\"brief\"]//div[@class=\"row\"]");
        if(briefElement.size()!=0){
            for(WebElement brief:briefElement){
                String type=brief.findElement(By.xpath(".//span[@class=\"rowtit\"]")).getText();
                if(type.equals("关键词：")){
                    List<WebElement> keywordElement=brief.findElements(By.xpath(".//p[@class=\"keywords\"]//a"));
                    if(keywordElement.size()!=0){
                        List<String> keywords=new ArrayList<>();
                        for(WebElement keyword:keywordElement){
                            String keywordText=keyword.getAttribute("textContent");
                            keywordText=keywordText.replace(";","");
                            keywordText=keywordText.replace("；","");
                            keywordText=keywordText.strip();
                            keywords.add(keywordText);
                        }
                        paper.setKeywords(keywords);
                    }
                }
                else if(type.equals("摘要：")){
                    List<WebElement> moreElement=brief.findElements(By.xpath(".//span//a[@id=\"ChDivSummaryMore\"]"));
                    if(moreElement.size()!=0){
                        WebElement more=moreElement.get(0);
                        String style=more.getAttribute("style");
                        if(style.equals("")){
                            Actions actions=new Actions(driver);
                            actions.click(more).perform();
                            Thread.sleep(2000);
                        }
                    }
                    List<WebElement> abstractElement=brief.findElements(By.xpath(".//span[@id=\"ChDivSummary\"]"));
                    if(abstractElement.size()!=0){
                        String abstractText=abstractElement.get(0).getAttribute("textContent");
                        abstractText=abstractText.strip();
                        paper.setPaperAbstract(abstractText);
                    }
                }
            }
        }
        String journalUrl = null;
        // 期刊进行这一步操作
        if(paperType.equals("J")){
            // 期刊、发表年份、卷数、期数
            List<WebElement> journalInfoElement=driver.findElementsByXPath("//div[@class=\"top-tip\"]//span//a");
            if(journalInfoElement.size()==2){
                Paper.Journal paperJournal=new Paper.Journal();
                WebElement journalElement=journalInfoElement.get(0);
                String journalName=journalElement.getAttribute("textContent");
                // 拼接url
                journalUrl=journalElement.getAttribute("onclick");
                if(journalUrl!=null) {
                    journalUrl=journalUrl.strip().replace("getKns8NaviLink","").replace("(","").replace(");","").replace("'","");
                    String[] all=journalUrl.split(",");
                    if(all.length!=2){
                        journalUrl=null;
                    }
                    else {
                        String dbcode=all[0];
                        String baseid=all[1];
                        journalUrl="https://kns.cnki.net/kcms/detail/navipage.aspx?dbcode="+dbcode+"&baseid="+baseid;
                    }
                }
                paperJournal.setTitle(journalName);
                WebElement yearAndVolumeElement=journalInfoElement.get(1);
                String yearAndVolume=yearAndVolumeElement.getAttribute("textContent");
                String year=yearAndVolume.substring(0,yearAndVolume.indexOf(","));
                String issue=yearAndVolume.substring(yearAndVolume.indexOf(",")+1,yearAndVolume.indexOf("("));
                String volume=yearAndVolume.substring(yearAndVolume.indexOf("(")+1,yearAndVolume.indexOf(")"));
                paper.setYear(Integer.valueOf(year));
                paper.setDate(year+"01-01");
                paperJournal.setVolume(volume);
                paperJournal.setIssue(issue);
                paper.setJournal(paperJournal);
            }
        }
        // 学科、DOI
        List<WebElement> subjectAndTopicElement = driver.findElementsByXPath("//li[@class=\"top-space\"]");
        if (subjectAndTopicElement.size() != 0) {
            for (WebElement subjectAndTopic : subjectAndTopicElement) {
                String type = subjectAndTopic.findElement(By.xpath(".//span")).getText();
                if (type.equals("专题：")) {
                    String allSubject = subjectAndTopic.findElement(By.xpath(".//p")).getText();
                    allSubject = allSubject.replaceAll(" ", "");
                    List<String> subjects = Arrays.asList(allSubject.split(";"));
                    paper.setSubjects(subjects);
                }
                else if(type.equals("DOI：")){
                    String doi = subjectAndTopic.findElement(By.xpath(".//p")).getText();
                    paper.setDoi(doi);
                }
            }
        }
        // 参考文献
        List<WebElement> iframeElement=driver.findElementsByXPath("//iframe[@id=\"frame1\"]");
        // 切换iframe
        if(iframeElement.size()!=0){
            driver.switchTo().frame(iframeElement.get(0));
            List<WebElement> essayBoxElement=driver.findElementsByXPath("//div[@class=\"essayBox\"]//li");
            if(essayBoxElement.size()!=0){
                List<String> references=new ArrayList<>();
                for(WebElement essayBox:essayBoxElement){
                    String em=essayBox.findElement(By.xpath(".//em")).getText();
                    String allText=essayBox.getText();
                    String refer=allText.replace(em,"");
                    references.add(refer);
                }
                paper.setReferences(references);
            }
            driver.switchTo().defaultContent();
        }
        statusCtrl.paperRepository.save(paper);
        driver.close();
        driver.quit();
        // 作者url队列
        List<Paper.Author> researchers=new ArrayList<>();
        for(AuthorAndUrl authorAndUrl:authorUrlList){
            String url=authorAndUrl.getUrl();
            String name=authorAndUrl.getName();
            if(url==null){
                Paper.Author paperResearcher=new Paper.Author();
                paperResearcher.setName(name);
                researchers.add(paperResearcher);
                continue;
            }
            ChromeOptions options = new ChromeOptions().setHeadless(false).addArguments("--blink-settings=imagesEnabled=false");
            driver = new ChromeDriver(options);
            driver.get(url);
            Thread.sleep(2000);
            String researcherName;
            String instName;
            List<WebElement> authorNameElement=driver.findElementsByXPath("//h1[@id=\"showname\"]");
            if(authorNameElement.size()==0){
                Paper.Author paperResearcher=new Paper.Author();
                paperResearcher.setName(name);
                researchers.add(paperResearcher);
                driver.close();
                driver.quit();
                continue;
            }
            researcherName=authorNameElement.get(0).getText();
            List<WebElement> curInstElement=driver.findElementsByXPath("//h3//span//a");
            if(curInstElement.size()==0){
                Paper.Author paperResearcher=new Paper.Author();
                paperResearcher.setName(name);
                researchers.add(paperResearcher);
                driver.close();
                driver.quit();
                continue;
            }
            instName=curInstElement.get(0).getText();
            Researcher researcher = statusCtrl.esUtil.findResearcherByNameAndInst(researcherName, instName);
            if(researcher!=null){
                Paper.Author paperResearcher=new Paper.Author();
                paperResearcher.setName(researcher.getName());
                paperResearcher.setId(researcher.getId());
                researchers.add(paperResearcher);
                driver.close();
                driver.quit();
                continue;
            }

            researcher=new Researcher();
            researcher.setName(researcherName);
            Researcher.Institution curInstitution = new Researcher.Institution();
            // get inst by name.
            Institution institution = statusCtrl.esUtil.findInstByName(instName);
            if (institution == null) {
                institution = new Institution();
                institution.setName(instName);
                // add into database
                statusCtrl.institutionRepository.save(institution);
                statusCtrl.researcherRepository.save(researcher);
            }
            curInstitution.setId(institution.getId());
            curInstitution.setName(instName);
            researcher.setCurrentInst(curInstitution);
            statusCtrl.researcherRepository.save(researcher);
            // 加入文章作者列表
            Paper.Author paperResearcher=new Paper.Author();
            paperResearcher.setName(researcher.getName());
            paperResearcher.setId(researcher.getId());
            researchers.add(paperResearcher);
            // 获取方向
            List<WebElement> interestsElement=driver.findElementsByXPath("//div[@class=\"doc\"]/div/h3");
            if(interestsElement.size()==2){
                String interestText=interestsElement.get(1).getText();
                if(interestText.contains(";")||interestText.contains("；")){
                    interestText=interestText.replace(";",",");
                    interestText=interestText.replace("；",",");
                }
                interestText=interestText.strip();
                String[] interests = interestText.split(",");
                researcher.setInterests(Arrays.asList(interests));
                statusCtrl.researcherRepository.save(researcher);
            }
            driver.close();
            driver.quit();
        }
        paper.setAuthors(researchers);
        if(paperType.equals("J")&&journalUrl!=null){
            ChromeOptions options = new ChromeOptions().setHeadless(false).addArguments("--blink-settings=imagesEnabled=false");
            driver = new ChromeDriver(options);
            driver.get(journalUrl);
            Thread.sleep(2000);
            Journal foundJournal = statusCtrl.esUtil.findJournalByName(paper.getJournal().getTitle());
            if(foundJournal==null){
                String journalName=paper.getJournal().getTitle();
                foundJournal=new Journal();
                foundJournal.setTitle(journalName);
                List<WebElement> imageElement=driver.findElementsByXPath("//dt[@id=\"J_journalPic\"]//img");
                if(imageElement.size()!=0){
                    String imageUrl=imageElement.get(0).getAttribute("src");
                    foundJournal.setCoverUrl(imageUrl);
                }
                List<WebElement> journalMoreElement=driver.findElementsByXPath("//div[@class=\"btnbox\"]//a[@id=\"J_sumBtn-stretch\"]");
                if(journalMoreElement.size()!=0){
                    Actions actions=new Actions(driver);
                    actions.click(journalMoreElement.get(0)).perform();
                }
                List<WebElement> infoElement=driver.findElementsByXPath("//p[contains(@class,\"hostUnit\")]");
                if(infoElement.size()!=0){
                    for (WebElement info:infoElement){
                        List<WebElement> typeElement=info.findElements(By.xpath(".//label"));
                        if(typeElement.size()==0){
                            continue;
                        }
                        String type=typeElement.get(0).getAttribute("textContent");
                        if(type.equals("主办单位")){
                            String sponsor=info.findElement(By.xpath(".//span")).getAttribute("textContent");
                            foundJournal.setSponsor(sponsor);
                        }
                        else if(type.equals("ISSN")){
                            String issn=info.findElement(By.xpath(".//span")).getAttribute("textContent");
                            foundJournal.setIssn(issn);
                        }
                    }
                }
                statusCtrl.journalRepository.save(foundJournal);
                driver.close();
                driver.quit();
            }
            Paper.Journal paperJournal=paper.getJournal();
            paperJournal.setId(foundJournal.getId());
            paper.setJournal(paperJournal);
        }
    }
}
