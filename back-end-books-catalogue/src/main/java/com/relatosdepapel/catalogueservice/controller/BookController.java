package com.relatosdepapel.catalogueservice.controller;

import com.relatosdepapel.catalogueservice.dto.BookRequest;
import com.relatosdepapel.catalogueservice.dto.BookResponse;
import com.relatosdepapel.catalogueservice.entity.Book;
import com.relatosdepapel.catalogueservice.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.relatosdepapel.catalogueservice.dto.PaginatedResponse;
import org.springframework.data.domain.Page;
import java.util.Set;
@RestController
@RequestMapping("/api/books")
public class BookController {

    private static final int MAX_PAGE_SIZE = 100;

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public BookResponse create(@Valid @RequestBody BookRequest request) {
        Book created = bookService.create(request);
        return new BookResponse(created);
    }
    @GetMapping
    public PaginatedResponse<BookResponse> findAll(
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

        String safeSortBy = resolveSortField(sortBy);

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                safePage - 1,
                safeLimit,
                Sort.by(direction, safeSortBy)
        );

        Page<Book> result = bookService.findAll(
                title,
                author,
                publicationDate,
                category,
                isbn,
                rating,
                visible,
                pageable
        );
        List<BookResponse> rows = result.getContent()
                .stream()
                .map(BookResponse::new)
                .toList();
        return new PaginatedResponse<>(
                rows,
                result.getTotalElements(),
                safePage,
                safeLimit,
                result.getTotalPages(),
                result.hasNext(),
                result.hasPrevious()
        );
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
            @RequestBody Book book
    ) {
        Book updated = bookService.partialUpdate(externalId, book);
        return new BookResponse(updated);
    }

    @DeleteMapping("/{externalId}")
    public void delete(@PathVariable UUID externalId) {
        bookService.delete(externalId);
    }
    private String resolveSortField(String sortBy) {
        Set<String> allowedFields = Set.of(
                "title",
                "author",
                "publicationDate",
                "category",
                "isbn",
                "rating",
                "visible",
                "stock",
                "price",
                "createdAt"
        );

        if (sortBy == null || !allowedFields.contains(sortBy)) {
            return "title";
        }

        return sortBy;
    }
}