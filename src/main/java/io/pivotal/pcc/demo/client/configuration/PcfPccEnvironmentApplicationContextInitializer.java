package io.pivotal.pcc.demo.client.configuration;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.Assert;

public class PcfPccEnvironmentApplicationContextInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final String DEFAULT_PCF_PCC_SERVICE_INSTANCE_NAME = "pccService";
	private static final String PCC_DEMO_CONFIGURATION_PROPERTY_SOURCE_NAME = "pcc-demo.configuration.properties";
	private static final String PCF_PCC_SERVICE_NAME_PROPERTY = "io.pivotal.cloudfoundry.services.cloudcache.name";
	private static final String PCF_PCC_USER_REQUIRED_ROLE = "cluster_operator";

	// PCC Configuration Properties
	private static final String PCC_LOCATORS_PROPERTY = "vcap.services.%s.credentials.locators";
	private static final String PCC_GFSH_URL_PROPERTY = "vcap.services.%s.credentials.urls.gfsh";
	private static final String PCC_USER_NAME_PROPERTY = "vcap.services.%s.credentials.users[%d].username";
	private static final String PCC_USER_ROLE_PROPERTY = "vcap.services.%s.credentials.users[%d].roles";
	private static final String PCC_USER_PASSWORD_PROPERTY = "vcap.services.%s.credentials.users[%d].password";

	// SDG Configuration Properties
	private static final String SDG_MANAGEMENT_HTTP_HOST_PROPERTY = "spring.data.gemfire.management.http.host";
	private static final String SDG_MANAGEMENT_HTTP_PORT_PROPERTY = "spring.data.gemfire.management.http.port";
	private static final String SDG_POOL_LOCATORS_PROPERTY = "spring.data.gemfire.pool.locators";
	private static final String SDG_SECURITY_USERNAME_PROPERTY = "spring.data.gemfire.security.username";
	private static final String SDG_SECURITY_PASSWORD_PROPERTY = "spring.data.gemfire.security.password";

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		ConfigurableEnvironment environment = applicationContext.getEnvironment();
		Properties gemfireProperties = GemfirePropertiesBuilder.from(environment).build();
		environment.getPropertySources()
			.addLast(new PropertiesPropertySource(PCC_DEMO_CONFIGURATION_PROPERTY_SOURCE_NAME, gemfireProperties));
	}

	protected static class GemfirePropertiesBuilder {
		private final ConfigurableEnvironment environment;
		private final Properties gemfireProperties = new Properties();
		private final String pivotalCloudCacheServiceInstanceName;
		protected static GemfirePropertiesBuilder from(ConfigurableEnvironment environment) {
			return new GemfirePropertiesBuilder(environment);
		}

		protected GemfirePropertiesBuilder(ConfigurableEnvironment environment) {
			Assert.notNull(environment, "Environment is required");
			this.environment = environment;
			this.pivotalCloudCacheServiceInstanceName =
				environment.getProperty(PCF_PCC_SERVICE_NAME_PROPERTY, DEFAULT_PCF_PCC_SERVICE_INSTANCE_NAME);
		}

		protected ConfigurableEnvironment getEnvironment() {
			return this.environment;
		}
		protected String getPivotalCloudCacheServiceInstanceName() {
			return this.pivotalCloudCacheServiceInstanceName;
		}
		protected String getProperty(String propertyName, Object... args) {
			List<Object> arguments = new ArrayList<>(Collections.singleton(getPivotalCloudCacheServiceInstanceName()));
			Collections.addAll(arguments, args);
			return getEnvironment().getProperty(resolvePropertyName(propertyName, arguments.toArray()));
		}

		private URL toUrl(String urlString) {
			try {
				return URI.create(urlString).toURL();
			}
			catch (MalformedURLException ignore) {
				return null;
			}
		}

		private String resolvePropertyName(String baseProperty, Object... args) {
			return String.format(baseProperty, args);
		}

		private int findClusterOperatorUserIndex() {
			int index = 0;
			ConfigurableEnvironment environment = getEnvironment();
			String pccServiceName = getPivotalCloudCacheServiceInstanceName();
			String roleProperty = resolvePropertyName(PCC_USER_ROLE_PROPERTY, pccServiceName, index);
			while (environment.containsProperty(roleProperty)) {
				if (String.valueOf(environment.getProperty(roleProperty)).contains(PCF_PCC_USER_REQUIRED_ROLE)) {
					return index;
				}
				roleProperty = resolvePropertyName(PCC_USER_ROLE_PROPERTY, pccServiceName, ++index);
			}
			return -1;
		}

		private Properties configureSecurity(Properties gemfireProperties) {
			int index = findClusterOperatorUserIndex();
			if (index > -1) {
				String username = getProperty(PCC_USER_NAME_PROPERTY, index);
				String password = getProperty(PCC_USER_PASSWORD_PROPERTY, index);

				gemfireProperties.setProperty(SDG_SECURITY_USERNAME_PROPERTY, username);
				gemfireProperties.setProperty(SDG_SECURITY_PASSWORD_PROPERTY, password);
			}
			return gemfireProperties;
		}

		private Properties configureManagement(Properties gemfireProperties) {
			String gfshUrl = getProperty(PCC_GFSH_URL_PROPERTY);
			return Optional.ofNullable(gfshUrl)
				.map(this::toUrl)
				.map(url -> {
					gemfireProperties.setProperty(SDG_MANAGEMENT_HTTP_HOST_PROPERTY, url.getHost());
					if (url.getPort() > 0) {
						gemfireProperties.setProperty(SDG_MANAGEMENT_HTTP_PORT_PROPERTY, String.valueOf(url.getPort()));
					}
					return gemfireProperties;

				})
				.orElse(gemfireProperties);
		}

		private Properties configureLocator(Properties gemfireProperties) {
			String locators = getProperty(PCC_LOCATORS_PROPERTY);
			gemfireProperties.setProperty(SDG_POOL_LOCATORS_PROPERTY, locators);
			return gemfireProperties;
		}

		public Properties build() {
			return configureSecurity(configureManagement(configureLocator(this.gemfireProperties)));
		}
	}
}
