package com.relatosdepapel.catalogueservice.service;

import com.relatosdepapel.catalogueservice.dto.BookRequest;
import com.relatosdepapel.catalogueservice.entity.Book;
import com.relatosdepapel.catalogueservice.exception.BadRequestException;
import com.relatosdepapel.catalogueservice.exception.ResourceNotFoundException;
import com.relatosdepapel.catalogueservice.repository.BookRepository;
import com.relatosdepapel.catalogueservice.repository.BookSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BadRequestException("Ya existe un libro con el ISBN: " + request.getIsbn());
        }

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublicationDate(request.getPublicationDate());
        book.setCategory(request.getCategory());
        book.setIsbn(request.getIsbn());
        book.setRating(request.getRating());
        book.setVisible(request.getVisible());
        book.setStock(request.getStock());
        book.setPrice(request.getPrice());

        return bookRepository.save(book);
    }

    public Page<Book> findAll(
            String title,
            String author,
            LocalDate publicationDate,
            String category,
            String isbn,
            Integer rating,
            Boolean visible,
            Pageable pageable
    ) {
        return bookRepository.findAll(
                BookSpecification.filter(
                        title,
                        author,
                        publicationDate,
                        category,
                        isbn,
                        rating,
                        visible
                ),
                pageable
        );
    }

    public Book findByExternalId(UUID externalId) {
        return bookRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con externalId: " + externalId));
    }

    public Book update(UUID externalId, BookRequest request) {
        Book book = findByExternalId(externalId);

        if (!book.getIsbn().equals(request.getIsbn()) && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BadRequestException("Ya existe otro libro con el ISBN: " + request.getIsbn());
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublicationDate(request.getPublicationDate());
        book.setCategory(request.getCategory());
        book.setIsbn(request.getIsbn());
        book.setRating(request.getRating());
        book.setVisible(request.getVisible());
        book.setStock(request.getStock());
        book.setPrice(request.getPrice());

        return bookRepository.save(book);
    }

    public Book partialUpdate(UUID externalId, Book data) {
        Book book = findByExternalId(externalId);

        if (data.getTitle() != null) {
            book.setTitle(data.getTitle());
        }

        if (data.getAuthor() != null) {
            book.setAuthor(data.getAuthor());
        }

        if (data.getPublicationDate() != null) {
            book.setPublicationDate(data.getPublicationDate());
        }

        if (data.getCategory() != null) {
            book.setCategory(data.getCategory());
        }

        if (data.getIsbn() != null) {
            if (!book.getIsbn().equals(data.getIsbn()) && bookRepository.existsByIsbn(data.getIsbn())) {
                throw new BadRequestException("Ya existe otro libro con el ISBN: " + data.getIsbn());
            }

            book.setIsbn(data.getIsbn());
        }

        if (data.getRating() != null) {
            book.setRating(data.getRating());
        }

        if (data.getVisible() != null) {
            book.setVisible(data.getVisible());
        }

        if (data.getStock() != null) {
            book.setStock(data.getStock());
        }

        if (data.getPrice() != null) {
            book.setPrice(data.getPrice());
        }

        return bookRepository.save(book);
    }

    public void delete(UUID externalId) {
        Book book = findByExternalId(externalId);
        bookRepository.delete(book);
    }
}