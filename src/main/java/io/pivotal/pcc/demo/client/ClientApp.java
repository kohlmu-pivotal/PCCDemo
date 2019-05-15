package io.pivotal.pcc.demo.client;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import io.pivotal.pcc.demo.client.configuration.PcfPccEnvironmentApplicationContextInitializer;
import io.pivotal.pcc.demo.client.service.BookService;

@SpringBootApplication
public class ClientApp {

	public static void main(String[] args) {

		new SpringApplicationBuilder(ClientApp.class)
			.web(WebApplicationType.SERVLET)
			.initializers(new PcfPccEnvironmentApplicationContextInitializer())
			.build()
			.run(args);
	}

	@Bean
	ApplicationRunner runner(BookService bookService) {
		return args -> bookService.getBook("1235432BMF342");
	}
}
