package io.pivotal.pcc.demo.client.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;

@Data
@Region("Books")
@AllArgsConstructor
public class Book {

	@Id
	private String isbn;
	private String title;
	private String author;
	private String genre;

}
