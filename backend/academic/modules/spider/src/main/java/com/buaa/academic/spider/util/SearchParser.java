package com.buaa.academic.spider.util;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchParser {
    private String url;
    private List<PaperObject> rootPaperList;

    // 本部分用于初始化爬取队列
    public void wanFangSpider() throws InterruptedException {
        ChromeOptions options = new ChromeOptions().setHeadless(true);
        RemoteWebDriver driver = new ChromeDriver(options);
        driver.get(this.url);
        Thread.sleep(2000);
        int flag;
        int totalPages = 0;
        do {
            System.out.println("初始化!");
            this.rootPaperList = new ArrayList<>();
            List<WebElement> searchResult = driver.findElementsByXPath("//table[@class=\"table-list\"]//tbody//tr[@class=\"table-list-item\"]");
            if (searchResult.size() != 0) {
                for (WebElement result : searchResult) {
                    String type = result.findElement(By.xpath(".//span[@class=\"essay-type\"]")).getText();
                    if (!type.equals("期刊论文")) {
                        continue;
                    }
                    WebElement title = result.findElement(By.xpath(".//span[@class=\"title\"]"));
                    String titleName = title.getText();
                    PaperObject paperObject = new PaperObject();


                    List<Paper.Author> authorList = new ArrayList<>();
                    List<WebElement> authors = result.findElements(By.xpath(".//td[@style=\"line-height: 20px;\"]"));
                    if (authors.size() != 0) {
                        for (WebElement author : authors) {
                            String authorName = author.getText();
                            Paper.Author paperAuthor = new Paper.Author();
                            paperAuthor.setName(authorName);
                            authorList.add(paperAuthor);
                        }
                    }
                    Paper paper = new Paper();
                    //todo find paper by referTitle and referAuthorName
                    if (paper != null) {
                        paper.setTitle(titleName);
                        paper.setAuthors(authorList);
                        paper.setCrawled(false);
                        //todo insert paper into database
                        paperObject.setPaper(paper);
                        // 切换窗口,获取url，返回窗口
                        String originalHandle = driver.getWindowHandle();
                        Actions actions = new Actions(driver);
                        actions.click(title).perform();
                        Set<String> allHandles = driver.getWindowHandles();
                        allHandles.remove(originalHandle);
                        assert allHandles.size() == 1;
                        driver.switchTo().window((String) allHandles.toArray()[0]);
                        String url = driver.getCurrentUrl();
                        System.out.println(url);
                        driver.close();
                        driver.switchTo().window(originalHandle);
                        paperObject.setUrl(url);
                        rootPaperList.add(paperObject);
                    }
                }
            }
            totalPages++;
            WebElement next = driver.findElementByXPath("//span[@class=\"next\"]");
            if (!next.getAttribute("style").equals("display: none;")) {
                Actions actions = new Actions(driver);
                actions.click(next).perform();
                Thread.sleep(2000);
                flag = 1;
            } else {
                flag = 0;
            }
        } while (flag != 0 && totalPages <= 2);
        driver.close();
    }
}
