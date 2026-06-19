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
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest originalRequest = exchange.getRequest();

        String path = originalRequest.getURI().getPath();

        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(originalRequest);

        if (token == null || token.isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return authValidationService.validateToken(token)
                .flatMap(response -> {
                    if (!response.isValid()) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    ServerHttpRequest request = originalRequest
                            .mutate()
                            .header("X-User-Id", response.getUserId().toString())
                            /*
                             * Importante para WebSocket:
                             * si el token vino como query param access_token,
                             * lo reinyectamos como Authorization para que el comms-service
                             * también pueda leerlo si lo necesita.
                             */
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .build();

                    return chain.filter(
                            exchange.mutate()
                                    .request(request)
                                    .build()
                    );
                })
                .onErrorResume(error -> {
                    log.error("Error validando token en gateway: {}", error.getMessage(), error);

                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private String resolveToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        if (isCommsWebSocketEndpoint(request.getURI().getPath())) {
            String accessToken = request.getQueryParams().getFirst("access_token");

            if (accessToken != null && !accessToken.isBlank()) {
                return accessToken.trim();
            }
        }

        return null;
    }

    private boolean isCommsWebSocketEndpoint(String path) {
        return pathMatcher.match("/comms-service/ws/chat/**", path);
    }

    private boolean isPublicEndpoint(String path) {
        return pathMatcher.match("/users-service/api/v1/auth/token", path)
                || pathMatcher.match("/users-service/api/v1/auth/refresh", path)
                || pathMatcher.match("/users-service/api/v1/auth/validate", path)
                || pathMatcher.match("/users-service/api/v1/users/register", path)
                || pathMatcher.match("/catalogue-service/api/v1/books/**", path);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}