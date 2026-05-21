package com.relatosdepapel.catalogueservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookPatchRequest {

    private String title;
    private String author;
    private LocalDate publicationDate;
    private String category;
    private String isbn;

    @Min(value = 1, message = "La valoracion minima es 1")
    @Max(value = 5, message = "La valoracion maxima es 5")
    private Integer rating;

    private Boolean visible;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

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