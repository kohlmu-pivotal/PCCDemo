package io.pivotal.pcc.demo.client.domain;

import java.io.Serializable;

public class Book implements Serializable {

	private String title;
	private String isbn;
	private String author;
	private String genre;

	public Book() {
	}

	public Book(String title, String isbn, String author, String genre) {
		this.title = title;
		this.isbn = isbn;
		this.author = author;
		this.genre = genre;
	}

	public String getTitle() {
		return title;
	}

	public String getIsbn() {
		return isbn;
	}

	public String getAuthor() {
		return author;
	}

	public String getGenre() {
		return genre;
	}

	@Override public String toString() {
		return "Book{" +
			"title='" + title + '\'' +
			", isbn='" + isbn + '\'' +
			", author='" + author + '\'' +
			", genre='" + genre + '\'' +
			'}';
	}
}
