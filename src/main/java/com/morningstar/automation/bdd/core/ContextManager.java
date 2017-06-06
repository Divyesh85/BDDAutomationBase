package com.morningstar.automation.bdd.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.testng.SkipException;

public class ContextManager {
	private static Map<String, TestContext> contextMap = new ConcurrentHashMap<String, TestContext>();

	private static String getCurrentThreadHashCode() {
		return String.valueOf(Thread.currentThread().hashCode());
	}

	public static TestContext getCurrentTestContext() {
		TestContext testContext = contextMap.get(getCurrentThreadHashCode());
		if (testContext == null) {
			testContext = new TestContext();
			contextMap.put(getCurrentThreadHashCode(), testContext);
		}
		return testContext;
	}

	public static void addCurrentDriver(WebDriver webDriver) {
		getCurrentTestContext().setWebDriver(webDriver);
	}

	public static WebDriver getCurrentDriver() {
		WebDriver webDriver = getCurrentTestContext().getWebDriver();
		if (webDriver == null)
			throw new SkipException("No driver found!");
		return webDriver;
	}

	public static void addCurrentMehodName(String methodName) {
		getCurrentTestContext().setMehtodName(methodName);
	}

	public static String getCurrentMehodName() {
		return getCurrentTestContext().getMehtodName();
	}
}
