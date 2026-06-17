package com.relatosdepapel.catalogueservice.repository.elastic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Document(indexName = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElasticBook {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private UUID externalId;

    @Field(type = FieldType.Search_As_You_Type)
    private String title;

    @Field(type = FieldType.Text)
    private String author;

    @Field(type = FieldType.Date)
    private LocalDate publicationDate;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String isbn;

    @Field(type = FieldType.Integer)
    private Integer rating;

    @Field(type = FieldType.Boolean)
    private Boolean visible;

    @Field(type = FieldType.Integer)
    private Integer stock;

    @Field(type = FieldType.Double)
    private BigDecimal price;
}