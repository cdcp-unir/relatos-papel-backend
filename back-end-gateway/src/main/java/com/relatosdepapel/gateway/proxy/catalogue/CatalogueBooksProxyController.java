package com.relatosdepapel.gateway.proxy.catalogue;

import com.relatosdepapel.gateway.proxy.catalogue.model.BookByIdProxyRequest;
import com.relatosdepapel.gateway.proxy.catalogue.model.BookSearchProxyRequest;
import com.relatosdepapel.gateway.proxy.catalogue.model.BookUpdateProxyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/proxy/catalogue/books")
@RequiredArgsConstructor
public class CatalogueBooksProxyController {

    private static final String CATALOGUE_SERVICE_BASE_URL = "http://catalogue-service/api/v1/books";

    private final WebClient.Builder webClientBuilder;

    @PostMapping("/search")
    public Mono<ResponseEntity<String>> search(@RequestBody BookSearchProxyRequest request) {
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("catalogue-service")
                        .path("/api/v1/books")
                        .queryParamIfPresent("title", Optional.ofNullable(request.getTitle()))
                        .queryParamIfPresent("author", Optional.ofNullable(request.getAuthor()))
                        .queryParamIfPresent("publicationDate", Optional.ofNullable(request.getPublicationDate()))
                        .queryParamIfPresent("category", Optional.ofNullable(request.getCategory()))
                        .queryParamIfPresent("isbn", Optional.ofNullable(request.getIsbn()))
                        .queryParamIfPresent("rating", Optional.ofNullable(request.getRating()))
                        .queryParamIfPresent("visible", Optional.ofNullable(request.getVisible()))
                        .queryParamIfPresent("page", Optional.ofNullable(request.getPage()))
                        .queryParamIfPresent("limit", Optional.ofNullable(request.getLimit()))
                        .queryParamIfPresent("sortBy", Optional.ofNullable(request.getSortBy()))
                        .queryParamIfPresent("sortDirection", Optional.ofNullable(request.getSortDirection()))
                        .build())
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/get")
    public Mono<ResponseEntity<String>> getByExternalId(@RequestBody BookByIdProxyRequest request) {
        return webClientBuilder.build()
                .get()
                .uri(CATALOGUE_SERVICE_BASE_URL + "/{externalId}", request.getExternalId())
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<String>> create(@RequestBody Object request) {
        return webClientBuilder.build()
                .post()
                .uri(CATALOGUE_SERVICE_BASE_URL)
                .bodyValue(request)
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/update")
    public Mono<ResponseEntity<String>> update(@RequestBody BookUpdateProxyRequest<Object> request) {
        return webClientBuilder.build()
                .put()
                .uri(CATALOGUE_SERVICE_BASE_URL + "/{externalId}", request.getExternalId())
                .bodyValue(request.getData())
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/patch")
    public Mono<ResponseEntity<String>> patch(@RequestBody BookUpdateProxyRequest<Object> request) {
        return webClientBuilder.build()
                .patch()
                .uri(CATALOGUE_SERVICE_BASE_URL + "/{externalId}", request.getExternalId())
                .bodyValue(request.getData())
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/delete")
    public Mono<ResponseEntity<Void>> delete(@RequestBody BookByIdProxyRequest request) {
        return webClientBuilder.build()
                .delete()
                .uri(CATALOGUE_SERVICE_BASE_URL + "/{externalId}", request.getExternalId())
                .retrieve()
                .toBodilessEntity();
    }
}