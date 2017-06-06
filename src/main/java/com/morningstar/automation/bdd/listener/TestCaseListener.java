package com.morningstar.automation.bdd.listener;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;

import com.morningstar.automation.base.cons.BaseCons;
import com.morningstar.automation.base.core.listener.BaseTestCaseListener;
import com.morningstar.automation.base.core.utils.Logger;
import com.morningstar.automation.base.core.utils.Util;
import com.morningstar.automation.bdd.core.ContextManager;

public class TestCaseListener extends BaseTestCaseListener {
	private static Logger logger = Logger.getLogger(BaseTestCaseListener.class);
	
	@Override
	public void takeScreenShot(ITestResult result) throws Exception {
		String outputPath = result.getTestContext().getOutputDirectory() + File.separator + BaseCons.SCREENSHOT_FOLDER;
		String browserType = Util.getBrowserType();
		String caseName = result.getName();

		// Add status into snapshot file name
		String status = null;
		switch (result.getStatus()) {
			case 2:
				status = "FAILURE";
				break;
			case 3:
				status = "SKIP";
				break;
			case 4:
				status = "SUCCESS_PERCENTAGE_FAILURE";
				break;
			case 16:
				status = "STARTED";
				break;
			default:
				status = "SUCCESS";
		}

		String fileName = caseName + "-" + browserType + "-" + status + "-" + System.currentTimeMillis() + ".png";
		String filePath = outputPath + File.separator + fileName;
		logger.info("[takeScreenShot]--" + "screen shot = " + filePath);

		try {
			//WebDriver driver = TestObjectManager.getDriver(caseName);
			WebDriver driver = ContextManager.getCurrentDriver();
			logger.info("[takeScreenShot]--" + "screenshot driver= " + driver);

			TakesScreenshot tsDriver;
			if (driver instanceof RemoteWebDriver) {
				WebDriver augmentedDriver = new Augmenter().augment(driver);
				tsDriver = (TakesScreenshot) augmentedDriver;
				logger.info("[takeScreenShot]--screenshot: Remote WebDriver");
			} else {
				tsDriver = (TakesScreenshot) driver;
				logger.info("[takeScreenShot]--screenshot: Local WebDriver");
			}
			File temFile = tsDriver.getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(temFile, new File(filePath));
			logger.info("[takeScreenShot]--screenshot: Success...");
		} catch (Exception ex) {
			logger.error("[takeScreenShot]--Test Case ID:" + result.getName() + "====The stracktrace of this case====");
			ex.printStackTrace();
			logger.info("[takeScreenShot]--screenshot: Failure...");
		}
	}
}

