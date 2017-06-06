package com.morningstar.automation.bdd.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import com.morningstar.automation.base.core.configurations.Environment;

public class AbstractPageBase {
	protected WebDriver driver;
	
	public WebDriver getDriver() {
		return driver;
	}

	public AbstractPageBase() {
		this(ContextManager.getCurrentDriver());
	}

	public AbstractPageBase(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, (int) Environment.getTimeOutInSeconds()), this);
	}

	
}
