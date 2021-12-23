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

    private boolean banned;

    public void wanFangSpider() throws InterruptedException {
        driver.get(this.url);
        ParserUtil.randomSleep(5000);
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
        String instName = curInstElement.get(0).getAttribute("textContent");

        String[] instNames = instName.split("[;；]");
        instName = StringUtil.formatInstitutionName(instNames[0]);

        // 检查数据库中是否已有相同姓名和机构的数据库
        Researcher researcher = statusCtrl.esUtil.findResearcherByNameAndInst(researcherName, instName);
        if (researcher == null) {
            researcher = new Researcher();
            researcher.setName(researcherName);
            researcher.setPaperNum(1);
            Researcher.Institution curInstitution = new Researcher.Institution();
            // get inst by name.
            Institution institution = statusCtrl.esUtil.findInstByName(instName);
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
            statusCtrl.researcherRepository.save(researcher);
        } else {
            this.researcher = researcher;
            researcher.setPaperNum(researcher.getPaperNum() + 1);
            statusCtrl.researcherRepository.save(researcher);
            return;
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

                corInst = StringUtil.formatInstitutionName(corInst);

                Institution institution = statusCtrl.esUtil.findInstByName(corInst);
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
        this.researcher = researcher;
        StatusCtrl.interestsQueue.add(researcher);
    }

    //获取作者研究领域
    // https://xueshu.baidu.com/usercenter/data/authorchannel?cmd=inject_page
    public void baiDuSpider() throws InterruptedException {
        String researcherName = this.researcher.getName();
        String curInstName = this.researcher.getCurrentInst().getName();
        List<WebElement> searchText = driver.findElementsByXPath("//form[@class=\"searchForm\"]//p[@class=\"formItem\"]");
        if (searchText.size() >= 2) {
            for (WebElement submitText : searchText) {
                String type = submitText.findElement(By.xpath(".//span[@class=\"label\"]")).getText();
                WebElement text = submitText.findElement(By.xpath(".//input[@type=\"text\"]"));
                if (type.equals("*姓名")) {
                    text.clear();
                    text.sendKeys(researcherName);
                } else if (type.equals("机构")) {
                    text.clear();
                    text.sendKeys(curInstName);
                }
            }
        }
        WebElement searchButton = driver.findElementByXPath("//form[@class=\"searchForm\"]//input[@type=\"submit\"]");
        Actions actions = new Actions(driver);
        actions.click(searchButton).perform();
        ParserUtil.randomSleep(2000);
        List<WebElement> matchResearchers = driver.findElementsByXPath("//div[@id=\"personalSearch_result\"]//div[contains(@class,\"searchResultItem\")]");
        if (matchResearchers.size() == 0) {
            return;
        }
        WebElement target = null;
        String rawInterests = null;
        for (WebElement matchResearcher : matchResearchers) {
            String name = matchResearcher.findElement(By.xpath(".//div[@class=\"searchResult_text\"]//a[@class=\"personName\"]")).getText();
            name = name.replace("\uE61F","");
            String instName = matchResearcher.findElement(By.xpath(".//div[@class=\"searchResult_text\"]//p[contains(@class,\"personInstitution\")]")).getText();
            List<WebElement> interest = matchResearcher.findElements(By.xpath(".//div[@class=\"searchResult_text\"]//p[@class=\"personField\"]//span[@class=\"aFiled\"]"));
            if (name.equals(researcherName) && (curInstName.contains(instName) || instName.contains(curInstName))) {
                if (!interest.isEmpty()) {
                    rawInterests = interest.get(0).getText();
                }
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
        ParserUtil.randomSleep(2000);
        Set<String> allHandles = driver.getWindowHandles();
        if (allHandles.size() == 1)
            return;
        allHandles.remove(originalHandle);
        driver.switchTo().window((String) allHandles.toArray()[0]);
        if (driver.getCurrentUrl().startsWith("https://wappass.baidu.com/")) {
            if (rawInterests != null) {
                this.researcher.setInterests(Arrays.stream(rawInterests.strip().split("\\s+")).toList());
                statusCtrl.researcherRepository.save(this.researcher);
            }
            driver.close();
            driver.switchTo().window(originalHandle);
            banned = true;
            return;
        }

        statusCtrl.changeRunningStatusTo(Thread.currentThread().getName(), "Get info of researcher: " + researcherName);
        List<WebElement> majorElement = driver.findElementsByXPath("//span[@class=\"person_domain person_text\"]//a");
        List<String> interests = new ArrayList<>();
        if (majorElement.size() != 0) {
            for (WebElement major : majorElement) {
                interests.add(major.getText());
            }
            this.researcher.setInterests(interests);
        }
        // H/G指数
        List<WebElement> indexElement = driver.findElementsByXPath("//ul[@class=\"p_ach_wr\"]//li[@class=\"p_ach_item\"]");
        if (indexElement.size() != 0) {
            for (WebElement index:indexElement) {
                String type = index.findElement(By.xpath(".//p[contains(@class,\"p_ach_type\")]")).getAttribute("textContent");
                if (type.equals("H指数") && researcher.getHIndex() == null) {
                    Integer hIndex = Integer.valueOf(index.findElement(By.xpath(".//p[@class=\"p_ach_num\"]")).getAttribute("textContent"));
                    this.researcher.setHIndex(hIndex);
                }
                else if (type.equals("G指数") && researcher.getGIndex() == null) {
                    Integer gIndex = Integer.valueOf(index.findElement(By.xpath(".//p[@class=\"p_ach_num\"]")).getAttribute("textContent"));
                    this.researcher.setGIndex(gIndex);
                }
            }
        }
        List<Researcher.Institution> corInsts = this.researcher.getInstitutions();
        if (corInsts == null) {
            corInsts = new ArrayList<>();
            // 合作机构
            List<WebElement> corInstElement = driver.findElementsByXPath("//div[@class=\"co_affiliate_wr\"]//ul[@class=\"co_affiliate_list\"]//li//span[@class=\"co_paper_name\"]");
            if (corInstElement.size() != 0) {
                for (WebElement inst:corInstElement) {
                    Researcher.Institution corInst = new Researcher.Institution();
                    String instName = inst.getAttribute("title");
                    Institution foundInst = statusCtrl.esUtil.findInstByName(instName);
                    if (foundInst == null) {
                        Institution newInst = new Institution();
                        newInst.setName(instName);
                        statusCtrl.institutionRepository.save(newInst);
                        corInst.setId(newInst.getId());
                        corInst.setName(instName);
                    }
                    else {
                        corInst.setId(foundInst.getId());
                        corInst.setName(instName);
                        corInsts.add(corInst);
                    }
                }
                this.researcher.setInstitutions(corInsts);
            }
        }
        statusCtrl.researcherRepository.save(this.researcher);

        driver.close();
        driver.switchTo().window(originalHandle);
        ParserUtil.randomSleep(2000);
    }
}
