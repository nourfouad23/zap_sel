package com.ZAP_Selenium_BrowserDriver;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class BrowserDriverFactory {

    // Make reference variable for WebDriver
    static WebDriver driver;

    public static WebDriver createChromeDriver(Proxy proxy, String path) {
        // Set proxy in the chrome browser
    	
    	ChromeOptions options = new ChromeOptions();

    	// Add the WebDriver proxy capability.
    	
    	proxy.setHttpProxy("myhttpproxy:3337");
    	options.setCapability("proxy", proxy);
    	
//        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
//        capabilities.setCapability("proxy", proxy);

        // Set system property for chrome driver with the path
        System.setProperty("webdriver.chrome.driver", path);
        System.setProperty("webdriver.chrome.whitelistedIps", "");
//        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
//        ChromeOptions options = new ChromeOptions();
//        options.merge(capabilities);
        return new ChromeDriver(options);
    }
}