package io.pivotal.pcc.demo.client;

import com.github.javafaker.Faker;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;

import io.pivotal.pcc.demo.client.domain.Book;
import io.pivotal.pcc.demo.client.service.BookService;

@SpringBootApplication
@EnableClusterConfiguration(useHttp = true)
@EnableEntityDefinedRegions(basePackageClasses = Book.class)
public class ClientApp {

	public static void main(String[] args) {

		new SpringApplicationBuilder(ClientApp.class)
			.web(WebApplicationType.SERVLET)
			.build()
			.run(args);
	}

	@Bean
	ApplicationRunner runner(BookService bookService) {
		return args -> bookService.getBook("1235432BMF342");
	}

	@Bean
	public Faker getFakerDataService() {
		return new Faker();
	}
}
