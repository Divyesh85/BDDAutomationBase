package com.morningstar.automation.bdd.core;

import org.openqa.selenium.WebDriver;

public class AbstractFlowBase {
	protected WebDriver driver;
	
	public WebDriver getDriver() {
		return driver;
	}
	
	public AbstractFlowBase() {
		this(ContextManager.getCurrentDriver());
	}
	
	public AbstractFlowBase(WebDriver driver) {
		this.driver = driver;
	}
}
