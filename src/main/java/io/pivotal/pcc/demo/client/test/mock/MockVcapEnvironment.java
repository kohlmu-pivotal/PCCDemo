package io.pivotal.pcc.demo.client.test.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

public abstract class MockVcapEnvironment {

	private static final String APPLICATION_VCAP_PROPERTIES = "application-vcap.properties";

	private static final Set<String> managedProperties = Collections.synchronizedSet(new HashSet<>());

	public static void setSystemProperties() {

		try {

			Resource vcapResource =
				new ClassPathResource(APPLICATION_VCAP_PROPERTIES, ClassUtils.getDefaultClassLoader());

			Properties vcapProperties = new Properties();

			vcapProperties.load(vcapResource.getInputStream());

			vcapProperties.stringPropertyNames().forEach(propertyName -> {
				managedProperties.add(propertyName);
				System.setProperty(propertyName, vcapProperties.getProperty(propertyName));
			});

			registerShutdownHook();
		}
		catch (IOException cause) {
			throw new IllegalStateException("Failed to load and initialize the VCAP environment", cause);
		}
	}

	private static void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(MockVcapEnvironment::clearSystemProperties,
			"Destroy VCAP Environment Thread"));
	}

	public static void clearSystemProperties() {
		managedProperties.forEach(System::clearProperty);
	}
}
