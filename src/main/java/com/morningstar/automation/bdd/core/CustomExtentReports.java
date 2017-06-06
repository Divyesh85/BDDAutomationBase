package com.morningstar.automation.bdd.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.relevantcodes.extentreports.DisplayOrder;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.NetworkMode;

public class CustomExtentReports extends ExtentReports {

	public CustomExtentReports(String filePath, Boolean replaceExisting, DisplayOrder displayOrder, NetworkMode networkMode, Locale locale) {
		super(filePath, replaceExisting, displayOrder, networkMode, locale);
	}

	public void removeRepeateTests() {
		if (testList == null) {
			return;
		}
		List<String> nameList = new ArrayList<String>();
		Collections.reverse(testList);
		Iterator<ExtentTest> iterator = testList.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next().getTest().getName();
			if (nameList.contains(name)) {
				iterator.remove();
			} else {
				nameList.add(name);
			}
		}
		Collections.reverse(testList);
	}
}
