package com.morningstar.automation.bdd.core;

import org.openqa.selenium.WebDriver;

public class TestContext {
	private String methodName;
	private WebDriver webDriver;

	public TestContext() {
		this.methodName = "";
		this.webDriver = null;
	}

	public void setMehtodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMehtodName() {
		return this.methodName;
	}

	public void setWebDriver(WebDriver webDriver) {
		this.webDriver = webDriver;
	}

	public WebDriver getWebDriver() {
		return this.webDriver;
	}
}
