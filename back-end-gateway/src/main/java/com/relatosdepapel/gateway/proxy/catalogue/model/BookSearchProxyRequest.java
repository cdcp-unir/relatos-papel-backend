package com.relatosdepapel.gateway.proxy.catalogue.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookSearchProxyRequest {

    private String title;
    private String author;
    private LocalDate publicationDate;
    private String category;
    private String isbn;
    private Integer rating;
    private Boolean visible;

    private Integer page;
    private Integer limit;
    private String sortBy;
    private String sortDirection;
}