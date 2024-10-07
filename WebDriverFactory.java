package com.testbuddie.core.web.selenium;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.testbuddie.core.listeners.AuraLoadingIndicatorListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;

import com.testbuddie.core.configuration.Configuration;
import com.testbuddie.core.enums.BrowserNames;
import com.testbuddie.core.exceptions.TestBuddieRuntimeException;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.events.EventFiringDecorator;

public class WebDriverFactory {

	private final String downloadFilepath = System.getProperty("user.dir") + File.separator + "src"+ File.separator + "test"+ File.separator + "resources"+ File.separator + "downloads";
	private Map<BrowserNames, String> mapDriverPath = new EnumMap<>(BrowserNames.class);

	WebDriverFactory() {
	}

	public WebDriver getWebDriver() 
	{
		//String executionOnEnvironment = Configuration.getPropertiesValue("Execution");
		String executionOnEnvironment = System.getProperty("Execution", "local");
		WebDriver driver = null;
		switch (executionOnEnvironment.toLowerCase()) {
		case "local":
			driver = loadLocalEnvironment();
			break;

		case "remote":
			driver = loadJenkinsEnvironment();
			break;

		case "localgrid":
			break;

		default:
			throw new TestBuddieRuntimeException("Verify 'Execution' environment mentioned correctly - as core is not able to find defined configuration");
		}

		final Long implicitlyWait = Long.valueOf(Configuration.getPropertiesValue("SELENIUM_IMPLICITLY_WAIT"));
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitlyWait));

		final Long scriptTimeout = Long.valueOf(Configuration.getPropertiesValue("SELENIUM_SCRIPT_TIMEOUT"));
		driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(scriptTimeout));

		final Long loadTimeout = Long.valueOf(Configuration.getPropertiesValue("SELENIUM_LOAD_TIMEOUT"));
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(loadTimeout));

		if(Configuration.getpropertiesBooleanValue("SetLoadingIndicatorListener")) {
			AuraLoadingIndicatorListener loaderListener = new AuraLoadingIndicatorListener(driver);
			WebDriver decoratedDriver = new EventFiringDecorator(loaderListener).decorate(driver);
			driver = decoratedDriver;
		}

		return driver;
	}

	public WebDriver loadLocalEnvironment() {
		WebDriver driver = null;
		this.setLocalDefaults();
		final String browser = Configuration.getPropertiesValue("BrowserType");
		switch (BrowserNames.valueOf(browser)) {
		case Firefox:
			driver = new FirefoxDriver(this.getLocalFirefoxOptions());
			break;

		case Chrome:
			driver = new ChromeDriver(getLocalChromeOptions());
			break;

		case InternetExplorer:
			return new InternetExplorerDriver(this.getLocalInternetExplorerOptions());

		case Edge:
			return new EdgeDriver(this.getLocalEdgeOptions());

		default:
			driver = new ChromeDriver(getLocalChromeOptions());
			break;
		}

		return driver;
	}

	public WebDriver loadJenkinsEnvironment() {
		WebDriver driver = null;
		final String browser = Configuration.getPropertiesValue("BrowserType");
		switch (browser.toLowerCase()) {
		case "firefox":
			WebDriverManager.firefoxdriver().setup();
			driver = new FirefoxDriver(this.getJenkinsFirefoxOptions());
			break;

		case "chrome":
			driver = new ChromeDriver(getJenkinsChromeOptions());;
			break;

		default:
			WebDriverManager.chromedriver().setup();
			driver = new ChromeDriver(getJenkinsChromeOptions());;
			break;
		}

		return driver;
	}


	private ChromeOptions getLocalChromeOptions() {
		System.setProperty("webdriver.chrome.driver", mapDriverPath.get(BrowserNames.Chrome));

		ChromeOptions options = new ChromeOptions();
		HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
		chromePrefs.put("profile.default_content_settings.popups", 0);
		chromePrefs.put("download.default_directory", downloadFilepath);
		options.setExperimentalOption("prefs", chromePrefs);
		options.addArguments("--no-sandbox"); // Bypass OS security
	 	options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
		options.addArguments("--disable-gpu");
		options.addArguments("--lang=en");
		options.addArguments("--remote-allow-origins=*");//this should be removed later as it is for known chromedriver issue with version 111.x.xxxx.xx, remove when browser is upgraded to next version 112
  		options.addArguments("--disable-notifications");
		options.addArguments("--auth-server-whitelist=*.eulerhermes.io");
		options.addArguments("--auth-negotiate-delegate-whitelist=*.eulerhermes.io");
		return options;
	}

	private FirefoxOptions getLocalFirefoxOptions() {
		System.setProperty("webdriver.gecko.driver", mapDriverPath.get(BrowserNames.Firefox));
		FirefoxOptions firefoxOptions = new FirefoxOptions();
		firefoxOptions.addPreference("javascript.enabled", true);
		firefoxOptions.addPreference("intl.accept_languages", "en");
		firefoxOptions.setCapability("marionette", true);
		firefoxOptions.addPreference("browser.download.folderList", 2);
		firefoxOptions.addPreference("browser.download.dir", downloadFilepath);
		firefoxOptions.addPreference("browser.helperApps.neverAsk.saveToDisk","text/csv/xlsx/pdf");
		firefoxOptions.addPreference("browser.download.manager.showWhenStarting", false);
		return firefoxOptions;
	}

	private InternetExplorerOptions getLocalInternetExplorerOptions(){ 
		System.setProperty("webdriver.ie.driver", mapDriverPath.get(BrowserNames.InternetExplorer));
		InternetExplorerOptions internetExplorerOptions = new InternetExplorerOptions();
		return internetExplorerOptions;
	}

	private EdgeOptions getLocalEdgeOptions() {
		System.setProperty("webdriver.edge.driver", mapDriverPath.get(BrowserNames.Edge));
		HashMap<String, Object> edgePrefs = new HashMap<String, Object>();
		edgePrefs.put("profile.default_content_settings.popups", 0);
		edgePrefs.put("profile.default_content_setting_values.notifications", 2);
		edgePrefs.put("profile.default_content_setting_values.automatic_downloads", 1);
		edgePrefs.put("profile.content_settings.pattern_pairs.*,*.multiple-automatic-downloads", 1);
		edgePrefs.put("download.default_directory", downloadFilepath);
		EdgeOptions egdeOptions = new EdgeOptions();
	 	egdeOptions.setExperimentalOption("prefs", edgePrefs);
		egdeOptions.setExperimentalOption("useAutomationExtension", false); 
		egdeOptions.setExperimentalOption("excludeSwitches",Collections.singletonList("enable-automation"));
		return egdeOptions;
	}

	private ChromeOptions getJenkinsChromeOptions() {
		ChromeOptions options = new ChromeOptions();
		HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
		chromePrefs.put("download.default_directory", downloadFilepath);
	 	options.setExperimentalOption("prefs", chromePrefs);
		options.addArguments("--headless","--disable-dev-shm-usage", "--verbose", "--disable-web-security", "--ignore-certificate-errors", "--allow-running-insecure-content", "--allow-insecure-localhost",
				"--no-sandbox", "--disable-gpu", "--incognito", "start-maximized");
		return options;
	}

	private FirefoxOptions getJenkinsFirefoxOptions() {
		FirefoxOptions firefoxOptions = new FirefoxOptions();
		//firefoxOptions.setHeadless(true);
		firefoxOptions.addPreference("browser.download.dir", downloadFilepath);
		return firefoxOptions;
	}
 
	private void setLocalDefaults(){
		mapDriverPath.put(BrowserNames.Chrome, "C:\\Webdrivers\\chromedriver.exe");
		mapDriverPath.put(BrowserNames.Firefox, "C:\\Webdrivers\\geckodriver.exe");
		mapDriverPath.put(BrowserNames.InternetExplorer, "C:\\Webdrivers\\IEDriverServer.exe");
		mapDriverPath.put(BrowserNames.Edge, "C:\\Webdrivers\\msedgedriver.exe");
	}

}
