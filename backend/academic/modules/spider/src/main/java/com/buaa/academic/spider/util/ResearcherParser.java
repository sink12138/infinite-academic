package com.buaa.academic.spider.util;


import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.spider.repository.InstitutionRepository;
import com.buaa.academic.spider.repository.ResearcherRepository;
import com.buaa.academic.spider.service.ExistenceService;
import com.buaa.academic.spider.service.Impl.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
public class ResearcherParser {
    private String url;
    private Researcher researcher;

    private StatusCtrl statusCtrl;

    public void wanFangSpider() throws InterruptedException {
        ChromeOptions options = new ChromeOptions().setHeadless(true);
        RemoteWebDriver driver = new ChromeDriver(options);
        driver.get(this.url);
        Thread.sleep(2000);
        Researcher researcher = new Researcher();
        // 获取作者姓名
        List<WebElement> nameElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte scholar-name-show no-description\"]");
        if (nameElement.size() != 0) {
            String name = nameElement.get(0).getText();
            researcher.setName(name);
            System.out.println("作者姓名： " + name);
        }
        // 获取当前机构
        List<WebElement> curInstElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte unit-name \"]");
        if (curInstElement.size() != 0) {
            String curInst = curInstElement.get(0).getText();
            System.out.println("当前机构： " + curInst);
            Researcher.Institution curInstitution = new Researcher.Institution();

            // get inst by name.
            Institution institution = statusCtrl.existenceService.findInstByName(curInst);
            if (institution == null) {
                institution = new Institution();
                institution.setName(curInst);
                // add into database
                statusCtrl.institutionRepository.save(institution);
            }

            curInstitution.setId(institution.getId());
            curInstitution.setName(curInst);
            researcher.setCurrentInst(curInstitution);
        }
        // 获取科研人员的H、G指数
        List<WebElement> scholarIndexElement = driver.findElementsByXPath("//ul[@class=\"scholar-index\"]//li");
        if (scholarIndexElement.size() != 0) {
            for (WebElement scholarIndex : scholarIndexElement) {
                List<WebElement> indexElement = scholarIndex.findElements(By.xpath(".//p"));
                if (indexElement.size() == 0) {
                    continue;
                }
                String scholar = indexElement.get(1).getText();
                System.out.println(scholar);
                if (scholar.equals("H指数")) {
                    Integer hIndex = Integer.valueOf(scholarIndex.findElement(By.xpath(".//p[@class=\"index-value\"]")).getText());
                    researcher.setHIndex(hIndex);
                    System.out.println("H指数： " + hIndex);
                } else if (scholar.equals("G指数")) {
                    Integer gIndex = Integer.valueOf(scholarIndex.findElement(By.xpath(".//p[@class=\"index-value\"]")).getText());
                    researcher.setGIndex(gIndex);
                    System.out.println("G指数： " + gIndex);
                }
            }
        }
        // 获取科研人员成果信息
        List<WebElement> citationElement = driver.findElementsByXPath("//div[@class=\"lt-num-text\"]");
        if (citationElement.size() != 0) {
            for (WebElement cit : citationElement) {
                String citName = cit.findElement(By.xpath(".//p")).getText();
                System.out.println(citName);
                if (citName.equals("总文献量")) {
                    int paperNum = Integer.parseInt(cit.findElement(By.xpath(".//span")).getText());
                    researcher.setPaperNum(paperNum);
                    System.out.println("总文献量： " + paperNum);
                } else if (citName.equals("总被引量")) {
                    int citationNum = Integer.parseInt(cit.findElement(By.xpath(".//span")).getText());
                    researcher.setCitationNum(citationNum);
                    System.out.println("总被引量： " + citationNum);
                }
            }
        }
        // 查看是否有“更多”按钮
        List<WebElement> moreElement = driver.findElementsByXPath("//div[@class=\"rt-bottom\"]//div[@class=\"more-wrapper\" and @style=\"display: block;\"]//a[@class=\"show-more\"]");
        if (moreElement.size() != 0) {
            WebElement morePoint = moreElement.get(0);
            Actions actions = new Actions(driver);
            actions.click(morePoint).perform();
            Thread.sleep(1000);
        }
        // 获取合作机构
        List<WebElement> instElement = driver.findElementsByXPath("//div[@class=\"bottom-list\"]");
        if (instElement.size() != 0) {
            List<Researcher.Institution> corInstitutions = new ArrayList<>();
            for (WebElement inst : instElement) {
                String corInst = inst.findElement(By.xpath(".//p[@class=\"list-title\"]")).getText();
                System.out.println("合作机构： " + corInst);

                Institution institution = statusCtrl.existenceService.findInstByName(corInst);
                if (institution == null) {
                    institution = new Institution();
                    institution.setName(corInst);
                    statusCtrl.institutionRepository.save(institution);
                }

                Researcher.Institution corInstitution = new Researcher.Institution();
                corInstitution.setId(institution.getId());
                corInstitution.setName(corInst);
                corInstitutions.add(corInstitution);
            }
            researcher.setInstitutions(corInstitutions);
        }
        // add researcher into database
        statusCtrl.researcherRepository.save(researcher);

        driver.close();
        ResearcherParser researcherParser = new ResearcherParser();
        researcherParser.setStatusCtrl(statusCtrl);
        researcherParser.setUrl("https://xueshu.baidu.com/usercenter/data/authorchannel?cmd=inject_page");
        researcherParser.setResearcher(researcher);
        researcherParser.baiDuSpider();
        this.researcher = researcherParser.getResearcher();
        statusCtrl.researcherRepository.save(researcher);
    }

    //获取作者研究领域
    // https://xueshu.baidu.com/usercenter/data/authorchannel?cmd=inject_page
    public void baiDuSpider() throws InterruptedException {
        ChromeOptions options = new ChromeOptions().setHeadless(true);
        RemoteWebDriver driver = new ChromeDriver(options);
        driver.get(this.url);
        Thread.sleep(2000);
        String researcherName = this.researcher.getName();
        String curInstName = this.researcher.getCurrentInst().getName();
        List<WebElement> searchText = driver.findElementsByXPath("//form[@class=\"searchForm\"]//p[@class=\"formItem\"]");
        if (searchText.size() >= 2) {
            for (WebElement submitText : searchText) {
                String type = submitText.findElement(By.xpath(".//span[@class=\"label\"]")).getText();
                WebElement text = submitText.findElement(By.xpath(".//input[@type=\"text\"]"));
                if (type.equals("姓名")) {
                    text.sendKeys(researcherName);
                } else if (type.equals("机构")) {
                    text.sendKeys(curInstName);
                }
            }
        }
        WebElement searchButton = driver.findElementByXPath("//form[@class=\"searchForm\"]//input[@type=\"submit\"]");
        Actions actions = new Actions(driver);
        actions.click(searchButton).perform();
        Thread.sleep(2000);
        List<WebElement> matchResearchers = driver.findElementsByXPath("//div[@id=\"personalSearch_result\"]//div[contains(@class,\"searchResultItem\")]");
        if (matchResearchers.size() == 0) {
            driver.close();
            return;
        }
        WebElement target = null;
        for (WebElement matchResearcher : matchResearchers) {
            String name = matchResearcher.findElement(By.xpath(".//div[@class=\"searchResult_text\"]//a[@class=\"personName\"]")).getText();
            String instName = matchResearcher.findElement(By.xpath(".//div[@class=\"searchResult_text\"]//p[contains(@class,\"personInstitution\")]")).getText();
            List<WebElement> interest = matchResearcher.findElements(By.xpath(".//div[@class=\"searchResult_text\"]//p[@class=\"personField\"]"));
            if (name.equals(researcherName) && curInstName.contains(instName) && interest.size() != 0) {
                target = matchResearcher.findElement(By.xpath(".//div[@class=\"searchResult_text\"]//a[@class=\"personName\"]"));
                break;
            }
        }
        if (target == null) {
            driver.close();
            return;
        }
        //切换页面
        actions.click(target).perform();
        String originalHandle = driver.getWindowHandle();
        Set<String> allHandles = driver.getWindowHandles();
        allHandles.remove(originalHandle);
        assert allHandles.size() == 1;
        driver.switchTo().window((String) allHandles.toArray()[0]);
        Thread.sleep(1000);
        List<WebElement> majorElement = driver.findElementsByXPath("//span[@class=\"person_domain person_text\"]//a");
        List<String> interests = new ArrayList<>();
        if (majorElement.size() != 0) {
            for (WebElement major : majorElement) {
                interests.add(major.getText());
            }
            this.researcher.setInterests(interests);

            System.out.println("interests: \n");
            for (String interest: interests)
                System.out.println(interest + " ");
            System.out.println("\n");
            //todo modify researcher.interests by researcher.id
        }
        driver.close();
        driver.switchTo().window(originalHandle);
        driver.close();
    }
}
