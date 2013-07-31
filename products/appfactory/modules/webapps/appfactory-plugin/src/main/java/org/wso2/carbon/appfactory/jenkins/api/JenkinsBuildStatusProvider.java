package org.wso2.carbon.appfactory.jenkins.api;

import hudson.model.Hudson;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.deployers.build.api.BuildStatusProvider;
import org.wso2.carbon.appfactory.deployers.build.api.BuildStatusProviderException;
import org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JenkinsBuildStatusProvider implements BuildStatusProvider {

	private static final Log log = LogFactory.getLog(JenkinsBuildStatusProvider.class);

	private HttpClient client = null;

	private static AppfactoryPluginManager.DescriptorImpl descriptor = new AppfactoryPluginManager.DescriptorImpl();


	public Map<String, String> getLastBuildInformation(String jobName)
			throws BuildStatusProviderException {
		String url = Hudson.getInstance().getRootUrlFromRequest() + "job/" + jobName + "/api/json";
		log.info("Calling jenkins api : " + url);
		GetMethod get = new GetMethod(url);
		NameValuePair valuePair =
				new NameValuePair("tree",
						"builds[number,status,timestamp,id,result]");
		get.setQueryString(new org.apache.commons.httpclient.NameValuePair[] { valuePair });

		getHttpClient().getState()
		.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(descriptor.getAdminUserName(), descriptor.getAdminPassword()));
		getHttpClient().getParams().setAuthenticationPreemptive(true);

		Map<String, String> buildInformarion = null;

		try {
			log.debug("Retrieving last build information for job : " + jobName);
			getHttpClient().executeMethod(get);
			log.info("Retrieving last build information for job : " + jobName +
					" status received : " + get.getStatusCode());
			if (get.getStatusCode() == HttpStatus.SC_OK) {
				String response = get.getResponseBodyAsString();
				log.debug("Returns build information for job : " + jobName + " - " + response);
				buildInformarion = extractBuildInformarion(response);
			} else {
				String msg =
						"Error while retrieving  build information for job : " + jobName +
						" Jenkins returned status code : " + get.getStatusCode();
				log.error(msg);
				throw new BuildStatusProviderException(msg, BuildStatusProviderException.INVALID_RESPONSE);
			}

		} catch (HttpException e) {
			String msg = "Error occuered while calling the API";
			throw new BuildStatusProviderException(msg);

		} catch (IOException e) {
			String msg = "Error occuered while calling the API";
			throw new BuildStatusProviderException(msg);
		} finally {
			get.releaseConnection();
		}
		return buildInformarion;
	}

	private Map<String, String> extractBuildInformarion(String response) {
		Gson gson = new Gson();
		Map<String, List<Map<String, String>>> buildInfoMap =
				gson.fromJson(response,
						new TypeToken<Map<String, List<Map<String, String>>>>() {
				}.getType());
		List<Map<String, String>> buildList = buildInfoMap.get("builds");
		if (buildList.size() > 0) {
			return buildList.get(0);
		} else {
			return null;
		}
	}

	private HttpClient getHttpClient() {

		if (client == null) {
			client = new HttpClient();
		}
		return client;
	}

	void setHttpClient(HttpClient client) {
		this.client = client;
	}

}
