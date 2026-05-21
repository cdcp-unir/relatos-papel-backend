package com.relatosdepapel.orders.facade;

import com.relatosdepapel.orders.exception.BadOrderModificationException;
import com.relatosdepapel.orders.exception.InternalServerException;
import com.relatosdepapel.orders.exception.OrderNotFoundException;
import com.relatosdepapel.orders.facade.model.BookDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class BooksCatalogueFacade {

    private final WebClient.Builder webClientBuilder;

    @Value("${booksCatalogue.url}")
    private String suppliesCatalogueUrl;

    public BookDto getBook(Integer bookId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(suppliesCatalogueUrl + "/books/{id}", bookId)
                    .retrieve()
                    .bodyToMono(BookDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new OrderNotFoundException("Libro con ID " + bookId + " no encontrado", e);
        } catch (WebClientResponseException.InternalServerError e) {
            throw new InternalServerException("Error al obtener el libro con ID " + bookId, e);
        }
    }

    public void updateBookStock(Integer bookId, Integer stock) {
        try {
            webClientBuilder.build().patch()
                    .uri(suppliesCatalogueUrl + "/books/{id}", bookId)
                    .bodyValue(Map.of("stock", stock))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new OrderNotFoundException("Libro con ID " + bookId + " no encontrado", e);
        } catch (WebClientResponseException.BadRequest e) {
            throw new BadOrderModificationException("Solicitud inválida para actualizar el stock del libro con ID " + bookId, e);
        } catch (WebClientResponseException.InternalServerError e) {
            throw new InternalServerException("Error al actualizar el stock del libro con ID" + bookId, e);
        }
    }

}
