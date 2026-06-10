package com.relatosdepapel.orders.service;

import com.relatosdepapel.orders.controller.model.*;
import com.relatosdepapel.orders.facade.BooksCatalogueFacade;
import com.relatosdepapel.orders.facade.model.BookDto;
import com.relatosdepapel.orders.repository.OrderJpaRepository;
import com.relatosdepapel.orders.repository.model.Order;
import com.relatosdepapel.orders.repository.model.OrderItem;
import com.relatosdepapel.orders.repository.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CreateOrderService {
    private final OrderJpaRepository orderJpaRepository;
    private final BooksCatalogueFacade booksCatalogueFacade;

    @Transactional
    public CreateOrderResponseDto CreateOrder(CreateOrderRequestDto requestDto, Integer userId) {
        List<RequestedBook> requestedBookList = requestDto.getItems();

        // validar que existan libros en la petición
        if (requestedBookList == null || requestedBookList.isEmpty()) {
            throw new IllegalArgumentException("La orden de compra debe tener almenos un libro");
        }

        BigDecimal totalAmount = new BigDecimal(0);
        List<OrderItem> orderItemList = new ArrayList<>();
        Map<UUID, BookDto> bookDtoMap = new HashMap<>();

        // Recuperar listado de libros
        for (RequestedBook requestedBook : requestedBookList) {
            BookDto bookDto = booksCatalogueFacade.getBook(requestedBook.getExternalId());
            bookDtoMap.put(bookDto.getExternalId(), bookDto);
        }

        for (RequestedBook requestedBook : requestedBookList) {
            BookDto bookDto = bookDtoMap.get(requestedBook.getExternalId());

            // validar que exista stock suficiente
            if (bookDto.getStock() < requestedBook.getQuantity()) {
                throw new IllegalArgumentException("Stock insuficiente para el libro con ID " + requestedBook.getExternalId());
            }

            BigDecimal subtotal = getSubtotal(bookDto.getPrice(), requestedBook.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .externalId(requestedBook.getExternalId())
                    .quantity(requestedBook.getQuantity())
                    .subTotal(subtotal)
                    .build();
            // acumular las ordenes listas
            orderItemList.add(orderItem);

            // acumular total
            totalAmount = totalAmount.add(subtotal);
        }

        Order order = Order.builder()
                .name("ORDER-" + System.currentTimeMillis())
                .orderDate(LocalDateTime.now())
                .total(totalAmount)
                .status(OrderStatus.EN_PROCESO)
                .ownerId(userId)
                .build();

        // Agregar items a la orden usando el método helper que mantiene la relación bidireccional
        for (OrderItem item : orderItemList) {
            order.addItem(item);
        }

        // guardar la orden generada
        orderJpaRepository.save(order);


        for (RequestedBook requestedBook : requestedBookList) {
            BookDto bookDto = bookDtoMap.get(requestedBook.getExternalId());
            int newStock = bookDto.getStock() - requestedBook.getQuantity();
            if (newStock < 0) {
                throw new IllegalArgumentException("Error crítico: el stock resultante sería negativo para el producto ID: " + requestedBook.getExternalId());
            }
            booksCatalogueFacade.updateBookStock(requestedBook.getExternalId(), newStock);

        }

        return CreateOrderResponseDto.builder()
                .name(order.getName())
                .build();
    }

    private BigDecimal getSubtotal(BigDecimal price, Integer quantity) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
