package com.relatosdepapel.catalogueservice.repository;

import com.relatosdepapel.catalogueservice.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByExternalId(UUID externalId);

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);
}