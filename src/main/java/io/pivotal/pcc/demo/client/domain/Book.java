package io.pivotal.pcc.demo.client.domain;

import java.io.Serializable;
import java.util.Date;

public class Book implements Serializable {
	private String title;
	private String isbn;
	private String author;
	private Date publishDate;

	public Book() {
	}

	public Book(String title, String isbn, String author, Date publishDate) {
		this.title = title;
		this.isbn = isbn;
		this.author = author;
		this.publishDate = publishDate;
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

	public Date getPublishDate() {
		return publishDate;
	}
}
