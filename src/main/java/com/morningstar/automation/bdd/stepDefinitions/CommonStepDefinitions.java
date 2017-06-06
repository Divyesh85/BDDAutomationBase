package com.morningstar.automation.bdd.stepDefinitions;

import com.morningstar.automation.bdd.flow.CommonFlow;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

public class CommonStepDefinitions {

	private CommonFlow actionFlow = new CommonFlow();

	@And("^Click ([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\)$")
	public void clickElement(String name, String how, String using) {
		actionFlow.click(how, using);
	}

	@And("^Click ([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\) and wait request completes$")
	public void clickElementAndWaitUntilAllAjaxRequestCompletes(String name, String how, String using) {
		actionFlow.clickAndWaitUntilAllAjaxRequestCompletes(how, using);
	}
	
	@And("^Wait until all ajax request completes$")
	public void waitUntilAllAjaxRequestCompletes() {
		actionFlow.waitUntilAllAjaxRequestCompletes();
	}

	@And("^Wait for \"([^\"]*)\" seconds$")
	public void wait(double seconds) {
		actionFlow.wait(seconds);
	}

	@And("^Click OK in Alert$")
	public void alertAccept(String how, String using) {
		actionFlow.alertAccept();
	}

	@And("^Click Cancel in Alert$")
	public void alertDismiss(String how, String using) {
		actionFlow.alertDismiss();
	}

	@And("^Move mouse to ([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\)$")
	public void moveToElement(String name, String how, String using) {
		actionFlow.moveToElement(how, using);
	}

	@And("^Press Enter on ([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\)$")
	public void pressEnter(String name, String how, String using) {
		actionFlow.pressEnter(how, using);
	}

	@And("^Press Tab on ([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\)$")
	public void pressTab(String name, String how, String using) {
		actionFlow.pressTab(how, using);
	}

	@And("^Scroll into view for ([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\)$")
	public void scrollIntoView(String name, String how, String using) {
		actionFlow.scrollIntoView(how, using);
	}

	@And("^Select \"([^\"]*)\" in dropdownlist of ([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\)$")
	public void selectByText(String text, String name, String how, String using) {
		actionFlow.selectByText(how, using, text);
	}

	@And("^Input \"([^\"]*)\" in ([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\)$")
	public void sendKeys(String keys, String name, String how, String using) {
		actionFlow.sendKeys(how, using, keys);
	}

	@Then("^([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\) is display$")
	public void verifyElementIsDisplay(String name, String how, String using) {
		actionFlow.verifyElementIsDisplay(how, using);
	}

	@Then("^([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\) is not display$")
	public void verifyElementIsNotDisplay(String name, String how, String using) {
		actionFlow.verifyElementIsNotDisplay(how, using);
	}

	@Then("^([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\) text is \"([^\"]*)\"$")
	public void verifyElementText(String name, String how, String using, String expectedText) {
		actionFlow.verifyElementText(how, using, expectedText);
	}
	
	@Then("^([^\"]*)\\(\"([^\"]*)\"=\"([^\"]*)\"\\) text is not empty$")
	public void verifyElementTextIsNotEmpty(String name, String how, String using) {
		actionFlow.verifyElementTextIsNotEmpty(how, using);
	}
}
