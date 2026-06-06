package com.relatosdepapel.gateway.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateTokenResponse {

    private String jwt;
    private boolean valid;
}