package com.morningstar.automation.bdd.report;

import java.io.File;

import com.morningstar.automation.bdd.core.ExtentCucumberFormatter;

public class CucumberReportManager {
	private static boolean setUp = false;
	private static boolean setUpByClass = false;
	private static String reportFolder = "cucumber";
	private static String reportName = "report.html";
	private static String reportFolderPath;
	private static String reportFilePath;

	static {
		reportFolderPath = "output" + File.separator + reportFolder;
		reportFilePath = reportFolderPath + File.separator + reportName;
	}

	public static void setUp() {
		if (!setUp) {
			ExtentCucumberFormatter.initiateExtentCucumberFormatter(new File(reportFilePath), true);
			setUp = true;
		}
	}

	public static void finish() {
		if (setUp) {
			ExtentCucumberFormatter.finish();
			setUp = false;
		}
	}

	public static void setUpByClass() {
		if (!setUp && !setUpByClass) {
			ExtentCucumberFormatter.initiateExtentCucumberFormatter(new File(reportFilePath), true);
			setUp = true;
			setUpByClass = true;
		}
	}

	public static void finishByClass() {
		if (setUp && setUpByClass) {
			ExtentCucumberFormatter.finish();
			setUp = false;
			setUpByClass = false;
		}
	}

	public static String getReportFolder() {
		return reportFolder;
	}

	public static String getReportName() {
		return reportName;
	}

	public static String getReportFolderPath() {
		return reportFolderPath;
	}

	public static String getReportFilePath() {
		return reportFilePath;
	}

}
