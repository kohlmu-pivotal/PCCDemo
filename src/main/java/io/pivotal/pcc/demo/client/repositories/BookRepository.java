package io.pivotal.pcc.demo.client.repositories;

import org.springframework.data.repository.CrudRepository;

import io.pivotal.pcc.demo.client.domain.Book;

public interface BookRepository extends CrudRepository<Book, String> {

}
