package io.pivotal.pcc.demo.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.pivotal.pcc.demo.client.domain.SecurityUser;

public class PCCConfigurer {

	public Map parseConnectionProperties() throws ParseException {
		Map pccProperties = new TreeMap();
		List<SecurityUser> userList = new ArrayList();

		String vcap_services = System.getenv("VCAP_SERVICES");

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(vcap_services);

		JSONArray pCloudCacheArray = (JSONArray) jsonObject.get("p-cloudcache");
		JSONObject pCloudCacheObj = (JSONObject) pCloudCacheArray.get(0);
		JSONObject credentials = (JSONObject) pCloudCacheObj.get("credentials");
		JSONArray usersArray = (JSONArray) credentials.get("users");
		JSONArray locatorsJson = (JSONArray) credentials.get("locators");

		pccProperties.put("locators", Arrays.copyOf(locatorsJson.toArray(), locatorsJson.size(), String[].class));

		for (Object user : usersArray) {
			JSONObject userJsonObject = (JSONObject) user;
			userList.add(new SecurityUser((String) userJsonObject.get("username"),
				(String) userJsonObject.get("password"),
				((JSONArray) userJsonObject.get("roles")).toArray()));
		}
		pccProperties.put("securityUsers", userList);
		return pccProperties;
	}
}
