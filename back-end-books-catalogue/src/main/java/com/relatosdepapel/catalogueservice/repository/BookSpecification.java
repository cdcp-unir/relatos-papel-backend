package com.relatosdepapel.catalogueservice.repository;

import com.relatosdepapel.catalogueservice.entity.Book;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class BookSpecification {

    private BookSpecification() {
    }

    public static Specification<Book> filter(
            String search,
            String title,
            String author,
            LocalDate publicationDate,
            String category,
            String isbn,
            Integer rating,
            Boolean visible
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (search != null && !search.trim().isEmpty()) {
                String searchValue = "%" + search.toLowerCase().trim() + "%";

                var searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                searchValue
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("author")),
                                searchValue
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("category")),
                                searchValue
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("isbn")),
                                searchValue
                        )
                );

                predicates = criteriaBuilder.and(predicates, searchPredicate);
            }

            if (title != null && !title.trim().isEmpty()) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                "%" + title.toLowerCase().trim() + "%"
                        )
                );
            }

            if (author != null && !author.trim().isEmpty()) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("author")),
                                "%" + author.toLowerCase().trim() + "%"
                        )
                );
            }

            if (publicationDate != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(root.get("publicationDate"), publicationDate)
                );
            }

            if (category != null && !category.trim().isEmpty()) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("category")),
                                "%" + category.toLowerCase().trim() + "%"
                        )
                );
            }

            if (isbn != null && !isbn.trim().isEmpty()) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("isbn")),
                                "%" + isbn.toLowerCase().trim() + "%"
                        )
                );
            }

            if (rating != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(root.get("rating"), rating)
                );
            }

            if (visible != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(root.get("visible"), visible)
                );
            }

            return predicates;
        };
    }
}