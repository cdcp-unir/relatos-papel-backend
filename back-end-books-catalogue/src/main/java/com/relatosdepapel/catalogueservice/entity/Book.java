package com.relatosdepapel.catalogueservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, updatable = false)
    private UUID externalId;

    @NotBlank(message = "El titulo es obligatorio")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "El autor es obligatorio")
    @Column(nullable = false, length = 150)
    private String author;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @NotBlank(message = "La categoria es obligatoria")
    @Column(nullable = false, length = 100)
    private String category;

    @NotBlank(message = "El ISBN es obligatorio")
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Min(value = 1, message = "La valoracion minima es 1")
    @Max(value = 5, message = "La valoracion maxima es 5")
    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false)
    private Boolean visible = true;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock = 0;

    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Book() {
    }

    @PrePersist
    public void prePersist() {
        if (this.externalId == null) {
            this.externalId = UUID.randomUUID();
        }

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.visible == null) {
            this.visible = true;
        }

        if (this.stock == null) {
            this.stock = 0;
        }

        if (this.price == null) {
            this.price = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setExternalId(UUID externalId) {
        this.externalId = externalId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}