package com.relatosdepapel.gateway.service;

import com.relatosdepapel.gateway.dto.ValidateTokenRequest;
import com.relatosdepapel.gateway.dto.ValidateTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class AuthValidationService {

    private final WebClient.Builder builder;
    @Value("${services.users.url}")
    private String usersServiceUrl;

    public Mono<ValidateTokenResponse> validateToken(String token) {

        return builder.build().post()
                .uri(usersServiceUrl + "/api/v1/auth/validate")
                .bodyValue(new ValidateTokenRequest(token))
                .retrieve()
                .bodyToMono(ValidateTokenResponse.class);
    }
}