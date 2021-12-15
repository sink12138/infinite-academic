package com.buaa.academic.spider.util;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.util.Random;

public abstract class ParserUtil {

    private final static Random random = new Random();

    public static RemoteWebDriver getDriver(Boolean headless) {
        ChromeOptions options = new ChromeOptions().setHeadless(headless).addArguments("--blink-settings=imagesEnabled=false");
        RemoteWebDriver driver = null;
        boolean success = false;
        do {
            try {
                driver = new ChromeDriver(options);
                success = true;
            } catch (Exception e) {
                StatusCtrl.errorHandler.report(e);
            }
        } while (!success && !StatusCtrl.jobStopped);
        return driver;
    }

    public static ChromeDriverService getDriverService() throws IOException {
        ChromeDriverService service = null;
        boolean success = false;
        do {
            try {
                service = ChromeDriverService.createDefaultService();
                success = true;
            } catch (Exception e) {
                StatusCtrl.errorHandler.report(e);
            }
        } while (!success && !StatusCtrl.jobStopped);
        return service;
    }

    public static void randomSleep(long millis) throws InterruptedException {
        Thread.sleep(millis / 2 + random.nextLong(millis));
    }

}
