package com.relatosdepapel.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateTokenResponse {

    private String jwt;

    private Boolean valid;

    private Integer userId;

    private String email;

    private String role;
}
