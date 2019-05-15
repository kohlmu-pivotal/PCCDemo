package io.pivotal.pcc.demo.client.domain;

import java.io.Serializable;

import javax.persistence.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;

@Entity
@Region("Books")
public class Book implements Serializable {

	@Id
	@javax.persistence.Id
	private String isbn;
	private String title;
	private String author;
	private String genre;

	public Book() {
	}

	public Book(String isbn, String title, String author, String genre) {
		this.title = title;
		this.isbn = isbn;
		this.author = author;
		this.genre = genre;
	}

	public String getAuthor() {
		return author;
	}

	public String getIsbn() {
		return isbn;
	}

	public String getGenre() {
		return genre;
	}

	public String getTitle() {
		return title;
	}

	@Override public String toString() {
		return "Book{" +
			"isbn='" + isbn + '\'' +
			", title='" + title + '\'' +
			", author='" + author + '\'' +
			", genre='" + genre + '\'' +
			'}';
	}
}
