package io.pivotal.pcc.demo.client.services;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.json.simple.parser.ParseException;

import io.pivotal.pcc.demo.client.PCCConfigurer;
import io.pivotal.pcc.demo.client.domain.Book;
import io.pivotal.pcc.demo.client.domain.SecurityUser;
import io.pivotal.pcc.demo.client.security.ClientAuthentication;

public class CacheService {

	private ClientCache clientCache;
	private Region bookRegion;

	public CacheService() {
		try {
			clientCache = initializeClientCache(new PCCConfigurer().parseConnectionProperties());
			initializeClientRegions(clientCache);
		}
		catch (ParseException e) {
			e.printStackTrace();
			System.exit(99);
		}
	}

	private void initializePDXSerialization(ClientCacheFactory clientCacheFactory) {
		clientCacheFactory.setPdxSerializer(new ReflectionBasedAutoSerializer(Book.class.getCanonicalName()));
	}

	private void initializeSecurity(ClientCacheFactory clientCacheFactory, List<SecurityUser> userList) {
		clientCacheFactory
			.set(ConfigurationProperties.SECURITY_CLIENT_AUTH_INIT, ClientAuthentication.class.getCanonicalName());
		userList.stream()
			.filter(user -> Arrays.asList(user.getRoles()).contains("developer"))
			.forEach(user -> {
				clientCacheFactory.set("security-username", user.getUsername());
				clientCacheFactory.set("security-password", user.getPassword());
			});
	}

	private void initializeLocatorPool(ClientCacheFactory clientCacheFactory, String[] locators) {
		for (String locator : locators) {
			String hostName = locator.substring(0, locator.lastIndexOf("["));
			String port = locator.substring(locator.lastIndexOf("[") + 1, locator.lastIndexOf("]"));
			clientCacheFactory.addPoolLocator(hostName, Integer.parseInt(port));
		}
	}

	private void initializeClientRegions(ClientCache clientCache) {
		bookRegion = clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("Books");
	}

	private ClientCache initializeClientCache(Map properties) {
		ClientCacheFactory clientCacheFactory = new ClientCacheFactory();
		initializePDXSerialization(clientCacheFactory);
		initializeLocatorPool(clientCacheFactory, (String[]) properties.get("locators"));
		initializeSecurity(clientCacheFactory, (List<SecurityUser>) properties.get("securityUsers"));
		return clientCacheFactory.create();
	}

	public Region getBookRegion() {
		return bookRegion;
	}
}
