package io.pivotal.pcc.demo.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ClientApp {

	private String[] locators;
	private List<User> userList = new ArrayList<>();

	private ClientCache clientCache;
	private Region userRegion;

	public ClientApp() {
		try {
			initializePCCConnectionProperties();
			clientCache = initializeClientCache(locators, userList);
			initializeClientRegions(clientCache);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void initializeClientRegions(ClientCache clientCache) {
		userRegion = clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("User");
	}

	private ClientCache initializeClientCache(String[] locators, List<User> userList) {
		ClientCacheFactory clientCacheFactory = new ClientCacheFactory();
		clientCacheFactory = initializeLocatorPool(clientCacheFactory, locators);
		clientCacheFactory = initializeSecurity(clientCacheFactory, userList);
		return clientCacheFactory.create();
	}

	private ClientCacheFactory initializeSecurity(ClientCacheFactory clientCacheFactory, List<User> userList) {
		clientCacheFactory.set("security-client-auth-init", ClientAuthentication.class.getCanonicalName());
		userList.stream()
			.filter(user -> Arrays.asList(user.roles).contains("developer"))
			.forEach(user -> {
				clientCacheFactory.set("security-username", user.username);
				clientCacheFactory.set("security-password", user.password);
			});
		return clientCacheFactory;
	}

	private ClientCacheFactory initializeLocatorPool(ClientCacheFactory clientCacheFactory, String[] locators) {
		for (String locator : locators) {

			String hostName = locator.substring(0, locator.lastIndexOf("["));
			String port = locator.substring(locator.lastIndexOf("[") + 1, locator.lastIndexOf("]"));
			clientCacheFactory.addPoolLocator(hostName, Integer.parseInt(port));
		}
		return clientCacheFactory;
	}

	private Properties initializePCCConnectionProperties() throws ParseException {
		Properties properties = new Properties();
		String vcap_services = System.getenv("VCAP_SERVICES");

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(vcap_services);

		JSONArray pCloudCache = (JSONArray) jsonObject.get("p-cloudcache");
		JSONObject pCloudCache1 = (JSONObject) pCloudCache.get(0);
		JSONObject credentials = (JSONObject) pCloudCache1.get("credentials");
		JSONArray usersArray = (JSONArray) credentials.get("users");
		JSONArray locatorsJson = (JSONArray) credentials.get("locators");

		locators = Arrays.copyOf(locatorsJson.toArray(), locatorsJson.size(), String[].class);

		for (Object user : usersArray) {
			JSONObject userJsonObject = (JSONObject) user;
			userList.add(new User((String) userJsonObject.get("username"),
				(String) userJsonObject.get("password"),
				((JSONArray) userJsonObject.get("roles")).toArray()));
		}

		return properties;
	}

	public static void main(String[] args) throws InterruptedException {
		ClientApp clientApp = new ClientApp();
		clientApp.addUser("Sam", "Smith");
		while (true) {
			Thread.sleep(1000);
		}
	}

	private void addUser(String firstName, String surname) {
		userRegion.put(1, firstName + " " + surname);
	}

	private class User {

		private String username;
		private String password;
		private String[] roles;

		public User(String username, String password, Object[] roles) {
			this.username = username;
			this.password = password;
			this.roles = Arrays.copyOf(roles, roles.length, String[].class);
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

		public String[] getRoles() {
			return roles;
		}

		@Override public String toString() {
			return "User{" +
				"username='" + username + '\'' +
				", password='" + password + '\'' +
				", roles=" + Arrays.toString(roles) +
				'}';
		}
	}
}