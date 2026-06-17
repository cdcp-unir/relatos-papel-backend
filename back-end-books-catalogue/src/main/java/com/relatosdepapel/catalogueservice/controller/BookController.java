package com.relatosdepapel.catalogueservice.controller;

import com.relatosdepapel.catalogueservice.dto.BookPatchRequest;
import com.relatosdepapel.catalogueservice.dto.BookRequest;
import com.relatosdepapel.catalogueservice.dto.BookResponse;
import com.relatosdepapel.catalogueservice.dto.PaginatedResponse;
import com.relatosdepapel.catalogueservice.entity.Book;
import com.relatosdepapel.catalogueservice.service.BookSearchService;
import com.relatosdepapel.catalogueservice.service.BookService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/books")
public class BookController {

    private static final int MAX_PAGE_SIZE = 100;

    private final BookService bookService;
    private final BookSearchService bookSearchService;

    public BookController(BookService bookService, BookSearchService bookSearchService) {
        this.bookService = bookService;
        this.bookSearchService = bookSearchService;
    }

    @PostMapping
    public BookResponse create(@Valid @RequestBody BookRequest request) {
        Book created = bookService.create(request);
        return new BookResponse(created);
    }

    @GetMapping
    public PaginatedResponse<BookResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publicationDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean visible,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        int safePage = Math.max(page, 1);
        int safeLimit = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);

        return bookSearchService.searchPaginated(
                search,
                title,
                author,
                category,
                isbn,
                rating,
                visible,
                safePage,
                safeLimit
        );
    }

    @PostMapping("/elastic/rebuild")
    public String rebuildSearchIndex(
            @RequestParam(defaultValue = "false") boolean recreateIndex
    ) {
        bookSearchService.rebuildIndex(recreateIndex);

        if (recreateIndex) {
            return "Índice de libros recreado y reconstruido correctamente";
        }

        return "Índice de libros reconstruido correctamente";
    }

    @GetMapping("/{externalId}")
    public BookResponse findByExternalId(@PathVariable UUID externalId) {
        Book book = bookService.findByExternalId(externalId);
        return new BookResponse(book);
    }

    @PutMapping("/{externalId}")
    public BookResponse update(
            @PathVariable UUID externalId,
            @Valid @RequestBody BookRequest request
    ) {
        Book updated = bookService.update(externalId, request);
        return new BookResponse(updated);
    }

    @PatchMapping("/{externalId}")
    public BookResponse partialUpdate(
            @PathVariable UUID externalId,
            @Valid @RequestBody BookPatchRequest request
    ) {
        Book updated = bookService.partialUpdate(externalId, request);
        return new BookResponse(updated);
    }

    @DeleteMapping("/{externalId}")
    public void delete(@PathVariable UUID externalId) {
        bookService.delete(externalId);
    }
}