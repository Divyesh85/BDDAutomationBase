package com.morningstar.automation.bdd.utils;

import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.morningstar.automation.base.core.configurations.Environment;

public class UtilExtend {
	public static String GetNodeName(WebDriver driver) {
		String node = null;
		try {
			String strTemp = Environment.getHub();
			String hub = strTemp.split("/")[2].split(":")[0];
			int port = Integer.parseInt(strTemp.split("/")[2].split(":")[1]);

			HttpHost host = new HttpHost(hub, port);

			@SuppressWarnings({ "resource" })
			DefaultHttpClient client = new DefaultHttpClient();
			String sessionUrl = "http://" + hub + ":" + port + "/grid/api/testsession?session=";
			URL session = new URL(sessionUrl + ((RemoteWebDriver) driver).getSessionId());

			BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("POST", session.toExternalForm());
			org.apache.http.HttpResponse response = client.execute(host, req);

			JSONObject object = new JSONObject(EntityUtils.toString(response.getEntity()));

			String proxyID = (String) object.get("proxyId");
			if (proxyID.indexOf("//") > -1)
				node = (proxyID.split("//")[1].split(":")[0]);
			else
				node = proxyID;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return node;
	}
}
