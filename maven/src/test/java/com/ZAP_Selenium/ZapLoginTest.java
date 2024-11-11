package com.ZAP_Selenium;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.zaproxy.clientapi.core.Alert;

import net.continuumsecurity.proxy.ScanningProxy;
import net.continuumsecurity.proxy.Spider;
import net.continuumsecurity.proxy.ZAProxyScanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.ZAP_Selenium_BrowserDriver.BrowserDriverFactory;

public class ZapLoginTest {

    // Provide details about ZAP Proxy
    static Logger log = Logger.getLogger(ZapLoginTest.class.getName());
    private static final String ZAP_PROXYHOST = "localhost";
    private static final int ZAP_PROXYPORT = 8090;
    private static final String ZAP_APIKEY = null;

    // Provide Chrome driver path
    private static final String BROWSER_DRIVER_PATH = "chromedriver.exe";
    private final static String MEDIUM = "MEDIUM";
    private final static String HIGH = "HIGH";
    private ScanningProxy zapScanner;
    private Spider zapSpider;
    private WebDriver driver;
    private Login login;

    // Provide scan policy names
    private final static String[] policyNames = { "directory-browsing", "cross-site-scripting", "sql-injection",
            "path-traversal", "remote-file-inclusion", "server-side-include", "script-active-scan-rules",
            "server-side-code-injection", "external-redirect", "crlf-injection" };
    int currentScanID;

    // Method to configure ZAP scanner, API client
    @Before
    public void setUp() {
        // Configure ZAP Scanner
        zapScanner = new ZAProxyScanner(ZAP_PROXYHOST, ZAP_PROXYPORT, ZAP_APIKEY);
        // Start new session
        zapScanner.clear();
        log.info("Started a new session: Scanner");
        // Create ZAP API client
        zapSpider = (Spider) zapScanner;
        log.info("Created client to ZAP API");
        // Create driver object
        driver = BrowserDriverFactory.createChromeDriver(createZapProxyConfiguration(), BROWSER_DRIVER_PATH);
        login = new Login(driver);
        driver.get(Login.BASE_URL);
    }

    // Method to close the driver connection
    @After
    public void tearDown() {
        driver.quit();
    }

    private void logAlerts(List<Alert> alerts) {
        for (Alert alert : alerts) {
            log.info("Alert: " + alert.getAlert() + " at URL: " + alert.getUrl() + " Parameter: " + alert.getParam()
                    + " CWE ID: " + alert.getCweId());
        }
    }

    // Method to filter the generated alerts based on Risk and Confidence
    private List<Alert> filterAlerts(List<Alert> alerts) {
        List<Alert> filtered = new ArrayList<Alert>();
        for (Alert alert : alerts) {
            if (alert.getRisk().equals(Alert.Risk.High) && alert.getConfidence() != Alert.Confidence.Low)
                filtered.add(alert);
        }
        return filtered;
    }

    // Method to specify the strength for the ZAP Scanner as High, Medium, or Low
    public void setAlertAndAttackStrength() {
        for (String policyName : policyNames) {
            String ids = enableZapPolicy(policyName);
            for (String id : ids.split(",")) {
                zapScanner.setScannerAlertThreshold(id, MEDIUM);
                zapScanner.setScannerAttackStrength(id, HIGH);
            }
        }
    }

    // Method to execute scan and log the progress
    private void scanWithZap() {
        log.info("Scanning...");
        zapScanner.scan(Login.BASE_URL);
        currentScanID = zapScanner.getLastScannerScanId();
        int complete = 0;
        while (complete < 100) {
            complete = zapScanner.getScanProgress(currentScanID);
            log.info("Scan is " + complete + "% complete.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Scanning done.");
    }

    // Method to configure the ZAP Scanner for specified security policies and
    // enable the scanner
    private String enableZapPolicy(String policyName) {
        String scannerIds = null;
        switch (policyName.toLowerCase()) {
            case "directory-browsing":
                scannerIds = "0";
                break;
            case "cross-site-scripting":
                scannerIds = "40012,40014,40016,40017";
                break;
            case "sql-injection":
                scannerIds = "40018";
                break;
            case "path-traversal":
                scannerIds = "6";
                break;
            case "remote-file-inclusion":
                scannerIds = "7";
                break;
            case "server-side-include":
                scannerIds = "40009";
                break;
            case "script-active-scan-rules":
                scannerIds = "50000";
                break;
            case "server-side-code-injection":
                scannerIds = "90019";
                break;
            case "remote-os-command-injection":
                scannerIds = "90020";
                break;
            case "external-redirect":
                scannerIds = "20019";
                break;
            case "crlf-injection":
                scannerIds = "40003";
                break;
            case "source-code-disclosure":
                scannerIds = "42,10045,20017";
                break;
            case "shell-shock":
                scannerIds = "10048";
                break;
            case "remote-code-execution":
                scannerIds = "20018";
                break;
            case "ldap-injection":
                scannerIds = "40015";
                break;
            case "xpath-injection":
                scannerIds = "90021";
                break;
            case "xml-external-entity":
                scannerIds = "90023";
                break;
            case "padding-oracle":
                scannerIds = "90024";
                break;
            case "el-injection":
                scannerIds = "90025";
                break;
            case "insecure-http-methods":
                scannerIds = "90028";
                break;
            case "parameter-pollution":
                scannerIds = "20014";
                break;
            default:
                throw new RuntimeException("No policy found for: " + policyName);
        }
        if (scannerIds == null)
            throw new RuntimeException("No matching policy found for: " + policyName);
        zapScanner.setEnableScanners(scannerIds, true);
        return scannerIds;
    }

    // Create ZAP proxy by specifying proxy host and proxy port
    private static Proxy createZapProxyConfiguration() {
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(ZAP_PROXYHOST + ":" + ZAP_PROXYPORT);
        proxy.setSslProxy(ZAP_PROXYHOST + ":" + ZAP_PROXYPORT);
        return proxy;
    }

    // Method to configure spider settings, execute ZAP spider, log the progress and
    // found URLs
    private void spiderWithZap() {
        zapSpider.excludeFromSpider(Login.LOGOUT_URL);
        zapSpider.setThreadCount(5);
        zapSpider.setMaxDepth(5);
        zapSpider.setPostForms(false);
        zapSpider.spider(Login.BASE_URL);
        int spiderID = zapSpider.getLastSpiderScanId();
        int complete = 0;
        while (complete < 100) {
            complete = zapSpider.getSpiderProgress(spiderID);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (String url : zapSpider.getSpiderResults(spiderID)) {
            log.info("Found URL: " + url);
        }
    }

    @Test
    public void testSecurityVulnerabilitiesBeforeLogin() throws Exception {
        //login.navigateBeforeLogin();
    	login.loginAsUser();
        // Using ZAP Spider
        log.info("Started spidering");
        spiderWithZap();
        log.info("Ended spidering");

        // Setting alert and attack
        setAlertAndAttackStrength();
        zapScanner.setEnablePassiveScan(true);

        // Using ZAP Scanner
        log.info("Started scanning");
        scanWithZap();
        log.info("Ended scanning");

        // log the found alerts and assert the count of alerts
        //List<Alert> alerts = filterAlerts(zapScanner.getAlerts());
        //logAlerts(alerts);
        //assertThat(alerts.size(), equalTo(0));
    }

//    @Test
//    public void testSecurityVulnerabilitiesAfterLogin() throws Exception {
//        login.loginAsUser();
//        //login.navigateAfterLogin();
//
//        // Using ZAP Spider
//        log.info("Started spidering");
//        spiderWithZap();
//        log.info("Ended spidering");
//
//        // Setting alert and attack
//        setAlertAndAttackStrength();
//        zapScanner.setEnablePassiveScan(true);
//
//        // Using ZAP Scanner
//        log.info("Started scanning");
//        scanWithZap();
//        log.info("Ended scanning");
//
//        // log the found alerts and assert the count of alerts
//        List<Alert> alerts = filterAlerts(zapScanner.getAlerts());
//        logAlerts(alerts);
//        assertThat(alerts.size(), equalTo(0));
//    }
}