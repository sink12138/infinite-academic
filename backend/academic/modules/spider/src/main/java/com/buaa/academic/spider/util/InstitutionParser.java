package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.spider.repository.InstitutionRepository;
import com.buaa.academic.spider.service.ExistenceService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
public class InstitutionParser {
    private String url;
    private Institution institution;

    @Autowired
    ExistenceService existenceService;

    @Autowired
    InstitutionRepository institutionRepository;

    //暂时都用不着

    //百度学术没有机构分类
    public void baiduSpider() {
    }

    public void zhiWangSpider() throws InterruptedException, NoSuchElementException {
//        String driverPath="C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe";
        ChromeOptions options = new ChromeOptions();
        //设置无网页
        options.addArguments("--headless");
//        System.setProperty("webdriver.chrome.driver",driverPath);
        RemoteWebDriver driver = new ChromeDriver(options);
        driver.get(this.url);
        ParserUtil.randomSleep(5000);
        //拖动滑块验证
        WebElement dstPicElement = null;
        WebElement srcPicElement = null;
        WebElement successElement = null;
        int dstX = 0;
        int srcX = 0;
        try {
            dstPicElement = driver.findElementByXPath("//div[@class=\"verify-gap\"]");
        } catch (NoSuchElementException e) {
            System.out.println("Can't find dstPic!");
        }
        try {
            srcPicElement = driver.findElementByXPath("//div[@class=\"verify-move-block\"]");
        } catch (NoSuchElementException e) {
            System.out.println("Can't find srcPic!");
        }
        try {
            successElement = driver.findElementByXPath("//span[@class=\"cate_right\"]");
        } catch (NoSuchElementException e) {
            System.out.println("Can't find success!");
        }
        if (dstPicElement != null) {
            dstX = dstPicElement.getLocation().getX();
            if (srcPicElement != null) {
                Actions action = new Actions(driver);
                action.click(srcPicElement).perform();
                srcX = srcPicElement.getLocation().getX();
                System.out.println("srcX:" + srcX + " dstX" + dstX);
                action.dragAndDropBy(srcPicElement, dstX - srcX + 4, 0).perform();
                srcX = srcPicElement.getLocation().getX();
                System.out.println("srcX:" + srcX + " dstX" + dstX);
                action.click(successElement).perform();
            }
        }
        ParserUtil.randomSleep(3000);
        System.out.println("Finish Hold and Drag");
        WebElement logoUrlElement = null;
        WebElement nameElement = null;
        try {
            nameElement = driver.findElementByXPath("//h1[@id=\"showname\"]");
        } catch (NoSuchElementException e) {
            System.out.println("Can't find name!");
        }
        try {
            logoUrlElement = driver.findElementByXPath("//div[@class=\"organ-logo\"]//img");
        } catch (NoSuchElementException e) {
            System.out.println("Can't find logoUrl!");

        }

        if (nameElement != null) {
            String name = nameElement.getText();
            Institution institution = existenceService.findInstByName(name);
            if (institution == null) {
                institution = new Institution();
                institution.setName(name);
                // 判断是否在数据库中
                if (logoUrlElement != null) {
                    String logoUrl = logoUrlElement.getAttribute("src");
                    institution.setLogoUrl(logoUrl);
                    System.out.println(logoUrl);
                }
                // 存入数据库
                institutionRepository.save(institution);
            }
        }
    }

    public void microsoftSpider() throws InterruptedException {
//        String driverPath = "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe";
        ChromeOptions options = new ChromeOptions();
        //设置无网页
//        options.addArguments("headless");
//        options.addArguments("window-size=1920x1080");
//        options.addArguments("no-sandbox");
//        System.setProperty("webdriver.chrome.driver",driverPath);
        RemoteWebDriver driver = new ChromeDriver(options);
        driver.get(this.url);
        ParserUtil.randomSleep(3000);
        WebElement nameElement = null;
        WebElement logoUrlElement = null;
        try {
            nameElement = driver.findElementByXPath("//h1[@class=\"name\"]");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        try {
            logoUrlElement = driver.findElementByXPath("//div[@class=\"edp institution\"]//img");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        if (nameElement != null) {
            String name = nameElement.getText();
            Institution institution = existenceService.findInstByName(name);
            if (institution == null) {
                institution = new Institution();
                institution.setName(name);
                System.out.println(name);
                // 判断是否在数据库中
                if (logoUrlElement != null) {
                    String logoUrl = logoUrlElement.getAttribute("src");
                    institution.setLogoUrl(logoUrl);
                    System.out.println(logoUrl);
                }
                // 存入数据库
                institutionRepository.save(institution);
            }
        }
    }

    //万方的机构没有图片
    public void wangFangSpider() {

    }
}
