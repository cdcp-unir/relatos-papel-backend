package com.relatosdepapel.catalogueservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookRequest {

    @NotBlank(message = "El titulo es obligatorio")
    private String title;

    @NotBlank(message = "El autor es obligatorio")
    private String author;

    private LocalDate publicationDate;

    @NotBlank(message = "La categoria es obligatoria")
    private String category;

    @NotBlank(message = "El ISBN es obligatorio")
    private String isbn;

    @NotNull(message = "La valoracion es obligatoria")
    @Min(value = 1, message = "La valoracion minima es 1")
    @Max(value = 5, message = "La valoracion maxima es 5")
    private Integer rating;

    private Boolean visible = true;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    private BigDecimal price;

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
}