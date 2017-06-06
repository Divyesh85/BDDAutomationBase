package com.morningstar.automation.bdd.flow;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.google.common.base.Function;
import com.morningstar.automation.base.core.utils.SeleniumUtil;
import com.morningstar.automation.bdd.core.AbstractFlowBase;

public class CommonFlow extends AbstractFlowBase {

	private By getBy(String how, String using) {
		if (how.equals("id")) {
			return By.id(using);
		} else if (how.equals("css")) {
			return By.cssSelector(using);
		} else if (how.equals("xpath")) {
			return By.xpath(using);
		} else if (how.equals("tagName")) {
			return By.tagName(using);
		} else if (how.equals("linkText")) {
			return By.linkText(using);
		} else if (how.equals("name")) {
			return By.name(using);
		} else if (how.equals("className")) {
			return By.className(using);
		} else if (how.equals("partialLinkText")) {
			return By.partialLinkText(using);
		}
		return null;
	}

	public void click(String how, String using) {
		SeleniumUtil.waitForElementVisible(driver, getBy(how, using)).click();
		SeleniumUtil.sleep(1);
	}

	public void clickAndWaitUntilAllAjaxRequestCompletes(String how, String using) {
		SeleniumUtil.waitForElementVisible(driver, getBy(how, using)).click();
		SeleniumUtil.sleep(1);
		SeleniumUtil.waitUntilAllAjaxRequestCompletes(driver);
		SeleniumUtil.sleep(1);
	}

	public void wait(double seconds) {
		SeleniumUtil.sleep(seconds);
	}

	public void waitUntilAllAjaxRequestCompletes() {
		SeleniumUtil.waitUntilAllAjaxRequestCompletes(driver);
	}

	public void sendKeys(String how, String using, String keys) {
		WebElement element = SeleniumUtil.waitForElementVisible(driver, getBy(how, using));
		element.clear();
		element.sendKeys(keys);
	}

	public void selectByText(String how, String using, String text) {
		Select select = new Select(SeleniumUtil.waitForElementVisible(driver, getBy(how, using)));
		select.selectByVisibleText(text);
	}

	public void scrollIntoView(String how, String using) {
		WebElement element = SeleniumUtil.waitForElementPresent(driver, getBy(how, using));
		SeleniumUtil.scrollIntoView(driver, element);
	}

	public void pressTab(String how, String using) {
		SeleniumUtil.waitForElementVisible(driver, getBy(how, using)).sendKeys(Keys.TAB);
	}

	public void pressEnter(String how, String using) {
		SeleniumUtil.waitForElementVisible(driver, getBy(how, using)).sendKeys(Keys.ENTER);
	}

	public void moveToElement(String how, String using) {
		WebElement element = SeleniumUtil.waitForElementVisible(driver, getBy(how, using));
		Actions actions = new Actions(driver);
		actions.moveToElement(element).perform();
	}

	private void waitAlertVisible() {
		Function<WebDriver, Boolean> waitFn = new Function<WebDriver, Boolean>() {
			public Boolean apply(WebDriver driver) {
				return (Boolean) SeleniumUtil.isAlertPresent(driver);
			}
		};

		WebDriverWait wait = SeleniumUtil.createWait(driver);
		wait.withMessage("This is no alert appear!");
		wait.until(waitFn);
	}

	public void alertAccept() {
		this.waitAlertVisible();
		driver.switchTo().alert().accept();
	}

	public void alertDismiss() {
		this.waitAlertVisible();
		driver.switchTo().alert().dismiss();
	}

	public void verifyElementIsDisplay(String how, String using) {
		SeleniumUtil.waitForElementVisible(driver, getBy(how, using));
	}

	public void verifyElementIsNotDisplay(String how, String using) {
		SeleniumUtil.waitForElementNotVisible(driver, getBy(how, using));
	}

	public void verifyElementText(String how, String using, String expectedText) {
		WebElement element = SeleniumUtil.waitForElementVisible(driver, getBy(how, using));
		Assert.assertEquals(element.getText().trim(), expectedText, "Element text is not \"" + expectedText + "\"");
	}

	public void verifyElementTextIsNotEmpty(String how, String using) {
		WebElement element = SeleniumUtil.waitForElementVisible(driver, getBy(how, using));
		if (StringUtils.isEmpty(element.getText().trim())) {
			Assert.fail("Text is empty!");
		}
	}
}
