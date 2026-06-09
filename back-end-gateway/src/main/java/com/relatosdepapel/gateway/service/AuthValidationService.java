package com.relatosdepapel.gateway.service;

import com.relatosdepapel.gateway.dto.ValidateTokenRequest;
import com.relatosdepapel.gateway.dto.ValidateTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthValidationService {

    private final WebClient webClient;

    public Mono<Boolean> validateToken(String token) {

        return webClient.post()
                .uri("http://users-service:8080/api/v1/auth/validate")
                .bodyValue(new ValidateTokenRequest(token))
                .retrieve()
                .bodyToMono(ValidateTokenResponse.class)
                .map(ValidateTokenResponse::isValid);
    }
}