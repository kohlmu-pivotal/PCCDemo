package io.pivotal.pcc.demo.client.configuration;

import com.github.javafaker.Faker;

import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.cache.config.EnableGemfireCaching;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.config.annotation.EnableSecurity;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

import io.pivotal.pcc.demo.client.domain.Book;
import io.pivotal.pcc.demo.client.repositories.BookRepository;

@ClientCacheApplication
@EnableClusterConfiguration
@EnableEntityDefinedRegions(basePackageClasses = Book.class)
@EnableGemfireCaching
@EnableGemfireRepositories(basePackageClasses = BookRepository.class)
@EnablePdx
@EnableSecurity
public class ClientAppConfiguration {

	@Bean
	public Faker getFakeValueService() {
		return new Faker();
	}

}
