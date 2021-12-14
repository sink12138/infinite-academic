package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.spider.model.queueObject.PaperObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
public class SearchParser {
    StatusCtrl statusCtrl;

    private String url;

    private Boolean headless;

    // 本部分用于初始化爬取队列
    public void wanFangSpider() throws InterruptedException {
        int crawledPaper = 0;
        int newPaper = 0;
        String threadName = Thread.currentThread().getName();
        RemoteWebDriver driver = ParserUtil.getDriver(headless);
        driver.get(this.url);
        Thread.sleep(2000);
        boolean continueCrawl;
        do {
            try {
                List<WebElement> searchResult = driver.findElementsByXPath("//table[@class=\"table-list\"]//tbody//tr[@class=\"table-list-item\"]");
                if (searchResult.size() != 0) {
                    for (WebElement result : searchResult) {

                        if (StatusCtrl.jobStopped) {
                            driver.close();
                            driver.quit();
                            return;
                        }

                        String type = result.findElement(By.xpath(".//span[@class=\"essay-type\"]")).getText();
                        if (!type.equals("期刊论文") && !type.equals("硕士论文") && !type.equals("博士论文")) {
                            continue;
                        }
                        WebElement title = result.findElement(By.xpath(".//span[@class=\"title\"]"));
                        String titleName = title.getText();
                        PaperObject paperObject = new PaperObject();

                        List<Paper.Author> authorList = new ArrayList<>();
                        List<WebElement> authors = result.findElements(By.xpath(".//td[contains(@style,\"line-height\")]//span[@class=\"authors\"]"));
                        if (authors.size() != 0) {
                            for (WebElement author : authors) {
                                String authorName = author.getText();
                                Paper.Author paperAuthor = new Paper.Author();
                                paperAuthor.setName(authorName);
                                authorList.add(paperAuthor);
                            }
                        }
                        crawledPaper++;
                        // find paper by referTitle and referAuthorName
                        Paper paper = statusCtrl.existenceService.findPaperByTileAndAuthors(titleName, authorList);
                        if (paper == null && !statusCtrl.existenceService.inTrash(titleName, authorList)) {
                            newPaper++;
                            paper = new Paper();
                            paper.setTitle(titleName);
                            paper.setAuthors(authorList);
                            paper.setCrawled(false);
                            paper.setCitationNum(0);
                            if (type.equals("期刊论文")) {
                                paper.setType("期刊论文");
                            } else {
                                paper.setType("学位论文");
                            }
                            // insert paper into database
                            statusCtrl.paperRepository.save(paper);
                            paperObject.setPaperId(paper.getId());
                            // 切换窗口,获取url，返回窗口
                            String originalHandle = driver.getWindowHandle();
                            Actions actions = new Actions(driver);
                            actions.click(title).perform();
                            Set<String> allHandles = driver.getWindowHandles();
                            allHandles.remove(originalHandle);
                            assert allHandles.size() == 1;
                            driver.switchTo().window((String) allHandles.toArray()[0]);
                            String url = driver.getCurrentUrl();
                            driver.close();
                            driver.switchTo().window(originalHandle);
                            paperObject.setUrl(url);

                            StatusCtrl.paperObjectQueue.add(paperObject);

                            PaperObject sourceObj = new PaperObject();
                            sourceObj.setUrl("https://xueshu.baidu.com/s?wd=" + titleName + "&sc_hit=2&tn=SE_baiduxueshu_c1gjeupa&ie=utf-8");
                            sourceObj.setPaperId(paper.getId());
                            StatusCtrl.sourceQueue.add(sourceObj);

                        } else if (paper != null && !paper.isCrawled()) {
                            newPaper++;
                            String url = driver.getCurrentUrl();
                            paperObject.setUrl(url);
                            paperObject.setPaperId(paper.getId());
                            StatusCtrl.paperObjectQueue.add(paperObject);
                            
                            PaperObject sourceObj = new PaperObject();
                            sourceObj.setUrl("https://xueshu.baidu.com/s?wd=" + titleName + "&sc_hit=2&tn=SE_baiduxueshu_c1gjeupa&ie=utf-8");
                            sourceObj.setPaperId(paper.getId());
                            StatusCtrl.sourceQueue.add(sourceObj);
                        }

                        statusCtrl.changeRunningStatusTo(threadName, "Crawl paper num: " + crawledPaper +
                                "; New paper num: " + newPaper);
                    }
                }
                List<WebElement> nextElement = driver.findElementsByXPath("//span[@class=\"next\"]");
                if (nextElement.size() == 0) {
                    continueCrawl = false;
                } else {
                    WebElement next = nextElement.get(0);
                    if (!next.getAttribute("style").equals("display: none;")) {
                        Actions actions = new Actions(driver);
                        actions.click(next).perform();
                        Thread.sleep(2000);
                        continueCrawl = true;
                    } else {
                        continueCrawl = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                continueCrawl = true;
            }
        } while (continueCrawl);
        driver.close();
        driver.quit();
    }
}
