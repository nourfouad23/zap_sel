package com.ZAP_Selenium;

import java.awt.Robot;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import com.ZAP_Selenium_ReadObjectRepository.Locator;

public class Login {

    WebDriver driver;
    // Main Directory of the project
    String currentDir = System.getProperty("user.dir");
    // Global locator file
    Locator locator = new Locator(currentDir + "//objectRepository.properties");

    final static String BASE_URL = "http://localhost:3000/#/login";
    final static String LOGOUT_URL = "Enter your logout url";
    final static String USERNAME = "nour.fouad@qestit.com";
    final static String PASSWORD = "Qestit@123";

    public Login(WebDriver driver) {
        this.driver = driver;
        this.driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        this.driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    // navigation before login
    public void navigateBeforeLogin() throws Exception {
//        driver.findElement(locator.getLocator("challenges")).click();
    }

    // login
    public void loginAsUser() throws Exception {
    	Actions actions = new Actions(driver);

    	Robot robot = new Robot();

    	robot.mouseMove(50,50);

    	actions.click().build().perform();
    	  driver.findElement(By.id("email")).sendKeys(USERNAME);
    	  driver.findElement(By.id("password")).sendKeys(PASSWORD);
    	  driver.findElement(By.id("loginButton")).click();
    	  driver.quit();
//        driver.findElement(locator.getLocator("login")).click();
//        driver.findElement(locator.getLocator("username_field")).sendKeys(USERNAME);
//        driver.findElement(locator.getLocator("password_field")).sendKeys(PASSWORD);
//        driver.findElement(locator.getLocator("login_button")).click();
//        verifyPresenceOfText("Successfully logged in");
    }

    // navigation after login
    public void navigateAfterLogin() throws Exception {
    	driver.quit();
        //driver.findElement(locator.getLocator("scoreboard")).click();
    }

    //Verify the page title must contain expected text
    public void verifyPresenceOfText(String text) {
        String pageSource = this.driver.getPageSource();
        if (!pageSource.contains(text))
            throw new RuntimeException("Expected text: [" + text + "] was not found.");
    }
}
