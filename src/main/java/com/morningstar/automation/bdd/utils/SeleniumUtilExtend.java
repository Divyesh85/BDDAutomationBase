package com.morningstar.automation.bdd.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.JavascriptExecutor;

import com.google.common.base.Function;
import com.morningstar.automation.base.core.utils.SeleniumUtil;

public class SeleniumUtilExtend {
	
	public static WebElement getElementByText(List<WebElement> eleList, String name) {
		WebElement result = null;
		for (WebElement ele : eleList) {
			if (name.equals(ele.getText())) {
				result = ele;
				break;
			}
		}
		return result;
	}
	
	public static WebElement getElementByTextIgnoreCase(List<WebElement> eleList, String name) {
		WebElement result = null;
		for (WebElement ele : eleList) {
			if (name.equalsIgnoreCase(ele.getText())) {
				result = ele;
				break;
			}
		}
		return result;
	}
	
	public static WebElement getElementByAttributeValue(List<WebElement> eleList, String attributeName, String attributeValue) {
		WebElement result = null;
		for (WebElement item : eleList) {
			if (attributeValue.equals(item.getAttribute(attributeName))) {
				result = item;
				break;
			}
		}
		return result;
	}
	
	public static WebElement getElementByAttributeValueIgnoreCase(List<WebElement> eleList, String attributeName, String attributeValue) {
		WebElement result = null;
		for (WebElement item : eleList) {
			if (attributeValue.equalsIgnoreCase(item.getAttribute(attributeName))) {
				result = item;
				break;
			}
		}
		return result;
	}

	public static WebElement getDisplayElement(List<WebElement> eleList) {
		for (WebElement ele : eleList) {
			if (ele.isDisplayed()) {
				return ele;
			}
		}
		return null;
	}
	
	public static List<WebElement> getAllVisibleElements(List<WebElement> elements) {
		List<WebElement> result = new ArrayList<WebElement>();
		for (WebElement element : elements) {
			if (element.isDisplayed())
				result.add(element);
		}
		return result;
	}
	
	public static void waitUntilAllAjaxRequestCompletes(WebDriver driver) {
		SeleniumUtil.sleep(1);
		SeleniumUtil.waitUntilAllAjaxRequestCompletes(driver);
		SeleniumUtil.sleep(1);
	}
	
	public static WebElement getNextBrotherElement(WebElement element) {
		return element.findElement(By.xpath("following-sibling::*[1]"));
	}
	
	public static void moveToElementLeftOutOfItToClick(WebDriver driver, WebElement outOfElement) {
		Actions action = new Actions(driver);
		action.moveByOffset(-outOfElement.getSize().getWidth(), 0).click().perform();
	}
	
	public static void moveMouseToElement(WebDriver driver, WebElement element, int xOffset, int yOffset) {
		Actions action = new Actions(driver);
		action.moveToElement(element, xOffset, yOffset).perform();
	}
	
	public static void waitForElementAttributeToBeChanged(WebDriver driver, final By locator, final String attrName, final String oldValue) {
		Function<WebDriver, Boolean> waitFn = new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return !driver.findElement(locator).getAttribute(attrName).equals(oldValue);
				} catch (Exception e) {

				}
				return false;
			}
		};
		SeleniumUtil.createWait(driver).withMessage("The " + attrName + " value of this element:" + locator.toString() + " is not changed.").until(waitFn);
	}
	
	public static boolean isDisplayed(WebElement element) {
		try {
			return element.isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}
	
	public static void selectByText(WebElement select, String text) {
		Select s = new Select(select);
		s.selectByVisibleText(text);
	}
	
	public static String scroll(int scrollLength, WebDriver driver, WebElement e) {
		SeleniumUtil.sleep(1);
		String code = "var obj = arguments[0];if((obj.scrollTop+" + scrollLength + ")<obj.scrollHeight){obj.scrollTop = obj.scrollTop + " + scrollLength + ";}else{obj.scrollTop = obj.scrollHeight;}";
		return (String) ((JavascriptExecutor) driver).executeScript(code, e);
	}

	public static boolean isScrollToBottom(WebDriver driver, WebElement e) {
		SeleniumUtil.sleep(1);
		String code = "var obj = arguments[0];if(obj.scrollTop == obj.scrollHeight) return true;else return false;";
		return (boolean) ((JavascriptExecutor) driver).executeScript(code, e);
	}
	
	public static String switchToWindow(WebDriver driver) {
		Set<String> list = driver.getWindowHandles();
		String pageHandle = driver.getWindowHandle();
		for (String handle : list) {
			if (!handle.equals(pageHandle)) {
				driver.switchTo().window(handle);
				driver.manage().window().maximize();
				break;
			}
		}
		return pageHandle;
	}
	
	public static void swithToWindowByIndex(WebDriver driver, int index) {
		String[] handles = new String[driver.getWindowHandles().size()];
		driver.getWindowHandles().toArray(handles);
		driver.switchTo().window(handles[index]);
	}
	
	public static void swithToWindowByTitle(WebDriver driver,String title){
		String[] handles = new String[driver.getWindowHandles().size()];
		driver.getWindowHandles().toArray(handles);
		for(String handle : handles){
			driver.switchTo().window(handle);
			if(driver.getTitle().contains(title)){
				break;
			}
		}
	}
	
	public static WebElement tryToFindElement(WebDriver driver, By by){
		try {
			return driver.findElement(by);
		} catch (Exception e) {
			return null;
		}
	}
}
