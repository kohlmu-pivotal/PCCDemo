package io.pivotal.pcc.demo.client.service;

import java.util.Random;

import com.github.javafaker.Faker;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.pivotal.pcc.demo.client.domain.Book;
import io.pivotal.pcc.demo.client.repositories.BookRepository;

@Service
public class BookService {

	private final BookRepository bookRepository;

	private final Faker faker;

	public BookService(BookRepository bookRepository, Faker faker) {
		this.bookRepository = bookRepository;
		this.faker = faker;
	}

	private void sleep(int bound) {

		try {
			Thread.sleep(new Random().nextInt(bound));
		}
		catch (InterruptedException cause) {
			cause.printStackTrace();
		}
	}

	@Cacheable("Books")
	public Book getBook(String isbn) {

		sleep(1500);

		com.github.javafaker.Book fakerBook = faker.book();
		Book book = new Book(isbn, fakerBook.title(), fakerBook.author(), fakerBook.genre());

		return book;
	}

	@CachePut(cacheNames = "Books", key = "#root.args[0].isbn")
	public Book addBook(Book book) {
		return book;
	}
}
