package com.relatosdepapel.catalogueservice.utils;

import com.relatosdepapel.catalogueservice.dto.BookResponse;
import com.relatosdepapel.catalogueservice.repository.elastic.model.ElasticBook;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElasticBookMapper {

    public BookResponse mapToBookResponse(ElasticBook elasticBook) {
        BookResponse response = new BookResponse();

        response.setExternalId(elasticBook.getExternalId());
        response.setTitle(elasticBook.getTitle());
        response.setAuthor(elasticBook.getAuthor());
        response.setPublicationDate(elasticBook.getPublicationDate());
        response.setCategory(elasticBook.getCategory());
        response.setIsbn(elasticBook.getIsbn());
        response.setRating(elasticBook.getRating());
        response.setVisible(elasticBook.getVisible());
        response.setStock(elasticBook.getStock());
        response.setPrice(elasticBook.getPrice());

        return response;
    }

    public List<BookResponse> mapToBookResponseList(List<ElasticBook> elasticBooks) {
        return elasticBooks.stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }
}