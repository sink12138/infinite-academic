package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.spider.model.queueObject.PaperObject;
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
    private PaperObject paperCraw;
    //private List<PaperObject> toCrawPaperList;
    private StatusCtrl statusCtrl;

    private JournalParser journalParser;


    public void wanFangSpider() throws InterruptedException {
        RemoteWebDriver driver = null;
        boolean success = false;
        ChromeOptions options = new ChromeOptions().setHeadless(true);
        while (!success) {
            try {
                driver = new ChromeDriver(options);
                success = true;
            } catch (Exception ignored) {}
        }
        driver.get(this.paperCraw.getUrl());
        Thread.sleep(2000);
        // find paper by this.paperCraw.getPaper().id;
        Paper paper = statusCtrl.existenceService.findPaperById(paperCraw.getPaper().getId());
        // 已经爬完了
        if (paper.isCrawled()) {
            driver.close();
            driver.quit();
            return;
        }
        paper.setCrawled(true);
        // 获取标题
        List<WebElement> titleElement = driver.findElementsByXPath("//span[@class=\"detailTitleCN\"]");
        if (titleElement.size() != 0) {
            String title = titleElement.get(0).getText();
            paper.setTitle(title);
            System.out.println("文章标题： " + title);
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
            System.out.println("摘要： " + allSummary);
        }
        // 获取DOI
        List<WebElement> DOIElement = driver.findElementsByXPath("//div[@class=\"doi list\"]//div[@class=\"itemUrl\"]//a");
        if (DOIElement.size() != 0) {
            String doi = DOIElement.get(0).getText();
            paper.setDoi(doi);
            System.out.println("DOI：" + doi);
        }
        // 获取关键词
        List<WebElement> keywordElement = driver.findElementsByXPath("//div[@class=\"keyword list\"]//div[@class=\"itemUrl\"]//a");
        if (keywordElement.size() != 0) {
            List<String> keywords = new ArrayList<>();
            for (WebElement keyword : keywordElement) {
                String word = keyword.getText();
                keywords.add(word);
                System.out.println("关键词： " + word);
            }
            paper.setKeywords(keywords);
        }
        // 获取作者
        List<WebElement> authorElement = driver.findElementsByXPath("//div[@class=\"author list\"]//div[@class=\"itemUrl\"]//a");
        if (authorElement.size() != 0) {
            List<Paper.Author> authors = new ArrayList<>();
            for (WebElement author : authorElement) {
                Paper.Author partAuthor = new Paper.Author();
                String authorName = author.getText();
                String authorUrl = author.getAttribute("href");
                System.out.println("作者姓名： " + authorName);
                System.out.println("作者主页： " + authorUrl);
                partAuthor.setName(authorName);
                ResearcherParser researcherParser = new ResearcherParser();
                researcherParser.setStatusCtrl(this.statusCtrl);
                researcherParser.setUrl(authorUrl);
                researcherParser.wanFangSpider();
                partAuthor.setId(researcherParser.getResearcher().getId());
                authors.add(partAuthor);
            }
            paper.setAuthors(authors);
        }
        // 获取参与机构
        List<WebElement> instElement = driver.findElementsByXPath("//div[@class=\"organization list\"]//div[@class=\"itemUrl\"]//a");
        if (instElement.size() != 0) {
            List<Paper.Institution> institutions = new ArrayList<>();
            for (WebElement institution : instElement) {
                Paper.Institution inst = new Paper.Institution();
                String instNameText = institution.getText();
                String instUrl = institution.getAttribute("href");
                String[] instNames = instNameText.split("；");
                for (String instName: instNames) {
                    if (instName.contains("，")) {
                        String[] instNameParts = instName.split("，");
                        String instNameLastPart = instNameParts[instNameParts.length - 1].replace(" ", "");
                        boolean isNum = instNameLastPart.matches("\\d{6}$");
                        if (isNum) {
                            List<String> parts = Arrays.asList(instNameParts);
                            parts.remove(parts.size() - 1);
                            instName = String.join(" ", parts);
                        }
                    }

                    System.out.println("机构名称： " + instName);
                    System.out.println("机构主页： " + instUrl);
                    // find inst by name
                    Institution foundInst = statusCtrl.existenceService.findInstByName(instName);
                    if (foundInst != null) {
                        inst.setId(foundInst.getId());
                        inst.setName(instName);
                    } else {
                        Institution newInst = new Institution();
                        newInst.setName(instName);
                        // insert inst(with name) into database
                        statusCtrl.institutionRepository.save(newInst);
                        inst.setId(newInst.getId());
                        inst.setName(instName);
                    }
                    institutions.add(inst);
                }
            }
            paper.setInstitutions(institutions);
        }
        // 获取期刊
        Paper.Journal journal = new Paper.Journal();
        List<WebElement> journalElement = driver.findElementsByXPath("//div[@class=\"serialTitle list\"]//div[@class=\"itemUrl\"]//a");
        if (journalElement.size() != 0) {
            String journalName = journalElement.get(0).getText();
            String journalUrl = journalElement.get(0).getAttribute("href");
            journal.setTitle(journalName);
            System.out.println("期刊标题： " + journalName);
            System.out.println("期刊主页： " + journalUrl);
            // find journal by name
            Journal foundJournal = statusCtrl.existenceService.findJournalByName(journalName);
            if (foundJournal != null) {
                journal.setId(foundJournal.getId());
            } else {
                // JournalParser journalParser = new JournalParser();
                journalParser.setUrl(journalUrl);
                journalParser.wanFangSpider();
                journal.setId(journalParser.getJournal().getId());
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
                issue = "null";
            }

            volume = volume.replaceAll("\\([0-9]*\\)", "");
            paper.setYear(Integer.valueOf(year));
            journal.setVolume(volume);
            journal.setIssue(issue);
            System.out.println("发表年份： " + year);
            System.out.println("卷号： " + volume);
            System.out.println("期号： " + issue);
        }
        // 获取起始页
        List<WebElement> pageNumElement = driver.findElementsByXPath("//div[@class=\"pageNum list\"]//div[@class=\"itemUrl\"]");
        if (pageNumElement.size() != 0) {
            String all = pageNumElement.get(0).getText();
            System.out.println(all);
            String pattern = "[0-9]*-[0-9]*";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(all);
            if (m.matches()) {
                String start = all.substring(0, all.indexOf("-"));
                String end = all.substring(all.indexOf("-") + 1);
                journal.setStartPage(Integer.valueOf(start));
                journal.setEndPage(Integer.valueOf(end));
                System.out.println("起始页： " + start);
                System.out.println("终止页： " + end);
            }
        }
        paper.setJournal(journal);
        // 获取发表日期
        List<WebElement> publishElement = driver.findElementsByXPath("//div[@class=\"publish list\"]//div[@class=\"itemUrl\"]");
        if (publishElement.size() != 0) {
            String date = publishElement.get(0).getText();
            date = date.replace("（万方平台首次上网日期，不代表论文的发表时间）", "");
            date = date.replace("\n", "").replace(" ", "");
            paper.setDate(date);
            System.out.println(date);
        } else {
            String date = paper.getYear() + "-01-01";
            paper.setDate(date);
            System.out.println(date);
        }
        /* 被引量不能爬取，应该在爬取过程中进行进行统计
        // 获取被引量
        List<WebElement> citeElement = driver.findElementsByXPath("//div[@id=\"cite\"]//div[@class=\"contentTitle\"]//span[@class=\"heade\"]");
        if (citeElement.size() != 0) {
            String cite = citeElement.get(0).getText();
            if (cite.contains("(") && cite.contains(")")) {
                cite = cite.substring(cite.indexOf("(") + 1, cite.indexOf(")"));
            } else {
                cite = "0";
            }
            paper.setCitationNum(Integer.parseInt(cite));
            System.out.println("被引量： " + cite);
        } else {
            paper.setCitationNum(0);
            System.out.println("被引量： 0");
        } */
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
                    System.out.println("参考文献标题： " + referTitle);
                    System.out.println("参考文献主页： " + referUrl);
                    System.out.println("参考文献类型： " + type);
                    List<WebElement> referAuthorElement = reference.findElements(By.xpath("..//span[@class=\"author\"]//span//a"));
                    List<Paper.Author> referAuthorList = new ArrayList<>();
                    if (referAuthorElement.size() != 0) {
                        for (WebElement referAuthor : referAuthorElement) {
                            Paper.Author referPaperAuthor = new Paper.Author();
                            String referName = referAuthor.getText();
                            referPaperAuthor.setName(referName);
                            referAuthorList.add(referPaperAuthor);
                            System.out.println("参考文献作者： " + referName);
                        }
                    }
                    // 只爬期刊
                    if (type.startsWith("[J]")) {
                        // find paper by referTitle and referAuthorName
                        Paper foundReferPaper = statusCtrl.existenceService.findPaperByTileAndAuthors(referTitle, referAuthorList);
                        if (foundReferPaper == null) {
                            foundReferPaper = new Paper();
                            foundReferPaper.setCrawled(false);
                            foundReferPaper.setTitle(referTitle);
                            foundReferPaper.setAuthors(referAuthorList);
                            foundReferPaper.setCitationNum(1);
                            foundReferPaper.setType("J");
                            // 插入数据库
                        } else {
                            foundReferPaper.setCitationNum(foundReferPaper.getCitationNum() + 1);
                        }
                        statusCtrl.paperRepository.save(foundReferPaper);
                        // 把url塞进队列
                        PaperObject paperObject = new PaperObject(referUrl, foundReferPaper);
                        //toCrawPaperList.add(paperObject);
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
        // modify the paper‘s properties by paperCraw.getPaper().id
        statusCtrl.paperRepository.save(paper);
        driver.close();
        driver.quit();
    }

    //获取文章的topic、subject
    public void zhiWangSpider() throws InterruptedException {
        RemoteWebDriver driver = null;
        boolean success = false;
        ChromeOptions options = new ChromeOptions().setHeadless(true);
        while (!success) {
            try {
                driver = new ChromeDriver(options);
                success = true;
            } catch (Exception ignored) {}
        }
        driver.get(this.paperCraw.getUrl());
        Thread.sleep(2000);
        String title = this.paperCraw.getPaper().getTitle();
        List<Paper.Author> paperAuthors = this.paperCraw.getPaper().getAuthors();
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
                    actions.click(searchType).perform();
                    break;
                }
            }
        }
        // 搜索
        List<WebElement> searchTextElement = driver.findElementsByXPath("//input[@id=\"txt_search\"]");
        if (searchTextElement.size() != 0) {
            WebElement searchText = searchTextElement.get(0);
            searchText.sendKeys(this.paperCraw.getPaper().getTitle());
        }
        //todo maybe small bug
        WebElement searchButton = driver.findElementByXPath("//input[@class=\"search-btn\"]");
        actions.click(searchButton).perform();
        Thread.sleep(2000);
        WebElement target = null;
        int flag = 0;
        List<WebElement> matchElement = driver.findElementsByXPath("//table[@class=\"result-table-list\"]//tbody//tr");
        if (matchElement.size() == 0) {
            driver.close();
            driver.quit();
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
            driver.close();
            driver.quit();
            return;
        }
        // 切换页面
        actions.click(target).perform();
        String originalHandle = driver.getWindowHandle();
        Set<String> allHandles = driver.getWindowHandles();
        allHandles.remove(originalHandle);
        assert allHandles.size() == 1;
        driver.switchTo().window((String) allHandles.toArray()[0]);
        List<WebElement> subjectAndTopicElement = driver.findElementsByXPath("//li[@class=\"top-space\"]");

        Paper paper = statusCtrl.existenceService.findPaperById(paperCraw.getPaper().getId());

        if (subjectAndTopicElement.size() != 0) {
            for (WebElement subjectAndTopic : subjectAndTopicElement) {
                String type = subjectAndTopic.findElement(By.xpath(".//span")).getText();
                if (type.equals("专题：")) {
                    String allSubject = subjectAndTopic.findElement(By.xpath(".//p")).getText();
                    allSubject = allSubject.replaceAll(" ", "");
                    List<String> subjects = Arrays.asList(allSubject.split(";"));
                    // modify paper's subjects by this.paperCraw.getPaper().id
                    paper.setSubjects(subjects);
                }
            }
        }

        statusCtrl.paperRepository.save(paper);

        driver.close();
        driver.switchTo().window(originalHandle);
        driver.close();
        driver.quit();
    }
}