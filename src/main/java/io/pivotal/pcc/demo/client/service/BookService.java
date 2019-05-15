package io.pivotal.pcc.demo.client.service;

import java.util.Random;

import com.github.javafaker.Faker;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.geode.core.util.ObjectUtils;
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

	@Cacheable("Books")
	public Book getBook(String isbn) {

		ObjectUtils.<String>doOperationSafely(() -> {
			Thread.sleep(new Random().nextInt(1500));
			return "success";
		}, cause -> "error");

		com.github.javafaker.Book fakerBook = faker.book();

		return new Book(isbn, fakerBook.title(), fakerBook.author(), fakerBook.genre());
	}

	@CachePut(cacheNames = "Books", key = "#root.args[0].isbn")
	public Book addBook(Book book) {
		return book;
	}

	@CacheEvict(cacheNames = "Books", key = "#root.args[0].isbn")
	public void removeBook(Book book) { }

}
