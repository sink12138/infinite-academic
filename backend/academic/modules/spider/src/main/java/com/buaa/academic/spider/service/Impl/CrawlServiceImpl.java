package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.document.system.Message;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.spider.service.CrawlService;
import com.buaa.academic.spider.util.EsUtil;
import com.buaa.academic.spider.util.PaperParser;
import com.buaa.academic.spider.util.ParserUtil;
import com.buaa.academic.spider.util.StringUtil;
import lombok.Data;
import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CrawlServiceImpl implements CrawlService {

    @Autowired
    private EsUtil esUtil;

    @Autowired
    private ElasticsearchRestTemplate template;

    public interface AutoSpider extends Runnable {
        Result<Paper> getResult();
    }

    @Override
    public void crawlWithUrl(String url, String userId) {
        AutoSpider spider;
        if (url.startsWith("https://kns.cnki.net/kcms/detail"))
            spider = new zhiwangUrlSpider(url);
        else
            spider = new WanfangUrlSpider(url);
        new Thread(() -> {
            Thread thread = new Thread(spider);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Result<Paper> result = spider.getResult();
            sendMsg(result, userId);
        }).start();
    }

    @Override
    public void crawlWithTitle(String title, String userId) {
        AutoSpider spider = new TitleSpider(title);
        new Thread(() -> {
            Thread thread = new Thread(spider);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Result<Paper> result = spider.getResult();
            sendMsg(result, userId);
        }).start();
    }

    private void sendMsg(Result<Paper> result, String userId) {
        Message msg = new Message();
        msg.setOwnerId(userId);
        String res = result.isSuccess() ? "成功" : "失败";
        msg.setTitle("自助添加文章" + res);
        if (result.isSuccess()) {
            msg.setContent(String.format("论文 《%s》 已由本系统自动获取完毕，请及时查看。如您发现错误，请提交修改申请或联系管理员。", result.getData().getTitle()));
        }
        else {
            msg.setContent(String.format("很抱歉，您提交的自助添加请求未能成功完成。原因：%s。", result.getMessage()));
        }
        msg.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        msg.setRead(false);
        template.save(msg);
    }

    @Data
    private class zhiwangUrlSpider implements AutoSpider{
        private String url;

        private Result<Paper> result = new Result<>(false, null, null);

        public zhiwangUrlSpider(String url) {
            this.url = url;
        }

        @Override
        public Result<Paper> getResult() {
            return result;
        }

        @SneakyThrows
        @Override
        public void run() {
            RemoteWebDriver driver = ParserUtil.getDriver(true);
            driver.get(url);
            ParserUtil.randomSleep(2000);
            String curUrl = driver.getCurrentUrl();
            if(!curUrl.startsWith("https://kns.cnki.net/kcms/detail")) {
                driver.close();
                driver.quit();
                result.setMessage("访问文章链接失败");
                return;
            }
            // 判断类型
            String paperType;
            if(curUrl.contains("CJFD")){
                paperType="期刊论文";
            }
            else if(curUrl.contains("CDFD")){
                paperType="学位论文";
            }
            else{
                result.setMessage("仅支持期刊论文和学位论文");
                driver.close();
                driver.quit();
                return;
            }
            String title;
            List<Paper.Author> authors=new ArrayList<>();
            List<WebElement> titleElement=driver.findElementsByXPath("//div[@class=\"wx-tit\"]//h1");
            if (titleElement.size() != 0) {
                title=titleElement.get(0).getAttribute("textContent");
            }
            else{
                result.setMessage("无法获取文章标题");
                driver.close();
                driver.quit();
                return;
            }
            // 作者
            List<WebElement> authorAndInst=driver.findElementsByXPath("//div[@class=\"wx-tit\"]//h3");
            if(authorAndInst.size()==0){
                result.setMessage("无法获取作者");
                driver.close();
                driver.quit();
                return;
            }
            WebElement allAuthor=authorAndInst.get(0);
            List<WebElement> authorElement;
            authorElement = allAuthor.findElements(By.xpath(".//span//a"));
            List<PaperParser.AuthorAndUrl> authorUrlList=new ArrayList<>();
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
                    if(getUrl == null){
                        PaperParser.AuthorAndUrl authorAndUrl=new PaperParser.AuthorAndUrl(nameText,null);
                        authorUrlList.add(authorAndUrl);
                        continue;
                    }
                    getUrl=getUrl.strip().replace("TurnPageToKnetV","").replace("(","").replace(");","").replace("'","");
                    String[] all=getUrl.split(",");
                    if(all.length!=4){
                        PaperParser.AuthorAndUrl authorAndUrl=new PaperParser.AuthorAndUrl(nameText,null);
                        authorUrlList.add(authorAndUrl);
                        continue;
                    }
                    String sfield=all[0];
                    String skey=all[1];
                    String code=all[2];
                    String v=all[3];
                    String todoUrl="https://kns.cnki.net/kcms/detail/knetsearch.aspx?sfield="+sfield+"&skey="+skey+"&code="+code+"&v="+v;
                    PaperParser.AuthorAndUrl authorAndUrl=new PaperParser.AuthorAndUrl(nameText,todoUrl);
                    authorUrlList.add(authorAndUrl);
                }
            }
            else{
                result.setMessage("无法获取作者");
                driver.close();
                driver.quit();
                return;
            }
            Paper paper = esUtil.findPaperByTileAndAuthors(title, authors);
            if (paper != null) {
                result.setMessage("数据库中已包含该篇文章");
                driver.close();
                driver.quit();
                return;
            }
            paper = new Paper();
            paper.setType(paperType);
            paper.setTitle(title);
            paper.setAuthors(authors);
            // 机构
            List<WebElement> instElement=driver.findElementsByXPath("//div[@class=\"wx-tit\"]//h3");
            if(instElement.size()==2){
                WebElement allInst=instElement.get(1);
                List<WebElement> insts=allInst.findElements(By.xpath(".//a"));
                if(insts.size() != 0) {
                    ArrayList<Paper.Institution> institutions = new ArrayList<>();
                    for (WebElement institution : insts) {
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
            if(paperType.equals("期刊论文")) {
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
                if(essayBoxElement.size()!=0) {
                    List<String> references=new ArrayList<>();
                    for(WebElement essayBox:essayBoxElement) {
                        String em=essayBox.findElement(By.xpath(".//em")).getText();
                        String allText=essayBox.getText();
                        String refer=allText.replace(em,"");
                        references.add(refer);
                    }
                    paper.setReferences(references);
                }
                driver.switchTo().defaultContent();
            }
            driver.close();
            driver.quit();
            // 作者url队列
            List<Paper.Author> researchers=new ArrayList<>();
            for(PaperParser.AuthorAndUrl authorAndUrl:authorUrlList) {
                String url = authorAndUrl.getUrl();
                String name=authorAndUrl.getName();
                if(url==null) {
                    Paper.Author paperResearcher=new Paper.Author();
                    paperResearcher.setName(name);
                    researchers.add(paperResearcher);
                    continue;
                }
                driver = ParserUtil.getDriver(true);
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
                Researcher researcher = esUtil.findResearcherByNameAndInst(researcherName, instName);
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
                Institution institution = esUtil.findInstByName(instName);
                if (institution == null) {
                    institution = new Institution();
                    institution.setName(instName);
                    // add into database
                    template.save(institution);
                    template.save(researcher);
                }
                curInstitution.setId(institution.getId());
                curInstitution.setName(instName);
                researcher.setCurrentInst(curInstitution);
                template.save(researcher);
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
                    template.save(researcher);
                }
                driver.close();
                driver.quit();
            }
            paper.setAuthors(researchers);
            if(paperType.equals("期刊论文") && journalUrl !=null ){
                driver = ParserUtil.getDriver(true);
                driver.get(journalUrl);
                Thread.sleep(2000);
                Journal foundJournal = esUtil.findJournalByName(paper.getJournal().getTitle());
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
                    template.save(foundJournal);
                    driver.close();
                    driver.quit();
                }
                Paper.Journal paperJournal=paper.getJournal();
                paperJournal.setId(foundJournal.getId());
                paper.setJournal(paperJournal);
                template.save(paper);
            }
            result.setData(paper);
            result.setSuccess(true);
        }
    }

    @Data
    private class WanfangUrlSpider implements AutoSpider {

        private String url;

        private Result<Paper> result = new Result<>(false, null, null);

        public WanfangUrlSpider(String url) {
            this.url = url;
        }

        @SneakyThrows
        @Override
        public void run() {
            RemoteWebDriver driver = ParserUtil.getDriver(true);
            driver.get(url);
            ParserUtil.randomSleep(2000);

            // 判断类型
            String paperType;
            if (url.contains("periodical")) {
                paperType = "期刊论文";
            }
            else if (url.contains("thesis")) {
                paperType = "学位论文";
            }
            else {
                result.setMessage("仅支持期刊论文和学位论文");
                return;
            }

            // 获取标题
            String title;
            List<WebElement> titleElement = driver.findElementsByXPath("//span[@class=\"detailTitleCN\"]");
            if (titleElement.size() != 0) {
                title = titleElement.get(0).getText();
            }
            else {
                result.setMessage("获取标题失败");
                return;
            }

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
            Paper paper = esUtil.findPaperByTileAndAuthors(title, authors);
            if (paper != null && paper.isCrawled()) {
                result.setMessage("数据库中已含有该论文");
                return;
            }
            else if (paper == null) {
                paper = new Paper();
                paper.setType(paperType);
                paper.setTitle(title);
                paper.setAuthors(authors);
                template.save(paper);
            }
            paper.setCrawled(true);

            try  {
                // 获取摘要
                List<WebElement> summaryElement = driver.findElementsByXPath("//div[@class=\"summary\"]");
                if (summaryElement.size() != 0) {
                    WebElement summary = summaryElement.get(0);
                    List<WebElement> noMoreElement = summary.findElements(By.xpath(".//span[@class=\"getMore\" and @style=\"display: none;\"]"));
                    List<WebElement> moreElement = summary.findElements(By.xpath(".//span[@class=\"getMore\"]"));
                    String allSummary = null;
                    if (noMoreElement.size() != 0) {
                        allSummary = summary.getText()
                                .replace("摘要：", "")
                                .replace("查看全部>>", "")
                                .replace("\n", "")
                                .strip();
                    } else if (moreElement.size() != 0) {
                        WebElement more = moreElement.get(0);
                        Actions actions = new Actions(driver);
                        actions.click(more).perform();
                        allSummary = summary.getText()
                                .replace("摘要：", "")
                                .replace("收起∧", "")
                                .replace("\n", "")
                                .strip();
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
                            parseJournal(journalUrl, journalDriver);
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
                boolean flag;
                do {
                    flag = false;
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
                                template.save(foundReferPaper);
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
                                flag = true;
                                Actions actions = new Actions(driver);
                                actions.click(able).perform();
                                break;
                            }
                        }
                    }
                    referenceElement.clear();
                    ableElement.clear();
                    ParserUtil.randomSleep(3000);
                } while (flag);

                paper.setReferences(referenceID);
            } catch (Exception e) {
                paper.setCrawled(false);
            }

            template.save(paper);
            result.setData(paper);
            result.setSuccess(true);
            driver.quit();
            authorsDriver.quit();
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

            // 检查数据库中是否已有相同姓名和机构的学者
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

        private void parseJournal(String url, RemoteWebDriver driver) throws InterruptedException {
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
    private class TitleSpider implements AutoSpider {

        private String title;

        private Result<Paper> result = new Result<>(false, null, null);

        public TitleSpider(String title) {
            this.title = title;
        }

        @SneakyThrows
        @Override
        public void run() {
            RemoteWebDriver driver = ParserUtil.getDriver(true);
            String searchUrl = "https://s.wanfangdata.com.cn/paper?q=" + title + "&style=detail&s=50";
            driver.get(searchUrl);
            ParserUtil.randomSleep(10000);
            List<WebElement> searchResult = driver.findElementsByXPath("//div[@class=\"normal-list\"]");
            if (searchResult.isEmpty()) {
                this.result.setMessage("未能联机搜索到指定的论文");
                return;
            }
            WebElement result = searchResult.get(0);
            if (result.findElements(By.xpath(".//span[@class=\"essay-type\"]")).isEmpty()) {
                this.result.setMessage("获取论文类型失败");
                return;
            }
            String type = result.findElement(By.xpath(".//span[@class=\"essay-type\"]")).getText();
            if (!type.equals("期刊论文") && !type.equals("硕士论文") && !type.equals("博士论文")) {
                this.result.setMessage("仅支持期刊论文和学位论文");
                return;
            }
            WebElement titleEle = result.findElement(By.xpath(".//span[@class=\"title\"]"));
            if (!titleEle.getText().equals(title)) {
                this.result.setMessage("未能联机搜索到指定的论文");
                return;
            }
            String originalHandle = driver.getWindowHandle();
            Actions actions = new Actions(driver);
            actions.click(titleEle).perform();
            ParserUtil.randomSleep(2000);
            Set<String> allHandles = driver.getWindowHandles();
            allHandles.remove(originalHandle);
            assert allHandles.size() == 1;
            driver.switchTo().window((String) allHandles.toArray()[0]);
            String url = driver.getCurrentUrl();
            AutoSpider spider = new WanfangUrlSpider(url);
            Thread thread = new Thread(spider);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.result = spider.getResult();
            driver.close();
            driver.quit();
        }
    }
}
