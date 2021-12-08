package com.buaa.academic.spider.util;


import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Researcher;
import io.swagger.models.auth.In;
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

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResearcherParser {
    private String url;
    private Researcher researcher;

    public void wanFangSpider() throws InterruptedException {
        ChromeOptions options=new ChromeOptions().setHeadless(true);
        RemoteWebDriver driver=new ChromeDriver(options);
        driver.get(this.url);
        Thread.sleep(2000);
        Researcher researcher=new Researcher();
        // 获取作者姓名
        List<WebElement> nameElement=driver.findElementsByXPath("//h3[@class=\"lt-top-tilte scholar-name-show no-description\"]");
        if(nameElement.size()!=0){
            String name=nameElement.get(0).getText();
            researcher.setName(name);
            System.out.println("作者姓名： "+name);
        }
        // 获取当前机构
        List<WebElement> curInstElement=driver.findElementsByXPath("//h3[@class=\"lt-top-tilte unit-name \"]");
        if(curInstElement.size()!=0){
            String curInst=curInstElement.get(0).getText();
            System.out.println("当前机构： "+curInst);
            Researcher.Institution curInstitution=new Researcher.Institution();
            Institution institution=new Institution();
            //todo get inst by name.
            if(institution==null){
                institution.setName(curInst);
                //todo add into database
            }
            curInstitution.setId(institution.getId());
            curInstitution.setName(curInst);
            researcher.setCurrentInst(curInstitution);
        }
        // 获取科研人员的H、G指数
        List<WebElement> scholarIndexElement=driver.findElementsByXPath("//ul[@class=\"scholar-index\"]//li");
        if(scholarIndexElement.size()!=0){
            for(WebElement scholarIndex:scholarIndexElement){
                List<WebElement> indexElement=scholarIndex.findElements(By.xpath(".//p"));
                if(indexElement.size()==0){
                    continue;
                }
                String scholar=indexElement.get(1).getText();
                System.out.println(scholar);
                if(scholar.equals("H指数")){
                    Integer hIndex= Integer.valueOf(scholarIndex.findElement(By.xpath(".//p[@class=\"index-value\"]")).getText());
                    researcher.setHIndex(hIndex);
                    System.out.println("H指数： "+hIndex);
                }
                else if(scholar.equals("G指数")){
                    Integer gIndex= Integer.valueOf(scholarIndex.findElement(By.xpath(".//p[@class=\"index-value\"]")).getText());
                    researcher.setGIndex(gIndex);
                    System.out.println("G指数： "+gIndex);
                }
            }
        }
        // 获取科研人员成果信息
        List<WebElement> citationElement=driver.findElementsByXPath("//div[@class=\"lt-num-text\"]");
        if(citationElement.size()!=0){
            for(WebElement cit:citationElement){
                String citName=cit.findElement(By.xpath(".//p")).getText();
                System.out.println(citName);
                if(citName.equals("总文献量")){
                    int paperNum= Integer.parseInt(cit.findElement(By.xpath(".//span")).getText());
                    researcher.setPaperNum(paperNum);
                    System.out.println("总文献量： "+paperNum);
                }
                else if(citName.equals("总被引量")){
                    int citationNum= Integer.parseInt(cit.findElement(By.xpath(".//span")).getText());
                    researcher.setCitationNum(citationNum);
                    System.out.println("总被引量： "+citationNum);
                }
            }
        }
        // 查看是否有“更多”按钮
        List<WebElement> moreElement=driver.findElementsByXPath("//div[@class=\"rt-bottom\"]//div[@class=\"more-wrapper\" and @style=\"display: block;\"]//a[@class=\"show-more\"]");
        if(moreElement.size()!=0){
            WebElement morePoint=moreElement.get(0);
            Actions actions=new Actions(driver);
            actions.click(morePoint).perform();
            Thread.sleep(1000);
        }
        // 获取合作机构
        List<WebElement> instElement=driver.findElementsByXPath("//div[@class=\"bottom-list\"]");
        if(instElement.size()!=0){
            List<Researcher.Institution> corInstitutions=new ArrayList<>();
            for(WebElement inst:instElement){
                String corInst=inst.findElement(By.xpath(".//p[@class=\"list-title\"]")).getText();
                System.out.println("合作机构： "+corInst);
                Institution institution=new Institution();
                //todo find inst by name.
                if(institution==null){
                    institution.setName(corInst);
                    //todo add into database
                }
                Researcher.Institution corInstitution=new Researcher.Institution();
                corInstitution.setId(institution.getId());
                corInstitution.setName(corInst);
                corInstitutions.add(corInstitution);
            }
            researcher.setInstitutions(corInstitutions);
        }
        //todo add researcher into database
        driver.close();
        //todo 到其他网站查询作者研究方向
    }

    // todo 获取作者研究领域
    // https://xueshu.baidu.com/usercenter/data/authorchannel?cmd=inject_page
    public void baiDuSpider() throws InterruptedException{

    }
}
