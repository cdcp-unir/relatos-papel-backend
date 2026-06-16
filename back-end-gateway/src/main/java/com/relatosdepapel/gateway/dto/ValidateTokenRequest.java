package com.relatosdepapel.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ValidateTokenRequest {

    private String token;
}