package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Journal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
public class JournalParser {

    private String url;

    private Journal journal;

    private StatusCtrl statusCtrl;

    private RemoteWebDriver driver;

    //本地测试完成
    public void wanFangSpider() {
        try {
            driver.get(this.url);
            Thread.sleep(2000);
            Journal journal = null;
            // 获取期刊标题
            List<WebElement> nameElement = driver.findElementsByXPath("//h1[@class=\"lh-36 m-b-5 fs-24 fw-500\"]");
            if (nameElement.size() != 0) {
                WebElement name = nameElement.get(0);
                String allTitle = name.getText();
                String subTitle = name.findElement(By.xpath(".//wf-block")).getText();
                String title = allTitle.replace(subTitle, "");
                title = title.strip();
                journal = statusCtrl.existenceService.findJournalByName(title);
            } else
                return;

            assert journal != null;
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
        }
    }
}
