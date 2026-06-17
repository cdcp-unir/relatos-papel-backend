package com.relatosdepapel.catalogueservice.dto;

import com.relatosdepapel.catalogueservice.repository.elastic.model.ElasticBook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BookSearchResult {

    private List<ElasticBook> books;
    private Long totalHits;
    private List<AggregationDetails> aggregations;
}