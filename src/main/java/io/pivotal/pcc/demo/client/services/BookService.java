package io.pivotal.pcc.demo.client.services;

import java.util.Optional;
import java.util.Random;

import com.github.javafaker.Faker;

import org.apache.geode.cache.Region;

import io.pivotal.pcc.demo.client.domain.Book;

public class BookService {

	private final Faker faker;
	private final Region<String, Book> bookRegion;

	public BookService(Region<String, Book> bookRegion) {
		this.faker = new Faker();
		this.bookRegion = bookRegion;
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
			com.github.javafaker.Book fakerBook = faker.book();
			Book book = new Book(fakerBook.title(), isbn, fakerBook.author(), fakerBook.genre());
			addBook(book);
			return book;
		});
	}

	private void addBook(Book book) {
		bookRegion.put(book.getIsbn(), book);
	}
}
