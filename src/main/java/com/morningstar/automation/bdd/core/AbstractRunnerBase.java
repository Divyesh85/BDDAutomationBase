package com.morningstar.automation.bdd.core;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import com.morningstar.automation.base.core.AbstractTest;
import com.morningstar.automation.base.core.annotation.MorningstarAutomationAnnotation;
import com.morningstar.automation.base.core.beans.UserBean;
import com.morningstar.automation.base.core.configurations.Environment;
import com.morningstar.automation.base.core.users.UserManager;
import com.morningstar.automation.base.core.utils.Logger;
import com.morningstar.automation.base.core.utils.SeleniumUtil;
import com.morningstar.automation.base.core.utils.TestObjectManager;
import com.morningstar.automation.base.core.utils.Util;
import com.morningstar.automation.bdd.report.CucumberReportManager;
import com.morningstar.automation.bdd.utils.UtilExtend;

import cucumber.api.testng.CucumberFeatureWrapper;

public abstract class AbstractRunnerBase {
	private static Logger logger = Logger.getLogger(AbstractTest.class);
	protected static final String DataProviderMethod = "features";
	protected Map<String, TestNGCucumberRunner> testNGCucumberRunnerMap;

	public AbstractRunnerBase() {
		testNGCucumberRunnerMap = new ConcurrentHashMap<String, TestNGCucumberRunner>();
	}

	public void feature(CucumberFeatureWrapper cucumberFeature, Method method) {
		testNGCucumberRunnerMap.get(method.getName()).runCucumber(cucumberFeature.getCucumberFeature());
	}

	@DataProvider
	public Object[][] features(Method method) {
		TestNGCucumberRunner testNGCucumberRunner;
		if (testNGCucumberRunnerMap.containsKey(method.getName())) {
			testNGCucumberRunner = testNGCucumberRunnerMap.get(method.getName());
		} else {
			testNGCucumberRunner = new TestNGCucumberRunner(this.getClass(), method);
			testNGCucumberRunnerMap.put(method.getName(), testNGCucumberRunner);
		}
		return new Object[][] { new Object[] { testNGCucumberRunner.provideFeatures()[0][0], method } };
	}

	@BeforeClass(alwaysRun = true)
	public void setUpClass() {
		CucumberReportManager.setUpByClass();
	}

	@AfterClass
	public void tearDownClass() throws Exception {
		for (TestNGCucumberRunner testNGCucumberRunner : testNGCucumberRunnerMap.values()) {
			testNGCucumberRunner.finish();
		}
		CucumberReportManager.finishByClass();
	}

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod(Method method) {
		try {
			ContextManager.addCurrentMehodName(method.getName());
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====BaseBeforeMethod start");
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====current env: " + Util.getEnvStr());
			
			//Get user type
			String userType = null;
			MorningstarAutomationAnnotation annotation = method.getAnnotation(MorningstarAutomationAnnotation.class);
			if (annotation != null) {
				userType = annotation.userType();
			}
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====Get user type successfully.");

			//Get user by type
			UserBean user = UserManager.getUser(userType);
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====Get the user you want successfully.");

			//Add user into TestObjectManger
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====Add the user into TestObjectManager...");
			TestObjectManager.addUser(method.getName(), user);
			logger.info("[SetUp]Add test case ID: " + method.getName() + ", user: "+TestObjectManager.getUser(method.getName())+" into TestObjectManager successfully.");

			// Get browser info.
			String browserType = Util.getBrowserType();
			String browserVersion = Util.getBrowserVersion();
			String platform = Util.getPlatform();
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====browserType: " + browserType + ", verison: " + browserVersion + ", platform: " + platform);

			// Create driver by type
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====Creating driver...");
			WebDriver driver = SeleniumUtil.createDriver(browserType, browserVersion, platform);
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====Create driver successfully.");

			//logger.info("[SetUp]Test Case ID:" + method.getName() + "====Add driver into TestObjectManager...");
			ContextManager.addCurrentDriver(driver);
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====Add driver successfully.");

			// log node to help to debug on Grid
			if (Util.isUseGrid()) {
				logger.info("[SetUp]Test Case ID:" + method.getName() + "===current node: " + UtilExtend.GetNodeName(ContextManager.getCurrentDriver()));
			}

			String owner = this.getOwner(method);
			TestObjectManager.addOwner(method.getName(), owner);
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====Add owner into TestObjectManager.");

			// Maximize browser window
			driver.manage().window().maximize();
			String homeURL = Environment.getHomePageUrl();
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====Navigating to URL: " + homeURL);
			driver.get(homeURL);

			// if the URL is failed to forward, refresh the browser again
			while (driver.getCurrentUrl().contains("undefined")) {
				logger.info("[SetUp]Test Case ID:" + method.getName() + "====Re-Navigating to URL: " + homeURL);
				driver.get(homeURL);
			}

			logger.info("[SetUp]Test Case ID:" + method.getName() + "====Navigate to URL: " + homeURL + " sucessfully.");
			logger.info("[SetUp]Test Case ID:" + method.getName() + "====BaseBeforeMethod end");
		} catch (Exception ex) {
			logger.error("[SetUp]-{BaseBeforeMethod}-Test Case ID:" + method.getName() + "====" + ex.getMessage());
			logger.error("[SetUp]-{BaseBeforeMethod}-Test Case ID:" + method.getName() + "====The stracktrace of this case====");
			ex.printStackTrace();
			Assert.fail("[SetUp]-{BaseBeforeMethod}-Test Case ID:" + method.getName() + "====ERROR:Fail to SetUp");
		}
	}

	@AfterMethod(alwaysRun = true)
	public void afterMethod(Method method) {
		logger.info("[TearDown]Test Case ID:" + method.getName() + "====BaseAfterMethod start");
		try {
			WebDriver driver = ContextManager.getCurrentDriver();
			logger.info("[TearDown]Test Case ID:" + method.getName() + "====URL: " + driver.getCurrentUrl());

			// Close windows one by one
			Set<String> handles = driver.getWindowHandles();
			for (int i = handles.size() - 1; i >= 0; i--) {
				driver.switchTo().window(handles.toArray()[i].toString());
				driver.close();
			}
			driver.quit();

			logger.info("[TearDown]Test Case ID:" + method.getName() + "====All browsers quit successfully on Windows.");
		} catch (Exception ex) {
			logger.error("[TearDown]-{BaseAfterMethod}-Test Case ID:" + method.getName() + "====" + ex.getMessage());
			logger.error("[SetUp]-{BaseAfterMethod}-Test Case ID:" + method.getName() + "====The stracktrace of this case====");
			ex.printStackTrace();
			logger.error("[TearDown]-{BaseAfterMethod}-Test Case ID:" + method.getName() + "====ERROR:Fail to TearDown");
		}

		logger.info("[TearDown]Test Case ID:" + method.getName() + "====BaseAfterMethod end");
	}

	public String getOwner(Method method) {
		String result = "";
		MorningstarAutomationAnnotation annotation = method.getAnnotation(MorningstarAutomationAnnotation.class);
		if (annotation != null) {
			result = annotation.owner();
		}
		return result == null ? "" : result;
	}
}
