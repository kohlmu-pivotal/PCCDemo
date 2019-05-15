package io.pivotal.pcc.demo.client.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.pivotal.pcc.demo.client.domain.Book;
import io.pivotal.pcc.demo.client.services.BookService;
import io.pivotal.pcc.demo.client.services.CacheService;

@Path("/")
public class BookController {

	private final static CacheService cacheService = new CacheService();
	private final static BookService bookService = new BookService(cacheService.getBookRegion());

	@GET
	@Path("/ping")
	public Response ping() {
		return Response.ok().entity("Service online").build();
	}

	@GET
	@Path("/books/{isbn}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getBook(@PathParam("isbn") String isbn) {
		long start = System.currentTimeMillis();
		Book book = bookService.getBook(isbn);
		long end = System.currentTimeMillis();
		return Response.ok()
			.entity(String
				.format("It took: %d millis to execute getBook: %s for ISBN: %s", (end - start), book.toString(), isbn))
			.build();
	}
}
