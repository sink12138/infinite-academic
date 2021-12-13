package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Researcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Arrays;
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

            // 获取作者姓名
            List<WebElement> nameElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte scholar-name-show no-description\"]");
            String researcherName = null;
            String instName = null;
            if (nameElement.size() != 0) {
                researcherName = nameElement.get(0).getText();
            }

            statusCtrl.changeRunningStatusTo(Thread.currentThread().getName(), "Get info of researcher:" + researcherName);

            // 获取当前机构名称
            List<WebElement> curInstElement = driver.findElementsByXPath("//h3[@class=\"lt-top-tilte unit-name \"]");
            if (curInstElement.size() != 0) {
                instName = curInstElement.get(0).getText();
            }
            if (researcherName == null || instName == null) {
                driver.close();
                driver.quit();
                return;
            }

            String[] instNames = instName.split("[;；]");
            instName = instNames[0];

            if (instName.contains(",") || instName.contains("，")) {
                String[] instNameParts = instName.contains(",") ? instName.split(",") : instName.split("，");
                String instNameLastPart = instNameParts[instNameParts.length - 1].replace(" ", "");
                boolean isNum = instNameLastPart.matches(".+\\d{6}$");
                if (isNum) {
                    ArrayList<String> parts = new ArrayList<>(Arrays.asList(instNameParts));
                    parts.remove(parts.size() - 1);
                    instName = String.join(" ", parts);
                }
            }

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
                }
                curInstitution.setId(institution.getId());
                curInstitution.setName(instName);
                researcher.setCurrentInst(curInstitution);
            } else {
                this.researcher = researcher;
                researcher.setPaperNum(researcher.getPaperNum() + 1);
                statusCtrl.researcherRepository.save(researcher);
                driver.close();
                driver.quit();
                return;
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
                    if (scholar.equals("H指数")) {
                        Integer hIndex = Integer.valueOf(scholarIndex.findElement(By.xpath(".//p[@class=\"index-value\"]")).getText());
                        researcher.setHIndex(hIndex);
                    } else if (scholar.equals("G指数")) {
                        Integer gIndex = Integer.valueOf(scholarIndex.findElement(By.xpath(".//p[@class=\"index-value\"]")).getText());
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
                Thread.sleep(1000);
            }
            // 获取合作机构
            List<WebElement> instElement = driver.findElementsByXPath("//div[@class=\"bottom-list\"]");
            if (instElement.size() != 0) {
                List<Researcher.Institution> corInstitutions = new ArrayList<>();
                for (WebElement inst : instElement) {
                    String corInst = inst.findElement(By.xpath(".//p[@class=\"list-title\"]")).getText();

                    if (corInst.contains(",") || corInst.contains("，")) {
                        String[] corInstNameParts = corInst.contains(",") ? corInst.split(",") : corInst.split("，");
                        String corInstNameLastPart = corInstNameParts[corInstNameParts.length - 1].replace(" ", "");
                        boolean isNum = corInstNameLastPart.matches(".+\\d{6}$");
                        if (isNum) {
                            ArrayList<String> parts = new ArrayList<>(Arrays.asList(corInstNameParts));
                            parts.remove(parts.size() - 1);
                            corInst = String.join(" ", parts);
                        }
                    }

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

            ChromeDriverService service = ParserUtil.getDriverService();
            service.start();
            RemoteWebDriver subDriver = ParserUtil.getDriver(headless);

            ResearcherParser researcherParser = new ResearcherParser();
            researcherParser.setStatusCtrl(statusCtrl);
            researcherParser.setUrl("https://xueshu.baidu.com/usercenter/data/authorchannel?cmd=inject_page");
            researcherParser.setResearcher(researcher);
            researcherParser.setDriver(subDriver);
            researcherParser.baiDuSpider();

            subDriver.quit();
            service.stop();

            researcher.setInterests(researcherParser.getResearcher().getInterests());
            statusCtrl.researcherRepository.save(researcher);
            this.researcher = researcher;
        } catch (Exception e) {
            e.printStackTrace();
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
            }
            driver.close();
            driver.switchTo().window(originalHandle);
            driver.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
