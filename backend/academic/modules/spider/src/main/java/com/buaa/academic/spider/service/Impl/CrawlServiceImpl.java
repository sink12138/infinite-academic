package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.spider.service.CrawlService;
import com.buaa.academic.spider.util.EsUtil;
import com.buaa.academic.spider.util.ParserUtil;
import com.buaa.academic.spider.util.StatusCtrl;
import com.buaa.academic.spider.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CrawlServiceImpl implements CrawlService {
    @Autowired
    EsUtil esUtil;

    @Autowired
    ElasticsearchRestTemplate template;

    @Override
    public void crawlWithTitle(String title) {
        new Thread(new titleSpider(title, esUtil, template)).start();
    }

    @Override
    public void crawlWithUrl(String url) {
        new Thread(new urlSpider(url, esUtil, template)).start();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class urlSpider implements Runnable {
        private String url;
        private EsUtil esUtil;
        private ElasticsearchRestTemplate template;

        @SneakyThrows
        @Override
        public void run() {
            RemoteWebDriver driver = ParserUtil.getDriver(true);
            driver.get(url);
            ParserUtil.randomSleep(2000);
            Paper paper = new Paper();

            paper.setCrawled(true);
            template.save(paper);

            // 获取标题
            List<WebElement> titleElement = driver.findElementsByXPath("//span[@class=\"detailTitleCN\"]");
            if (titleElement.size() != 0) {
                String title = titleElement.get(0).getText();
                paper.setTitle(title);
            }

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
                        // find inst by name
                        Institution foundInst = esUtil.findInstByName(instName);
                        if (foundInst != null) {
                            inst.setId(foundInst.getId());
                            inst.setName(instName);
                        } else {
                            Institution newInst = new Institution();
                            newInst.setName(instName);
                            template.save(newInst);
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
                    Journal foundJournal = esUtil.findJournalByName(journalName);
                    if (foundJournal != null) {
                        journal.setId(foundJournal.getId());
                    } else {
                        foundJournal = new Journal();
                        foundJournal.setTitle(journalName);
                        template.save(foundJournal);
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
                        RemoteWebDriver journalDriver = ParserUtil.getDriver(true);
                        journalSpider(journalUrl, journalDriver);
                        journalDriver.quit();
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
                        Paper foundReferPaper = esUtil.findPaperByTileAndAuthors(referTitle, referAuthorList);
                        if (foundReferPaper == null) {
                            foundReferPaper = new Paper();
                            foundReferPaper.setCrawled(false);
                            foundReferPaper.setTitle(referTitle);
                            foundReferPaper.setAuthors(referAuthorList);
                            foundReferPaper.setCitationNum(1);
                            foundReferPaper.setType(refType);
                        } else {
                            foundReferPaper.setCitationNum(foundReferPaper.getCitationNum() + 1);
                        }
                        // 插入数据库
                        referenceID.add(foundReferPaper.getId());
                        template.save(foundReferPaper);
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

            paper.setReferences(referenceID);

            // 获取作者
            List<WebElement> authorElement = driver.findElementsByXPath("//div[@class=\"author list\"]//div[@class=\"itemUrl\"]//a");
            RemoteWebDriver authorsDriver = ParserUtil.getDriver(true);
            ArrayList<Paper.Author> authors = new ArrayList<>();
            if (authorElement.size() != 0) {
                for (WebElement author : authorElement) {
                    String authorName = author.getText();
                    String authorUrl = author.getAttribute("href");
                    if (authorUrl.startsWith("https://trend.wanfangdata.com.cn/scholarsBootPage")) {
                        Paper.Author authorRes = authorSpider(authorUrl, authorsDriver);
                        if (authorRes == null)
                            authorRes = new Paper.Author(null, authorName);
                        authors.add(authorRes);
                    }
                }
            }
            paper.setAuthors(authors);
            driver.quit();
            authorsDriver.quit();
            template.save(paper);
        }

        private Paper.Author authorSpider(String url, RemoteWebDriver driver) throws InterruptedException {
            driver.get(url);
            ParserUtil.randomSleep(5000);
            // 处理url非法
            String curUrl = driver.getCurrentUrl();
            if (!curUrl.startsWith("https://trend.wanfangdata.com.cn/scholarsBootPage")) {
                return null;
            }

            // 获取作者姓名
            List<WebElement> nameElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte scholar-name-show no-description\"]");
            if (nameElement.isEmpty())
                nameElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte scholar-name-show\"]");
            if (nameElement.isEmpty())
                return null;
            String researcherName = nameElement.get(0).getText();

            // 获取当前机构名称
            List<WebElement> curInstElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte unit-name \"]");
            if (curInstElement.isEmpty())
                curInstElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte unit-name\"]");
            if (curInstElement.isEmpty())
                return null;
            String instName = curInstElement.get(0).getAttribute("textContent");

            String[] instNames = instName.split("[;；]");
            instName = StringUtil.rmPlaceNameAndCode(instNames[0]);

            // 检查数据库中是否已有相同姓名和机构的数据库
            Researcher researcher = esUtil.findResearcherByNameAndInst(researcherName, instName);
            if (researcher == null) {
                researcher = new Researcher();
                researcher.setName(researcherName);
                researcher.setPaperNum(1);
                Researcher.Institution curInstitution = new Researcher.Institution();
                // get inst by name.
                Institution institution = esUtil.findInstByName(instName);
                if (institution == null) {
                    institution = new Institution();
                    institution.setName(instName);
                    template.save(institution);
                }
                curInstitution.setId(institution.getId());
                curInstitution.setName(instName);
                researcher.setCurrentInst(curInstitution);
            } else {
                researcher.setPaperNum(researcher.getPaperNum() + 1);
                return new Paper.Author(researcher.getId(), researcherName);
            }

            // 获取科研人员的H、G指数
            List<WebElement> scholarIndexElement = driver.findElementsByXPath("//ul[@class=\"scholar-index\"]//li");
            if (scholarIndexElement.size() != 0) {
                for (WebElement scholarIndex : scholarIndexElement) {
                    List<WebElement> indexElement = scholarIndex.findElements(By.xpath(".//p"));
                    if (indexElement.size() != 2) {
                        continue;
                    }
                    String scholar = indexElement.get(1).getAttribute("textContent");
                    if (scholar.equals("H指数")) {
                        Integer hIndex = Integer.valueOf(indexElement.get(0).getAttribute("textContent"));
                        researcher.setHIndex(hIndex);
                    } else if (scholar.equals("G指数")) {
                        Integer gIndex = Integer.valueOf(indexElement.get(0).getAttribute("textContent"));
                        researcher.setGIndex(gIndex);
                    }
                }
            }
            // 查看是否有“更多”按钮
            List<WebElement> moreElement = driver.findElementsByXPath("//div[@class=\"rt-bottom\"]//div[@class=\"more-wrapper\" and @style=\"display: block;\"]//a[@class=\"show-more\"]");
            if (moreElement.size() != 0) {
                WebElement morePoint = moreElement.get(0);
                Actions actions = new Actions(driver);
                actions.click(morePoint).perform();
                ParserUtil.randomSleep(1000);
            }

            // 获取合作机构
            List<WebElement> instElement = driver.findElementsByXPath("//div[@class=\"bottom-list\"]");
            if (instElement.size() != 0) {
                List<Researcher.Institution> corInstitutions = new ArrayList<>();
                for (WebElement inst : instElement) {
                    String corInst = inst.findElement(By.xpath(".//p[@class=\"list-title\"]/a")).getAttribute("textContent");

                    corInst = StringUtil.rmPlaceNameAndCode(corInst);

                    Institution institution = esUtil.findInstByName(corInst);
                    if (institution == null) {
                        institution = new Institution();
                        institution.setName(corInst);
                        template.save(institution);
                    }

                    Researcher.Institution corInstitution = new Researcher.Institution();
                    corInstitution.setId(institution.getId());
                    corInstitution.setName(corInst);
                    corInstitutions.add(corInstitution);
                }
                researcher.setInstitutions(corInstitutions);
            }

            // add researcher into database
            template.save(researcher);
            return new Paper.Author(researcher.getId(), researcherName);
        }

        private void journalSpider(String url, RemoteWebDriver driver) throws InterruptedException {
            driver.get(url);
            ParserUtil.randomSleep(2000);
            // 处理url非法
            String curUrl = driver.getCurrentUrl();
            if (curUrl.startsWith("https://s.wanfangdata.com.cn/")) {
                return;
            }
            Journal journal;
            // 获取期刊标题
            List<WebElement> nameElement = driver.findElementsByXPath("//h1[@class=\"lh-36 m-b-5 fs-24 fw-500\"]");
            if (nameElement.size() != 0) {
                WebElement name = nameElement.get(0);
                String allTitle = name.getText();
                String subTitle = name.findElement(By.xpath(".//wf-block")).getText();
                String title = allTitle.replace(subTitle, "");
                title = title.strip();
                journal = esUtil.findJournalByName(title);
            } else
                return;

            if (journal == null)
                return;

            // 获取期刊封面
            List<WebElement> logoElement = driver.findElementsByXPath("//wf-place-holder//img");
            if (logoElement.size() != 0) {
                String coverUrl = logoElement.get(0).getAttribute("src");
                journal.setCoverUrl(coverUrl);
            }
            // 获取基本信息
            List<WebElement> basicInfoElement = driver.findElementsByXPath("//div[@class=\"w-330 float-left m-t-10\"]");
            if (basicInfoElement.size() != 0) {
                for (WebElement info : basicInfoElement) {
                    String lable = info.findElement(By.xpath(".//wf-field-lable")).getText();
                    if (lable.equals("主办单位：")) {
                        String sponsor;
                        sponsor = info.findElement(By.xpath(".//wf-field-value")).getText();
                        journal.setSponsor(sponsor);
                    } else if (lable.equals("国际刊号：")) {
                        String issn;
                        issn = info.findElement(By.xpath(".//wf-field-value")).getText();
                        journal.setIssn(issn);
                    }
                }
            }
            template.save(journal);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class titleSpider implements Runnable {
        private String title;
        private EsUtil esUtil;
        private ElasticsearchRestTemplate template;


        @SneakyThrows
        @Override
        public void run() {
            RemoteWebDriver driver = ParserUtil.getDriver(true);
            String searchUrl = "https://s.wanfangdata.com.cn/paper?q=" + title + "&style=detail&s=50";
            driver.get(searchUrl);
            ParserUtil.randomSleep(2000);
            try {
                List<WebElement> searchResult = driver.findElementsByXPath("//table[@class=\"table-list\"]//tbody//tr[@class=\"table-list-item\"]");
                if (searchResult.size() != 0) {
                    WebElement result = searchResult.get(0);
                    if (StatusCtrl.jobStopped) {
                        driver.close();
                        driver.quit();
                        return;
                    }
                    if (result.findElements(By.xpath(".//span[@class=\"essay-type\"]")).isEmpty())
                        return;
                    String type = result.findElement(By.xpath(".//span[@class=\"essay-type\"]")).getText();
                    if (!type.equals("期刊论文") && !type.equals("硕士论文") && !type.equals("博士论文")) {
                        return;
                    }
                    WebElement titleEle = result.findElement(By.xpath(".//span[@class=\"title\"]"));

                    String originalHandle = driver.getWindowHandle();
                    Actions actions = new Actions(driver);
                    actions.click(titleEle).perform();
                    ParserUtil.randomSleep(2000);
                    Set<String> allHandles = driver.getWindowHandles();
                    allHandles.remove(originalHandle);
                    assert allHandles.size() == 1;
                    driver.switchTo().window((String) allHandles.toArray()[0]);
                    String url = driver.getCurrentUrl();
                    new Thread(new urlSpider(url, esUtil, template)).start();

                    driver.close();
                    driver.switchTo().window(originalHandle);

                }
            } catch (Exception e) {
                StatusCtrl.errorHandler.report(e);
            }
            driver.close();
            driver.quit();
        }
    }
}
