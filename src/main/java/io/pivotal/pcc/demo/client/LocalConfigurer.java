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

import io.pivotal.pcc.demo.client.configuration.Configurer;
import io.pivotal.pcc.demo.client.domain.SecurityUser;

public class LocalConfigurer implements Configurer {

	@Override
	public Map parseConnectionProperties() throws ParseException {
		Map pccProperties = new TreeMap();
		List<SecurityUser> userList = new ArrayList();

		pccProperties.put("locators", new String[]{"localhost[55221]"});
		pccProperties.put("securityUsers", userList);
		return pccProperties;
	}
}
