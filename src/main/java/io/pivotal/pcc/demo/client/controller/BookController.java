package io.pivotal.pcc.demo.client.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.pcc.demo.client.domain.Book;
import io.pivotal.pcc.demo.client.service.BookService;

/**
 * The BookController class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RestController
public class BookController {

	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@GetMapping("/")
	public String home() {
		return "<H1>Using Spring Boot for Pivotal GemFire</H1>";
	}

	@GetMapping("/ping")
	public String ping() {
		return "<H1>PONG</H1>";
	}

	@GetMapping("/books/{isbn}")
	public String getBook(@PathVariable String isbn) {
		long start = System.currentTimeMillis();
		Book book = this.bookService.getBook(isbn);
		long end = System.currentTimeMillis();
		return String.format("<H2>It took[%d] millis to execute get Book [%s] for ISBN [%s]</H2>",
			(end - start), book.toString(), isbn);
	}
}
