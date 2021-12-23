package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Patent;
import com.buaa.academic.spider.model.queueObject.PatentObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PatentParser {
    private StatusCtrl statusCtrl;

    private RemoteWebDriver driver;

    public void wanFangSpider(PatentObject patentObject){
        String threadName = Thread.currentThread().getName();
        try {
            driver.get(patentObject.getUrl());
            ParserUtil.randomSleep(2000);
            Patent patent = statusCtrl.template.get(patentObject.getPatentId(), Patent.class);
            if (patent == null)
                return;
            // 获取标题
            List<WebElement> titleElement = driver.findElementsByXPath("//span[@class=\"detailTitleCN\"]");
            if (titleElement.size() != 0) {
                String title = titleElement.get(0).getText();
                patent.setTitle(title);
            }
            statusCtrl.changeRunningStatusTo(threadName, "Crawl info of patent with title: " + patent.getTitle());
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
                patent.setPatentAbstract(allSummary);
            }
            // 专利类型
            List<WebElement> typeElement = driver.findElementsByXPath("//div[@class=\"patentType list\"]//div[@class=\"itemUrl\"]");
            if (typeElement.size() != 0) {
                String type = typeElement.get(0).getText();
                patent.setType(type);
            }
            // 申请专利号
            List<WebElement> patentNumElement = driver.findElementsByXPath("//div[@class=\"patentCode list\"]//div[@class=\"itemUrl\"]");
            if (patentNumElement.size() != 0) {
                String patentNum = patentNumElement.get(0).getText();
                patent.setPatentNum(patentNum);
            }
            // 公开公告号
            List<WebElement> publicationNumElement = driver.findElementsByXPath("//div[@class=\"publicationNo list\"]//div[@class=\"itemUrl\"]");
            if (publicationNumElement.size() != 0) {
                String publicationNum = publicationNumElement.get(0).getText();
                patent.setPublicationNum(publicationNum);
            }
            // 申请日期、公开公告日
            List<WebElement> dateElement = driver.findElementsByXPath("//div[@class=\"applicationDate list\"]//div[@class=\"itemUrl\"]");
            if (dateElement.size() != 0) {
                for (WebElement date:dateElement) {
                    String dateType = date.findElement(By.xpath("..//span[@class=\"item\"]")).getText();
                    if (dateType.equals("申请日期：")) {
                        String applicationDate = date.getText();
                        patent.setFillingDate(applicationDate);
                    }
                    else if (dateType.equals("公开/公告日：")) {
                        String publicationDate = date.getText();
                        patent.setPublicationDate(publicationDate);
                    }
                }
            }
            // 主分类号
            List<WebElement> mainClassificationNumElement = driver.findElementsByXPath("//div[@class=\"mainClassCode list\"]//div[@class=\"itemUrl\"]");
            if (mainClassificationNumElement.size() != 0) {
                String mainClassificationNum = mainClassificationNumElement.get(0).getText();
                patent.setMainClassificationNum(mainClassificationNum);
            }
            // 分类号
            List<WebElement> ClassificationNumElement = driver.findElementsByXPath("//div[@class=\"classify list\"]//div[@class=\"itemUrl\"]");
            if (ClassificationNumElement.size() != 0) {
                String ClassificationNum = ClassificationNumElement.get(0).getText();
                patent.setClassificationNum(ClassificationNum);
            }
            // 申请人、发明人
            List<WebElement> applicantElement = driver.findElementsByXPath("//div[@class=\"applicant list\"]//div[@class=\"itemUrl\"]");
            if (applicantElement.size() != 0) {
                for (WebElement people:applicantElement) {
                    String peopleType = people.findElement(By.xpath("..//span[@class=\"item\"]")).getText();
                    if (peopleType.equals("申请/专利权人：")) {
                        String applicant = people.getText();
                        patent.setApplicant(applicant);
                    }
                    else if (peopleType.equals("发明/设计人：")) {
                        List<WebElement> inventorElement = people.findElements(By.xpath(".//a"));
                        if (inventorElement.size() != 0) {
                            List<Patent.Inventor> inventors = new ArrayList<>();
                            for (WebElement inventor:inventorElement) {
                                String inventorName = inventor.getText();
                                Patent.Inventor patentInventor=new Patent.Inventor();
                                patentInventor.setName(inventorName);
                                inventors.add(patentInventor);
                            }
                            patent.setInventors(inventors);
                        }
                    }
                }
            }
            // 地址
            List<WebElement> addressElement = driver.findElementsByXPath("//div[@class=\"applicantAddress list\"]//div[@class=\"itemUrl\"]");
            if (addressElement.size() != 0) {
                String address = addressElement.get(0).getText();
                patent.setAddress(address);
            }
            // 代理机构
            List<WebElement> agencyElement = driver.findElementsByXPath("//div[@class=\"agency list\"]//div[@class=\"itemUrl\"]");
            if (agencyElement.size() != 0) {
                String agency = agencyElement.get(0).getText();
                patent.setAgency(agency);
            }
            // 代理人
            List<WebElement> agentElement = driver.findElementsByXPath("//div[@class=\"agent list\"]//div[@class=\"itemUrl\"]");
            if (agentElement.size() != 0) {
                String agent = agentElement.get(0).getText();
                patent.setAgent(agent);
            }
            // 国省代码
            List<WebElement> countryProvinceCodeElement = driver.findElementsByXPath("//div[@class=\"applicantArea list\"]//div[@class=\"itemUrl\"]");
            if (countryProvinceCodeElement.size() != 0) {
                String countryProvinceCode = countryProvinceCodeElement.get(0).getText();
                patent.setCountryProvinceCode(countryProvinceCode);
            }
            // 主权项
            List<WebElement> claimElement = driver.findElementsByXPath("//div[@class=\"signoryItem list\"]//div[@class=\"itemUrl\"]");
            if (claimElement.size() != 0) {
                String claim = claimElement.get(0).getText();
                if (claim.length() > 2048) {
                    claim = claim.substring(0, 2048) + "...";
                }
                patent.setClaim(claim);
            }
            statusCtrl.patentRepository.save(patent);
        } catch (Exception e) {
            System.out.println(patentObject.getUrl());
            e.printStackTrace();
        }
    }
}
