package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Journal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
public class JournalParser {
    private String url;

    private Journal journal;

    private StatusCtrl statusCtrl;

    private Boolean headless;

    private RemoteWebDriver driver;

    // 现在没用
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
        driver.get(this.url);
        Thread.sleep(3000);
        WebElement nameElement = null;
        WebElement coverUrlElement = null;
        WebElement sponsorElement = null;
        WebElement issnElement = null;
        WebElement basicInfoElement = null;
        List<WebElement> infoElements = new ArrayList<>();
        Journal journal = new Journal();
        try {
            nameElement = driver.findElementByXPath("//h3[@class=\"titbox\"]");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        try {
            coverUrlElement = driver.findElementByXPath("//img[@class=\"pic-book\"]");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        try {
            basicInfoElement = driver.findElementsByXPath("//ul[@id=\"JournalBaseInfo\"]//li").get(1);
            infoElements = basicInfoElement.findElements(By.xpath("//p"));
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        try {
            for (WebElement webElement : infoElements) {
                if (webElement.findElement(By.xpath("//label")).getText().equals("主办单位")) {

                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        try {
            issnElement = driver.findElementByXPath("//img[@class=\"pic-book\"]");
            issnElement.findElement(By.xpath(""));
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    public void baiduSpider() {

    }

    public void microsoftSpider() {
    }

    //本地测试完成
    public void wanFangSpider() {
        try {
            driver.get(this.url);
            Thread.sleep(2000);
            Journal journal = new Journal();
            // 获取期刊标题
            List<WebElement> nameElement = driver.findElementsByXPath("//h1[@class=\"lh-36 m-b-5 fs-24 fw-500\"]");
            if (nameElement.size() != 0) {
                WebElement name = nameElement.get(0);
                String allTitle = name.getText();
                String subTitle = name.findElement(By.xpath(".//wf-block")).getText();
                String title = allTitle.replace(subTitle, "");
                title = title.replace("\n", "");
                journal.setTitle(title);
            }

            statusCtrl.changeRunningStatusTo(Thread.currentThread().getName(), "Get info of journal: " + journal.getTitle());

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
            // insert journal into database
            statusCtrl.journalRepository.save(journal);
            this.journal = journal;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.close();
        }
    }
}
