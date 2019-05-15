package io.pivotal.pcc.demo.client;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.json.simple.parser.ParseException;

import io.pivotal.pcc.demo.client.configuration.Configurer;
import io.pivotal.pcc.demo.client.domain.Book;
import io.pivotal.pcc.demo.client.domain.SecurityUser;
import io.pivotal.pcc.demo.client.security.ClientAuthentication;


public class ClientApp {
	private ClientCache clientCache;
	private Region bookRegion;

	public ClientApp(Configurer configurer) {
		try {
			clientCache = initializeClientCache(configurer.parseConnectionProperties());
			initializeClientRegions(clientCache);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void initializeClientRegions(ClientCache clientCache) {
		bookRegion = clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("User");
	}

	private ClientCache initializeClientCache(Map properties) {
		ClientCacheFactory clientCacheFactory = new ClientCacheFactory();
		initializeLocatorPool(clientCacheFactory, (String[]) properties.get("locators"));
		initializeSecurity(clientCacheFactory, (List<SecurityUser>) properties.get("securityUsers"));
		initializePDXSerialization(clientCacheFactory);
		return clientCacheFactory.create();
	}

	private void initializePDXSerialization(ClientCacheFactory clientCacheFactory) {
		clientCacheFactory.setPdxSerializer(new ReflectionBasedAutoSerializer(Book.class.getCanonicalName()));
	}

	private void initializeSecurity(ClientCacheFactory clientCacheFactory, List<SecurityUser> userList) {
		userList.stream()
			.filter(user -> Arrays.asList(user.getRoles()).contains("developer"))
			.forEach(user -> {
				clientCacheFactory.set("security-client-auth-init", ClientAuthentication.class.getCanonicalName());
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

	public static void main(String[] args) throws InterruptedException {
		ClientApp clientApp = new ClientApp(new LocalConfigurer());
		Book book = clientApp.getBook("12343BNS22");
		System.err.println("Retrieved Book: " + book);
		while (true) {
			Thread.sleep(1000);
		}
	}

	public Book getBook(String isbn) {
		return (Book) Optional.ofNullable(bookRegion.get(isbn)).orElseGet(() ->
		{
			try {
				Thread.sleep(new Random().nextInt(1500));
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			Book book = new Book("The Myths of Lore and Legends", isbn, "The Spring Developer",
				new Date());
			bookRegion.put(isbn, book);
			return book;
		});
	}

	private void addBook(Book book) {
		bookRegion.put(book.getIsbn(), book);
	}
}