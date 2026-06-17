package com.relatosdepapel.catalogueservice.service;

import com.relatosdepapel.catalogueservice.dto.BookResponse;
import com.relatosdepapel.catalogueservice.dto.BookSearchResult;
import com.relatosdepapel.catalogueservice.dto.PaginatedResponse;
import com.relatosdepapel.catalogueservice.entity.Book;
import com.relatosdepapel.catalogueservice.repository.BookRepository;
import com.relatosdepapel.catalogueservice.repository.elastic.BookSearchRepository;
import com.relatosdepapel.catalogueservice.repository.elastic.model.ElasticBook;
import com.relatosdepapel.catalogueservice.utils.ElasticBookMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookSearchService {

    private final BookRepository bookRepository;
    private final BookSearchRepository bookSearchRepository;
    private final ElasticBookMapper elasticBookMapper;

    public BookSearchResult findWithAggregations(
            String title,
            String author,
            String category,
            String isbn,
            Integer minRating,
            Integer minStock,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer pageSize,
            Integer page
    ) {
        boolean hasFilters =
                StringUtils.hasLength(title)
                        || StringUtils.hasLength(author)
                        || StringUtils.hasLength(category)
                        || StringUtils.hasLength(isbn)
                        || minRating != null
                        || minStock != null
                        || minPrice != null
                        || maxPrice != null;

        if (hasFilters) {
            return bookSearchRepository.findByComplexQueryWithAggregations(
                    title,
                    author,
                    category,
                    isbn,
                    minRating,
                    minStock,
                    minPrice,
                    maxPrice,
                    pageSize,
                    page
            );
        }

        return bookSearchRepository.findAllWithAggregations(pageSize, page);
    }

    public List<BookResponse> find(
            String title,
            String author,
            String category,
            String isbn,
            Integer minRating,
            Integer minStock,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer pageSize,
            Integer page
    ) {
        boolean hasFilters =
                StringUtils.hasLength(title)
                        || StringUtils.hasLength(author)
                        || StringUtils.hasLength(category)
                        || StringUtils.hasLength(isbn)
                        || minRating != null
                        || minStock != null
                        || minPrice != null
                        || maxPrice != null;

        List<ElasticBook> books;

        if (hasFilters) {
            books = bookSearchRepository.findByComplexQuery(
                    title,
                    author,
                    category,
                    isbn,
                    minRating,
                    minStock,
                    minPrice,
                    maxPrice,
                    pageSize,
                    page
            );
        } else {
            books = bookSearchRepository.findAll(pageSize, page);
        }

        return elasticBookMapper.mapToBookResponseList(books);
    }

    public void rebuildIndex() {
        rebuildIndex(false);
    }

    public void rebuildIndex(boolean recreateIndex) {
        if (recreateIndex) {
            bookSearchRepository.recreateBooksIndex();
        } else {
            bookSearchRepository.ensureBooksIndex();
        }

        List<Book> books = bookRepository.findAll();

        for (Book book : books) {
            indexBook(book);
        }
    }
    public void indexBook(Book book) {
        if (book == null || book.getExternalId() == null) {
            log.warn("No se puede indexar el libro porque es nulo o no tiene externalId");
            return;
        }

        try {
            ElasticBook elasticBook = ElasticBook.builder()
                    .id(book.getExternalId().toString())
                    .externalId(book.getExternalId())
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .publicationDate(book.getPublicationDate())
                    .category(book.getCategory())
                    .isbn(book.getIsbn())
                    .rating(book.getRating())
                    .visible(book.getVisible())
                    .stock(book.getStock())
                    .price(book.getPrice())
                    .build();

            bookSearchRepository.save(elasticBook);

            log.info("Libro {} sincronizado con OpenSearch", book.getExternalId());
        } catch (Exception e) {
            log.error(
                    "Error sincronizando libro {} con OpenSearch: {}",
                    book.getExternalId(),
                    e.getMessage(),
                    e
            );
        }
    }

    public void deleteBook(Book book) {
        if (book == null || book.getExternalId() == null) {
            log.warn("No se puede eliminar el libro de OpenSearch porque es nulo o no tiene externalId");
            return;
        }

        try {
            bookSearchRepository.deleteById(book.getExternalId().toString());

            log.info("Libro {} eliminado de OpenSearch", book.getExternalId());
        } catch (Exception e) {
            log.error(
                    "Error eliminando libro {} de OpenSearch: {}",
                    book.getExternalId(),
                    e.getMessage(),
                    e
            );
        }
    }
    public PaginatedResponse<BookResponse> searchPaginated(
            String search,
            String title,
            String author,
            String category,
            String isbn,
            Integer rating,
            Boolean visible,
            int page,
            int limit
    ) {
        int safePage = Math.max(page, 1);
        int safeLimit = Math.max(limit, 1);
        int elasticPage = safePage - 1;

        String finalTitle = title;

        if (search != null && !search.trim().isEmpty()
                && (title == null || title.trim().isEmpty())) {
            finalTitle = search.trim();
        }

        BookSearchResult result = findWithAggregations(
                finalTitle,
                author,
                category,
                isbn,
                rating,
                null,
                null,
                null,
                safeLimit,
                elasticPage
        );

        List<BookResponse> rows = elasticBookMapper.mapToBookResponseList(result.getBooks());

        long total = result.getTotalHits() == null ? 0L : result.getTotalHits();

        int totalPages = safeLimit == 0
                ? 0
                : (int) Math.ceil((double) total / safeLimit);

        return new PaginatedResponse<>(
                rows,
                total,
                safePage,
                safeLimit,
                totalPages,
                safePage < totalPages,
                safePage > 1,
                result.getAggregations()
        );
    }
}