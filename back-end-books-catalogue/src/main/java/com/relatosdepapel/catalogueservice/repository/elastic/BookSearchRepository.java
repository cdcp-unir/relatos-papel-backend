package com.relatosdepapel.catalogueservice.repository.elastic;

import com.relatosdepapel.catalogueservice.dto.AggregationDetails;
import com.relatosdepapel.catalogueservice.dto.BookSearchResult;
import com.relatosdepapel.catalogueservice.repository.elastic.model.ElasticBook;
import lombok.RequiredArgsConstructor;
import org.opensearch.data.client.orhlc.NativeSearchQuery;
import org.opensearch.data.client.orhlc.NativeSearchQueryBuilder;
import org.opensearch.data.client.orhlc.OpenSearchAggregations;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.Aggregation;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookSearchRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public BookSearchResult findAllWithAggregations(Integer pageSize, Integer page) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withPageable(PageRequest.of(page, pageSize))
                .withAggregations(
                        AggregationBuilders
                                .terms("categories")
                                .field("category")
                                .size(100)
                                .missing("Sin categoría")
                )
                .build();

        SearchHits<ElasticBook> searchHits = elasticsearchOperations.search(
                searchQuery,
                ElasticBook.class
        );

        List<ElasticBook> books = searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        List<AggregationDetails> aggregations = extractCategoryAggregations(searchHits);

        return BookSearchResult.builder()
                .books(books)
                .totalHits(searchHits.getTotalHits())
                .aggregations(aggregations)
                .build();
    }

    public List<ElasticBook> findAll(Integer pageSize, Integer page) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withPageable(PageRequest.of(page, pageSize))
                .build();

        SearchHits<ElasticBook> searchHits = elasticsearchOperations.search(searchQuery, ElasticBook.class);

        return searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    public Optional<ElasticBook> findById(String id) {
        return Optional.ofNullable(elasticsearchOperations.get(id, ElasticBook.class));
    }

    public void save(ElasticBook book) {
        elasticsearchOperations.save(book);
    }

    public void deleteById(String id) {
        elasticsearchOperations.delete(id, ElasticBook.class);
    }

    public BookSearchResult findByComplexQueryWithAggregations(
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
        BoolQueryBuilder boolQuery = buildBookQuery(
                title,
                author,
                category,
                isbn,
                minRating,
                minStock,
                minPrice,
                maxPrice
        );

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, pageSize))
                .withAggregations(
                        AggregationBuilders
                                .terms("categories")
                                .field("category")
                                .size(100)
                                .missing("Sin categoría")
                )
                .build();

        SearchHits<ElasticBook> searchHits = elasticsearchOperations.search(
                searchQuery,
                ElasticBook.class
        );

        List<ElasticBook> books = searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        List<AggregationDetails> aggregations = extractCategoryAggregations(searchHits);

        return BookSearchResult.builder()
                .books(books)
                .totalHits(searchHits.getTotalHits())
                .aggregations(aggregations)
                .build();
    }

    public List<ElasticBook> findByComplexQuery(
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
        BoolQueryBuilder boolQuery = buildBookQuery(
                title,
                author,
                category,
                isbn,
                minRating,
                minStock,
                minPrice,
                maxPrice
        );

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, pageSize))
                .build();

        SearchHits<ElasticBook> searchHits = elasticsearchOperations.search(searchQuery, ElasticBook.class);

        return searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    private BoolQueryBuilder buildBookQuery(
            String title,
            String author,
            String category,
            String isbn,
            Integer minRating,
            Integer minStock,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        /*
         * BÚSQUEDA GLOBAL
         *
         * En tu flujo actual, el parámetro "search" del frontend llega a este método
         * como "title", porque en BookSearchService haces:
         *
         * finalTitle = search
         *
         * Por eso aquí usamos "title" como texto global para buscar por:
         * - título
         * - autor
         * - categoría
         * - ISBN
         */
        if (title != null && !title.trim().isEmpty()) {
            String searchText = title.trim();

            BoolQueryBuilder globalSearch = QueryBuilders.boolQuery();

            /*
             * Título con search_as_you_type.
             * Permite búsquedas parciales como:
             * "cien", "fahren", "quijo", "amor"
             */
            globalSearch.should(
                    QueryBuilders.multiMatchQuery(
                                    searchText,
                                    "title",
                                    "title._2gram",
                                    "title._3gram"
                            )
                            .type(MultiMatchQueryBuilder.Type.BOOL_PREFIX)
            );

            /*
             * Autor flexible.
             * Permite buscar por:
             * "Gabriel", "García", "Bradbury", "Cervantes"
             */
            globalSearch.should(
                    QueryBuilders.matchQuery("author", searchText)
            );

            /*
             * Categoría exacta.
             *
             * Como tu mapping tiene category como keyword:
             *
             * "category": { "type": "keyword" }
             *
             * entonces funciona mejor con coincidencia exacta:
             * "Novela", "Tecnología", "Ciencia ficción"
             */
            globalSearch.should(
                    QueryBuilders.termQuery("category", searchText)
            );

            /*
             * ISBN exacto.
             */
            globalSearch.should(
                    QueryBuilders.termQuery("isbn", searchText)
            );

            /*
             * Debe coincidir al menos una de las búsquedas anteriores.
             */
            globalSearch.minimumShouldMatch(1);

            boolQuery.must(globalSearch);
        }

        /*
         * FILTRO ESPECÍFICO POR AUTOR
         *
         * Se usa cuando mandas:
         * /api/v1/books?author=Gabriel
         */
        if (author != null && !author.trim().isEmpty()) {
            boolQuery.must(
                    QueryBuilders.matchQuery("author", author.trim())
            );
        }

        /*
         * FILTRO ESPECÍFICO POR CATEGORÍA
         *
         * Se usa cuando das clic en el sidebar:
         * /api/v1/books?category=Novela
         */
        if (category != null && !category.trim().isEmpty()) {
            boolQuery.must(
                    QueryBuilders.termQuery("category", category.trim())
            );
        }

        /*
         * FILTRO ESPECÍFICO POR ISBN
         */
        if (isbn != null && !isbn.trim().isEmpty()) {
            boolQuery.must(
                    QueryBuilders.termQuery("isbn", isbn.trim())
            );
        }

        /*
         * FILTRO POR RATING MÍNIMO
         */
        if (minRating != null) {
            boolQuery.must(
                    QueryBuilders.rangeQuery("rating").gte(minRating)
            );
        }

        /*
         * FILTRO POR STOCK MÍNIMO
         */
        if (minStock != null) {
            boolQuery.must(
                    QueryBuilders.rangeQuery("stock").gte(minStock)
            );
        }

        /*
         * FILTRO POR PRECIO MÍNIMO
         */
        if (minPrice != null) {
            boolQuery.must(
                    QueryBuilders.rangeQuery("price").gte(minPrice)
            );
        }

        /*
         * FILTRO POR PRECIO MÁXIMO
         */
        if (maxPrice != null) {
            boolQuery.must(
                    QueryBuilders.rangeQuery("price").lte(maxPrice)
            );
        }

        /*
         * Solo libros visibles en catálogo.
         */
        boolQuery.must(
                QueryBuilders.termQuery("visible", true)
        );

        return boolQuery;
    }

    private List<AggregationDetails> extractCategoryAggregations(SearchHits<ElasticBook> searchHits) {
        OpenSearchAggregations aggregations = (OpenSearchAggregations) searchHits.getAggregations();

        if (aggregations == null) {
            return List.of();
        }

        Map<String, Aggregation> aggs = Objects.requireNonNull(aggregations)
                .aggregations()
                .asMap();

        ParsedStringTerms categoriesAgg = (ParsedStringTerms) aggs.get("categories");

        if (categoriesAgg == null) {
            return List.of();
        }

        return categoriesAgg.getBuckets()
                .stream()
                .map(bucket -> {
                    String category = bucket.getKeyAsString();

                    String encodedCategory = java.net.URLEncoder.encode(
                            category,
                            java.nio.charset.StandardCharsets.UTF_8
                    );

                    AggregationDetails details = new AggregationDetails();
                    details.setKey(category);
                    details.setCount((int) bucket.getDocCount());
                    details.setUri("/api/v1/books?category=" + encodedCategory);

                    return details;
                })
                .collect(Collectors.toList());
    }

    public void ensureBooksIndex() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(ElasticBook.class);

        if (!indexOperations.exists()) {
            indexOperations.create();
            indexOperations.putMapping(buildBooksMapping());
        }
    }

    public void recreateBooksIndex() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(ElasticBook.class);

        if (indexOperations.exists()) {
            indexOperations.delete();
        }

        indexOperations.create();
        indexOperations.putMapping(buildBooksMapping());
    }

    private Document buildBooksMapping() {
        return Document.parse("""
                {
                  "properties": {
                    "id": {
                      "type": "keyword"
                    },
                    "externalId": {
                      "type": "keyword"
                    },
                    "title": {
                      "type": "search_as_you_type"
                    },
                    "author": {
                      "type": "text",
                      "fields": {
                        "keyword": {
                          "type": "keyword"
                        }
                      }
                    },
                    "publicationDate": {
                      "type": "date",
                      "format": "yyyy-MM-dd||strict_date_optional_time||epoch_millis"
                    },
                    "category": {
                      "type": "keyword"
                    },
                    "isbn": {
                      "type": "keyword"
                    },
                    "rating": {
                      "type": "integer"
                    },
                    "visible": {
                      "type": "boolean"
                    },
                    "stock": {
                      "type": "integer"
                    },
                    "price": {
                      "type": "double"
                    }
                  }
                }
                """);
    }
}