package com.relatosdepapel.gateway.proxy.orders;

import com.relatosdepapel.gateway.proxy.orders.model.GetOrdersByOwnerProxyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/proxy/orders")
@RequiredArgsConstructor
public class OrdersProxyController {

    private static final String ORDERS_SERVICE_BASE_URL = "http://orders-service/api/v1/orders";

    private final WebClient.Builder webClientBuilder;

    /**
     * Frontend:
     * POST /proxy/orders/by-owner
     *
     * Gateway transcribe a:
     * GET /api/v1/orders/owner/{ownerId}
     */
    @PostMapping("/recent")
    public Mono<ResponseEntity<String>> getOrdersByOwner(
            @RequestBody GetOrdersByOwnerProxyRequest request
    ) {
        return webClientBuilder.build()
                .get()
                .uri(ORDERS_SERVICE_BASE_URL + "/users/{ownerId}/recent", request.getOwnerId())
                .retrieve()
                .toEntity(String.class);
    }

    /**
     * Frontend:
     * POST /proxy/orders/create
     *
     * Gateway transcribe a:
     * POST /api/v1/orders
     */
    @PostMapping("/create")
    public Mono<ResponseEntity<String>> createOrder(
            @RequestBody Object request
    ) {
        return webClientBuilder.build()
                .post()
                .uri(ORDERS_SERVICE_BASE_URL)
                .bodyValue(request)
                .retrieve()
                .toEntity(String.class);
    }
}