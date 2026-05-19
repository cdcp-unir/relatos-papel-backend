package com.relatosdepapel.catalogueservice.dto;

import com.relatosdepapel.catalogueservice.entity.Book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class BookResponse {

    private UUID externalId;
    private String title;
    private String author;
    private LocalDate publicationDate;
    private String category;
    private String isbn;
    private Integer rating;
    private Boolean visible;
    private Integer stock;
    private BigDecimal price;
    private LocalDateTime createdAt;

    public BookResponse() {
    }

    public BookResponse(Book book) {
        this.externalId = book.getExternalId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.publicationDate = book.getPublicationDate();
        this.category = book.getCategory();
        this.isbn = book.getIsbn();
        this.rating = book.getRating();
        this.visible = book.getVisible();
        this.stock = book.getStock();
        this.price = book.getPrice();
        this.createdAt = book.getCreatedAt();
    }

    public UUID getExternalId() {
        return externalId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public String getCategory() {
        return category;
    }

    public String getIsbn() {
        return isbn;
    }

    public Integer getRating() {
        return rating;
    }

    public Boolean getVisible() {
        return visible;
    }

    public Integer getStock() {
        return stock;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}