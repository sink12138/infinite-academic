package com.buaa.academic.spider.util;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;

public abstract class ParserUtil {
    public static RemoteWebDriver getDriver(Boolean headless) {
        ChromeOptions options = new ChromeOptions().setHeadless(headless);
        RemoteWebDriver driver = null;
        boolean success;
        do {
            try {
                driver = new ChromeDriver(options);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
        } while (!success);
        return driver;
    }

    public static ChromeDriverService getDriverService() throws IOException {
        return ChromeDriverService.createDefaultService();
    }
}
