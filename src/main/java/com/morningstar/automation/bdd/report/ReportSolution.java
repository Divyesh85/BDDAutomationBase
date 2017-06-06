package com.morningstar.automation.bdd.report;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;

import com.morningstar.automation.base.cons.BaseCons;
import com.morningstar.automation.base.core.beans.MailSenderInfo;
import com.morningstar.automation.base.core.beans.ReportBean;
import com.morningstar.automation.base.core.beans.ResultStatusEnum;
import com.morningstar.automation.base.core.beans.TestCaseResultBean;
import com.morningstar.automation.base.core.configurations.Environment;
import com.morningstar.automation.base.core.report.PdfReport;
import com.morningstar.automation.base.core.utils.EmailUtil;
import com.morningstar.automation.base.core.utils.FtpUploadUtil;
import com.morningstar.automation.base.core.utils.Logger;
import com.morningstar.automation.base.core.utils.TestObjectManager;
import com.morningstar.automation.base.core.utils.Util;
import com.morningstar.automation.base.core.utils.XmlUtil;


public class ReportSolution {

	private static final Logger logger = Logger.getLogger(ReportSolution.class);
	private ReportBean reportBean;
	private ISuite suite;
	private Date startDate;
	private Date endDate;
	private String rndNum;

	public ReportSolution(ISuite suite, Date startDate, Date endDate) {
		this.suite = suite;
		this.startDate = startDate;
		this.endDate = endDate;
		initDestPath();
		this.rndNum = Integer.toString((new Random()).nextInt(1000));
	}

	private String getExcutiontimeStr() {
		String result = "";
		long distance = endDate.getTime() - startDate.getTime();
		long min = distance / (1000 * 60);
		if (min > 0) {
			result = min + "  min  ";
		}

		long s = (distance - min * 60 * 1000) / 1000;
		if (s > 0) {
			result += s + "  s";
		}

		if (result.isEmpty()) {
			result = "0 s";
		}

		return result;
	}

	public ReportBean getReportData() {
		if (reportBean == null) {
			String suiteName = suite.getName();
			logger.info("suiteName=" + suiteName);
			String outputPath = suite.getOutputDirectory();
			logger.info("outputPath=" + outputPath);

			String browser = Util.getBrowserType();
			String browserVersion = Util.getBrowserVersion();
			if (browserVersion != null && !browserVersion.isEmpty()) {
				browser += browserVersion;
			}

			String executionTime = getExcutiontimeStr();
			String url = Environment.getHomePageUrl();
			String environment = Environment.getEnvironmentBean().getType();
			String teamName = Environment.getTeamName();

			reportBean = new ReportBean();
			reportBean.setBrowser(browser);
			reportBean.setEnvironment(environment);
			reportBean.setExecutionTime(executionTime);
			reportBean.setSuite(suiteName);
			reportBean.setUrl(url);
			reportBean.setTeam(teamName);
		}

		return reportBean;
	}

	private List<String> getResultFileList() {
		List<String> resultFileList = new ArrayList<String>();
		Map<String, ISuiteResult> suiteResults = suite.getResults();
		Set<String> keySet = suiteResults.keySet();
		for (String key : keySet) {
			ISuiteResult sr = suiteResults.get(key);
			ITestContext tc = sr.getTestContext();
			resultFileList.add(tc.getName());
		}
		return resultFileList;
	}

	public void backupReport() {
		String targetFTPFolder = getFtpFolderName();
		FtpUploadUtil.upload(Environment.getTeamName(), targetFTPFolder, suite.getOutputDirectory());
		FtpUploadUtil.upload(Environment.getTeamName(), targetFTPFolder, CucumberReportManager.getReportFolderPath());
	}
	
	public void backupCucumberReport(String targetReportFolderPath) {
		String targetFTPFolder = getFtpFolderName();
		FtpUploadUtil.upload(Environment.getTeamName(), targetFTPFolder, targetReportFolderPath);
	}

	public void generateReport() {
		List<String> resultFileList = getResultFileList();
		Document allDoc = DocumentHelper.createDocument();
		Element rootEl = allDoc.addElement("testsuites");
		for (String fileName : resultFileList) {
			try {
				String filePath = suite.getOutputDirectory() + File.separator + fileName + ".xml";
				logger.info("filePath=" + filePath);
				File file = new File(filePath);
				SAXReader saxReader = new SAXReader();
				Document doc = saxReader.read(file);
				logger.info(doc.getRootElement().asXML());
				rootEl.add(doc.getRootElement().createCopy());
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}

		List<TestCaseResultBean> results = parseTestResults(allDoc);
		String reportContent = getReportContent(results);
		ReportBean bean = getReportData();

		File file = new File(suite.getOutputDirectory() + File.separator + bean.getSuite() + ".html");
		File xmlfile = new File(suite.getOutputDirectory() + File.separator + bean.getSuite() + ".xml");
		try {
			FileUtils.writeStringToFile(xmlfile, allDoc.asXML());
			FileUtils.writeStringToFile(file, reportContent);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String pdfFileName = getPdfFileName();
		PdfReport report = new PdfReport(bean, suite.getOutputDirectory());
		report.create(pdfFileName);
		logger.info("pdfReportName=" + pdfFileName);
	}

	private String getFtpFolderName() {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		ReportBean bean = getReportData();
		return bean.getSuite() + "_" + bean.getBrowser() + "_" + format.format(startDate) + "_rnd_" + rndNum;
	}

	private String getPdfFileName() {
		ReportBean bean = getReportData();
		return bean.getSuite() + "_" + bean.getBrowser() + "_" + bean.getExecutionTime().replaceAll(" +", "") + ".pdf";
	}

	/*
	 * replace html code in message
	 */
	private String fiterHtmlTag(String str) {
		if (str != null && !str.isEmpty()) {
			str = str.replaceAll("<", "");
			str = str.replaceAll(">", "");
			return str;
		}
		return "";
	}

	private boolean hasTestcaseInGroup(List<TestCaseResultBean> results, ResultStatusEnum status) {
		for (TestCaseResultBean tcrb : results) {
			if (tcrb.getStatus().equals(status)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * format html report
	 */
	private String getReportContent(List<TestCaseResultBean> results) {
		StringBuffer content = new StringBuffer();
		File file = new File(Util.getClassPath() + "reports" + File.separator + "email-template.html");
		
		try {
			content.append(FileUtils.readFileToString(file));
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuffer failedContent = new StringBuffer();
		failedContent.append("<div class='failed-title'><h1>Failed List</h1></div>");
		failedContent.append("<br/>");		
		failedContent.append("<table class='failed-table' border='1' cellpadding='0' cellspacing='0'>");
		failedContent
				.append("<tr class='table-header'><td>Name</td><td>Owner</td><td>CostTime(s)</td><td>ErrorMessage</td><td>StackInfo</td></tr>");

		StringBuffer passedContent = new StringBuffer();
		passedContent.append("<div class='passed-title'><h1>Passed List</h1></div>");
		passedContent.append("<br/>");	
		passedContent.append("<table class='passed-table' border='1' cellpadding='0' cellspacing='0'>");
		passedContent
				.append("<tr class='table-header'><td>Name</td><td>Owner</td><td>CostTime(s)</td><td>ClassName</td></tr>");

		StringBuffer skippedContent = new StringBuffer();
		skippedContent.append("<div class='skipped-title'><h1>Skipped List</h1></div>");
		skippedContent.append("<br/>");	
		skippedContent.append("<table class='skipped-table' border='1' cellpadding='0' cellspacing='0'>");
		skippedContent
				.append("<tr class='table-header'><td>Name</td><td>Owner</td><td>CostTime(s)</td><td>ClassName</td></tr>");

		for (TestCaseResultBean tcrb : results) {
			String name = tcrb.getName();
			String owner = TestObjectManager.getOwner(name);
			switch (tcrb.getStatus()) {
				case PASSED:
					passedContent.append("<tr>");
					passedContent.append("<td>" + name + "</td>");
					passedContent.append("<td>" + owner + "</td>");
					passedContent.append("<td>" + tcrb.getCostTime() + "</td>");
					passedContent.append("<td>" + tcrb.getClassName() + "</td>");
					passedContent.append("</tr>");
					break;
				case FAILED:
					failedContent.append("<tr>");
					failedContent.append("<td>" + name + "</td>");
					failedContent.append("<td>" + owner + "</td>");
					failedContent.append("<td>" + tcrb.getCostTime() + "</td>");
					ReportBean bean = getReportData();
					String targetFTPFolderName = getFtpFolderName();
					String logFilePath = "ftp://" + BaseCons.FTP_HOST + File.separator + bean.getTeam()
							+ File.separator + targetFTPFolderName + File.separator + bean.getSuite() + File.separator
							+ BaseCons.LOG_FOLDER + File.separator + name + ".html";
					String errMessage = "<a href='" + logFilePath + "'>logger :</a>\r\n";
					errMessage += fiterHtmlTag(StringEscapeUtils.unescapeXml(tcrb.getErrorMessage()));
					errMessage = errMessage.replaceAll("Timed out after \\d* seconds:", "");
					errMessage = errMessage.replaceAll("Build info.*", "");
					failedContent.append("<td class='error'>" + errMessage + "</td>");
					failedContent.append("<td><pre>" + fiterHtmlTag(tcrb.getStackInfo()) + "</pre></td>");
					failedContent.append("</tr>");
					break;
				case SKIPPED:
					skippedContent.append("<tr>");
					skippedContent.append("<td>" + name + "</td>");
					skippedContent.append("<td>" + owner + "</td>");
					skippedContent.append("<td>" + tcrb.getCostTime() + "</td>");
					skippedContent.append("<td>" + tcrb.getClassName() + "</td>");
					skippedContent.append("</tr>");
					break;
			}
		}

		failedContent.append("</table>");
		passedContent.append("</table>");
		skippedContent.append("</table>");
		
		if (hasTestcaseInGroup(results, ResultStatusEnum.FAILED)) {
			content.append(failedContent);
		}

		if (hasTestcaseInGroup(results, ResultStatusEnum.PASSED)) {
			content.append(passedContent);
		}

		if (hasTestcaseInGroup(results, ResultStatusEnum.SKIPPED)) {
			content.append(skippedContent);
		}

		return content.toString();
	}

	private boolean isSkippedCase(Element testcaseEl) {
		List<Element> list = XmlUtil.getElementList(testcaseEl, "skipped");
		return list.size() > 0;
	}

	private boolean isFailedCase(Element testcaseEl) {
		List<Element> list = XmlUtil.getElementList(testcaseEl, "failure");
		return list.size() > 0;
	}

	/*
	 * parse xml
	 */
	private List<TestCaseResultBean> parseTestResults(Document allDoc) {
		List<TestCaseResultBean> results = new ArrayList<TestCaseResultBean>();
		List<Element> testcaseList = XmlUtil.getElementList(allDoc, "/testsuites/testsuite/testcase");

		for (Element testcaseEl : testcaseList) {
			TestCaseResultBean tcrb = new TestCaseResultBean();
			String name = XmlUtil.getElementAttributeValue(testcaseEl, "name", "");
			if (name.startsWith("@")) {
				continue;
			}
			ResultStatusEnum status = null;
			float costTime = Float.parseFloat(XmlUtil.getElementAttributeValue(testcaseEl, "time", ""));

			String className = XmlUtil.getElementAttributeValue(testcaseEl, "classname", "");
			String errorMessage = null;
			String stackInfo = null;

			if (isFailedCase(testcaseEl)) {
				// failed testcase
				status = ResultStatusEnum.FAILED;
				Element failureEl = XmlUtil.getSingleElement(testcaseEl, "failure");
				errorMessage = XmlUtil.getElementAttributeValue(failureEl, "message", "");
				stackInfo = XmlUtil.getElementValue(failureEl, "");
			} else if (isSkippedCase(testcaseEl)) {
				// skipped testcase
				status = ResultStatusEnum.SKIPPED;
			} else {
				// passeded testcase
				status = ResultStatusEnum.PASSED;
			}

			tcrb.setName(name);
			tcrb.setStatus(status);
			tcrb.setCostTime(costTime);
			tcrb.setClassName(className);
			tcrb.setErrorMessage(errorMessage);
			tcrb.setStackInfo(stackInfo);
			results.add(tcrb);

		}
		return filterData(results);
	}

	private HashSet<TestCaseResultBean> getPassedResultSet(List<TestCaseResultBean> results) {
		HashSet<TestCaseResultBean> passedSet = new HashSet<TestCaseResultBean>();
		for (TestCaseResultBean tcrb : results) {
			if (tcrb.getStatus().equals(ResultStatusEnum.PASSED)) {
				passedSet.add(tcrb);
			}
		}
		return passedSet;
	}

	private HashSet<TestCaseResultBean> getFailedResultSet(List<TestCaseResultBean> results,
			HashSet<TestCaseResultBean> passedSet) {
		HashSet<TestCaseResultBean> failedSet = new HashSet<TestCaseResultBean>();
		for (TestCaseResultBean tcrb : results) {
			if(tcrb.getStatus().equals(ResultStatusEnum.FAILED) && !passedSet.contains(tcrb)) {
				failedSet.remove(tcrb);
				failedSet.add(tcrb);
			}
		}
		return failedSet;
	}

	private HashSet<TestCaseResultBean> getSkippedResultSet(List<TestCaseResultBean> results, HashSet<TestCaseResultBean> passedSet, HashSet<TestCaseResultBean> failedSet){
		HashSet<TestCaseResultBean> skippedSet = new HashSet<TestCaseResultBean>();
		for (TestCaseResultBean tcrb : results) {
			if (tcrb.getStatus().equals(ResultStatusEnum.SKIPPED) && !passedSet.contains(tcrb) && !failedSet.contains(tcrb)) {
				skippedSet.add(tcrb);
			}
		}
		return skippedSet;
	}

	private List<TestCaseResultBean> filterData(List<TestCaseResultBean> results) {

		List<TestCaseResultBean> resultList = new ArrayList<TestCaseResultBean>();

		HashSet<TestCaseResultBean> passedSet = getPassedResultSet(results);
		HashSet<TestCaseResultBean> failedSet = getFailedResultSet(results, passedSet);
		HashSet<TestCaseResultBean> skippedSet = getSkippedResultSet(results,passedSet,failedSet);
		

		resultList.addAll(failedSet);
		resultList.addAll(passedSet);
		resultList.addAll(skippedSet);
		updateSummaryInfo(failedSet, passedSet, skippedSet);
		return resultList;

	}

	private void updateSummaryInfo(HashSet<TestCaseResultBean> failedList, HashSet<TestCaseResultBean> passedList,
			HashSet<TestCaseResultBean> skippedList) {
		ReportBean bean = this.getReportData();
		bean.setFailed(failedList.size());
		bean.setPassed(passedList.size());
		bean.setSkipped(skippedList.size());
	}

	/*private boolean isInGroup(List<TestCaseResultBean> list, String testCaseName) {
		for (TestCaseResultBean tcrb : list) {
			if (tcrb.getName().equals(testCaseName)) {
				return true;
			}
		}
		return false;
	}*/

	/*
	 * if folder is not exists, create the folder.
	 */
	private void initDestPath() {
		File file = new File(suite.getOutputDirectory());
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	private String getCurtimeStr() {
		DateFormat format = new java.text.SimpleDateFormat("yyyy/M/d HH:mm:ss");
		return format.format(Calendar.getInstance().getTime());
	}

	public void sendEmail(String content) {
		MailSenderInfo mailInfo = new MailSenderInfo();
		mailInfo.setMailServerHost(Environment.getEmailSenderInfo().getHost());
		mailInfo.setMailServerPort(Environment.getEmailSenderInfo().getPort());
		mailInfo.setValidate(true);
		mailInfo.setUserName(Environment.getEmailSenderInfo().getUserName());
		mailInfo.setPassword(Environment.getEmailSenderInfo().getPassword());
		mailInfo.setFromAddress(Environment.getEmailSenderInfo().getUserName());
		mailInfo.setFromName(Environment.getEmailSenderInfo().getFromName());
		mailInfo.setToAddress(Environment.getEmailRecipients());

		mailInfo.setSubject("[" + Util.getEnvStr() + "]--Automation Test Report--" + Environment.getTeamName() + "--"
				+ getCurtimeStr());

		logger.info("_content=" + content);

		mailInfo.setContent(content);

		try {
			EmailUtil.sendHtmlMail(mailInfo);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	public String getContent() {
		ReportBean bean = getReportData();
		String targetFTPFolderName = getFtpFolderName();
		String ftpPath = "ftp://" + BaseCons.FTP_HOST + File.separator + bean.getTeam() + File.separator
				+ targetFTPFolderName + File.separator + bean.getSuite();

		logger.info("ftpPath=" + ftpPath);
		
		StringBuffer result = new StringBuffer();
		result.append("<p>Team : " + bean.getTeam() + "</p>");
		result.append("<p>Environment : " + bean.getEnvironment() + "</p>");
		result.append("<p>URL : " + bean.getUrl() + "</p>");
		result.append("<p>Execution Time : " + bean.getExecutionTime() + "</p>");
		result.append("<p>Browser : " + bean.getBrowser() + "</p>");
		result.append("<p>Suite : " + bean.getSuite() + "</p>");

		int passed = bean.getPassed();
		int failed = bean.getFailed();
		int skipped = bean.getSkipped();

		result.append("<p># of Passed : " + passed + "</p>");
		result.append("<p># of Failed : " + failed + "</p>");
		result.append("<p># of Skipped : " + skipped + "</p>");
		
		result.append("<p>% of Failed : "
				+ String.format("%10.2f%%", (double) (failed * 100) / (passed + failed + skipped)) + "</p>");
		result.append("<p>% of Skipped : "
				+ String.format("%10.2f%%", (double) (skipped * 100) / (passed + failed + skipped)) + "</p>");
		
		String pdfFilePath = ftpPath + File.separator + BaseCons.PDFREPORT_FOLDER + File.separator + getPdfFileName();
		result.append("<p><a href='" + pdfFilePath + "'>Summary Report</a></p>");

		String relativeSceenshotFilePath = bean.getTeam() + File.separator
				+ targetFTPFolderName + File.separator + bean.getSuite() + File.separator + BaseCons.SCREENSHOT_FOLDER;
		
		if (checkFTPFolderPath(relativeSceenshotFilePath)) {
			String screenshotFilePath = "ftp://" + BaseCons.FTP_HOST + File.separator + relativeSceenshotFilePath;
			result.append("<p><a href='" + screenshotFilePath + "'>Screenshot</a></p>");
		}

		//cucumber report
		String cucumberReportPath="ftp://" + BaseCons.FTP_HOST + File.separator + bean.getTeam() + File.separator
			+ targetFTPFolderName + File.separator+CucumberReportManager.getReportFolder() + File.separator + CucumberReportManager.getReportName();
		result.append("<p><a href='" + cucumberReportPath + "'>Cucumber Report</a></p>");		
		
		String emailTemplateFilePath = suite.getOutputDirectory() + File.separator + bean.getSuite() + ".html";
		File file = new File(emailTemplateFilePath);

		try {
			String detailContent = FileUtils.readFileToString(file);
			result.append(detailContent);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Add debug information in UI report
		result.append("<p>*************** Debug Information ***************</p>");
		String automaionBaseLoc = Environment.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		String automaionBaseVersion = automaionBaseLoc.substring(automaionBaseLoc.lastIndexOf("/") + 1);
		result.append("<p>AutomationBase : " + automaionBaseVersion + "</p>");
		
		//Add job info.
		String jobInf = GetJobInf();
		if(jobInf != null){
			result.append(jobInf);
		}
		
//		logger.info(result);
		return result.toString();
	}
	
	private static boolean checkFTPFolderPath(String path) {
		FTPClient ftp = new FTPClient();
		try {
			ftp.connect(BaseCons.FTP_HOST, BaseCons.FTP_PORT);
			ftp.login(BaseCons.FTP_USER_NAME, BaseCons.FTP_USER_PWD);

			ftp.changeWorkingDirectory(path);
			if (ftp.getReplyCode() == 550) {
				return false;
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
	
	private static String GetJobInf() {
		String jobInf = System.getProperty(BaseCons.PROPERTY_JOBINF_KEY);
//		String jobInf = "{'JobInf':{'JobName':'HG/Mercury_Automation_BranchDebug_Tests','BuildId':'#821','BuildURL':'https://jenkins.morningstar.com/job/HG/job/Mercury_Automation_BranchDebug_Tests/821/'}}";
		if(jobInf != null) {		
			JSONObject jsonObj = JSONObject.fromObject(JSONObject.fromObject(jobInf).getString("JobInf"));
			
			@SuppressWarnings("unchecked")
			Iterator<String> keys = jsonObj.keys();
			String temp = "";
			
			while(keys.hasNext()){
				String key = keys.next();
				temp += "<p>" + key + " : " + jsonObj.get(key) + "</p>";
			}			
			
			return temp;
		}else{
			return null;
		}
	}

	public static void main(String[] args) {
//		String path1 = "Mercury\\Debug_Park_FTP_Issue_firefox_2015-12-28-17-50-57_rnd_244\\Debug_Park_FTP_Issue\\screenshot";
//		String path2 = "Mercury\\Debug_Park_FTP_Issue_firefox_2015-12-28-17-50-57_rnd_244\\Debug_Park_FTP_Issue\\";
//		String path3 = "Mercury\\Debug_Lzhang7-15668_chrome_2015-12-27-23-59-10_rnd_262\\Debug_Lzhang7-15668\\screenshot\\";
//		String path4 = "Mercury\\Debug_Lzhang7-15668_chrome_2015-12-27-23-59-10_rnd_262\\Debug1_Lzhang7-15668\\11";
//
//		System.out.println(checkFTPFolderPath(path1));
//		System.out.println(checkFTPFolderPath(path2));
//		System.out.println(checkFTPFolderPath(path3));
//		System.out.println(checkFTPFolderPath(path4));
		
		System.out.println(GetJobInf());
		System.out.println((double)(Math.round(3*100/(3+4+0))) + "%");
		System.out.println(String.format("%10.2f%%", (double)(3*100)/(3+4+2)));
	}
	
}
