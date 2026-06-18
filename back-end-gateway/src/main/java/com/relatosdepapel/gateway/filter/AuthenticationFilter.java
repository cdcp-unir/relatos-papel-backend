package com.relatosdepapel.gateway.filter;

import com.relatosdepapel.gateway.service.AuthValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final AuthValidationService authValidationService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthenticationFilter(AuthValidationService authValidationService) {
        this.authValidationService = authValidationService;
    }

    @Override
    public Mono<Void> filter( ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || token.isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return authValidationService.validateToken(token)
                .flatMap(response -> {

                    if (!response.isValid()) {
                        exchange.getResponse()
                                .setStatusCode(HttpStatus.UNAUTHORIZED);

                        return exchange.getResponse()
                                .setComplete();
                    }

                    ServerHttpRequest request = exchange.getRequest()
                            .mutate()
                            .header("X-User-Id",
                                    response.getUserId().toString())
                            .build();

                    return chain.filter(
                            exchange.mutate()
                                    .request(request)
                                    .build()
                    );
                });
    }

    private boolean isPublicEndpoint(String path) {
        return pathMatcher.match("/users-service/api/v1/auth/token", path)
                || pathMatcher.match("/users-service/api/v1/auth/refresh", path)
                || pathMatcher.match("/users-service/api/v1/auth/validate", path)
                || pathMatcher.match("/users-service/api/v1/users/register", path)
                || pathMatcher.match("/books-service/api/v1/books/**", path);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}