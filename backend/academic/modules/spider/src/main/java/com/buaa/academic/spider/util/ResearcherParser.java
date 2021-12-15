package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Researcher;
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
public class ResearcherParser {

    private String url;

    private Researcher researcher;

    private StatusCtrl statusCtrl;

    private RemoteWebDriver driver;

    private boolean headless;

    public void wanFangSpider() {
        try {
            driver.get(this.url);
            Thread.sleep(2000);
            // 处理url非法
            String curUrl = driver.getCurrentUrl();
            if (!curUrl.startsWith("https://trend.wanfangdata.com.cn/scholarsBootPage")) {
                return;
            }

            // 获取作者姓名
            List<WebElement> nameElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte scholar-name-show no-description\"]");
            if (nameElement.isEmpty())
                nameElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte scholar-name-show\"]");
            if (nameElement.isEmpty())
                return;
            String researcherName = nameElement.get(0).getText();

            statusCtrl.changeRunningStatusTo(Thread.currentThread().getName(), "Get info of researcher:" + researcherName);

            // 获取当前机构名称
            List<WebElement> curInstElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte unit-name \"]");
            if (curInstElement.isEmpty())
                curInstElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte unit-name\"]");
            if (curInstElement.isEmpty())
                return;
            String instName = curInstElement.get(0).getText();

            String[] instNames = instName.split("[;；]");
            instName = StringUtil.rmPlaceNameAndCode(instNames[0]);

            // 检查数据库中是否已有相同姓名和机构的数据库
            Researcher researcher = statusCtrl.existenceService.findResearcherByNameAndInst(researcherName, instName);
            if (researcher == null) {
                researcher = new Researcher();
                researcher.setName(researcherName);
                researcher.setPaperNum(1);
                Researcher.Institution curInstitution = new Researcher.Institution();
                // get inst by name.
                Institution institution = statusCtrl.existenceService.findInstByName(instName);
                if (institution == null) {
                    institution = new Institution();
                    institution.setName(instName);
                    // add into database
                    statusCtrl.institutionRepository.save(institution);
                    statusCtrl.researcherRepository.save(researcher);
                }
                curInstitution.setId(institution.getId());
                curInstitution.setName(instName);
                researcher.setCurrentInst(curInstitution);
            } else {
                this.researcher = researcher;
                researcher.setPaperNum(researcher.getPaperNum() + 1);
                statusCtrl.researcherRepository.save(researcher);
                return;
            }


            // 获取科研人员的H
            List<WebElement> scholarIndexElement = driver.findElementsByXPath("//ul[@class=\"scholar-index\"]//li//p[@class=\"index-value\"]");
            if (scholarIndexElement.size() != 0) {
                Integer hIndex = Integer.valueOf(scholarIndexElement.get(0).getText());
                researcher.setHIndex(hIndex);
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
                    String corInst = inst.findElement(By.xpath(".//p[@class=\"list-title\"]/a")).getText();

                    corInst = StringUtil.rmPlaceNameAndCode(corInst);

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
            /*
            String originalHandle = driver.getWindowHandle();
            driver.executeScript("window.open('about:blank','_blank');");
            Set<String> handles = driver.getWindowHandles();
            handles.remove(originalHandle);
            driver.switchTo().window(handles.toArray(new String[0])[0]);*/
            ResearcherParser researcherParser = new ResearcherParser();
            researcherParser.setStatusCtrl(statusCtrl);
            researcherParser.setUrl("https://xueshu.baidu.com/usercenter/data/authorchannel?cmd=inject_page");
            researcherParser.setResearcher(researcher);
            researcherParser.setDriver(driver);
            researcherParser.baiDuSpider();
            //driver.switchTo().window(originalHandle);

            researcher.setInterests(researcherParser.getResearcher().getInterests());
            statusCtrl.researcherRepository.save(researcher);
            this.researcher = researcher;
        } catch (Exception e) {
            StatusCtrl.errorHandler.report(e);
        }
    }

    //获取作者研究领域
    // https://xueshu.baidu.com/usercenter/data/authorchannel?cmd=inject_page
    public void baiDuSpider() {
        try {
            driver.get(this.url);
            Thread.sleep(2000);
            String researcherName = this.researcher.getName();
            String curInstName = this.researcher.getCurrentInst().getName();
            List<WebElement> searchText = driver.findElementsByXPath("//form[@class=\"searchForm\"]//p[@class=\"formItem\"]");
            if (searchText.size() >= 2) {
                for (WebElement submitText : searchText) {
                    String type = submitText.findElement(By.xpath(".//span[@class=\"label\"]")).getText();
                    WebElement text = submitText.findElement(By.xpath(".//input[@type=\"text\"]"));
                    if (type.equals("*姓名")) {
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
                return;
            }
            WebElement target = null;
            for (WebElement matchResearcher : matchResearchers) {
                String name = matchResearcher.findElement(By.xpath(".//div[@class=\"searchResult_text\"]//a[@class=\"personName\"]")).getText();
                name = name.replace("\uE61F","");
                String instName = matchResearcher.findElement(By.xpath(".//div[@class=\"searchResult_text\"]//p[contains(@class,\"personInstitution\")]")).getText();
                List<WebElement> interest = matchResearcher.findElements(By.xpath(".//div[@class=\"searchResult_text\"]//p[@class=\"personField\"]"));
                if (name.equals(researcherName) && (curInstName.contains(instName) || instName.contains(curInstName)) && interest.size() != 0) {
                    target = matchResearcher.findElement(By.xpath(".//div[@class=\"searchResult_text\"]//a[@class=\"personName\"]"));
                    break;
                }
            }
            if (target == null) {
                return;
            }
            //切换页面
            String originalHandle = driver.getWindowHandle();
            actions.click(target).perform();
            Thread.sleep(1000);
            Set<String> allHandles = driver.getWindowHandles();
            allHandles.remove(originalHandle);
            if (allHandles.size() == 1)
                return;
            driver.switchTo().window((String) allHandles.toArray()[0]);
            List<WebElement> majorElement = driver.findElementsByXPath("//span[@class=\"person_domain person_text\"]//a");
            List<String> interests = new ArrayList<>();
            if (majorElement.size() != 0) {
                for (WebElement major : majorElement) {
                    interests.add(major.getText());
                }
                this.researcher.setInterests(interests);
            }
            List<WebElement> indexElement = driver.findElementsByXPath("//ul[@class=\"p_ach_wr\"]//li[@class=\"p_ach_item\"]");
            if (indexElement.size() != 0) {
                for (WebElement index:indexElement) {
                    String type = index.findElement(By.xpath(".//p[contains(@class,\"p_ach_type\")]")).getText();
                    if (type.equals("H指数")) {
                         Integer hIndex = Integer.valueOf(index.findElement(By.xpath(".//p[@class=\"p_ach_num\"]")).getText());
                         this.researcher.setHIndex(hIndex);
                    }
                    else if (type.equals("G指数")) {
                        Integer gIndex = Integer.valueOf(index.findElement(By.xpath(".//p[@class=\"p_ach_num\"]")).getText());
                        this.researcher.setHIndex(gIndex);
                    }
                }
            }
            driver.close();
            driver.switchTo().window(originalHandle);
        } catch (Exception e) {
            StatusCtrl.errorHandler.report(e);
        }
    }
}
