package com.relatosdepapel.catalogueservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AggregationDetails {

    private String key;
    private Integer count;
    private String uri;
}