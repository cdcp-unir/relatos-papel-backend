package com.relatosdepapel.orders.service;

import com.relatosdepapel.orders.controller.model.CreateOrderRequestDto;
import com.relatosdepapel.orders.controller.model.CreateOrderResponseDto;
import com.relatosdepapel.orders.controller.model.RequestedBook;
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
    public CreateOrderResponseDto CreateOrder(CreateOrderRequestDto requestDto) {
        List<RequestedBook> requestedBookList = requestDto.getItems();

        // validar que existan libros en la petición
        if (requestedBookList == null || requestedBookList.isEmpty()) {
            throw new IllegalArgumentException("La orden de compra debe tener almenos un libro");
        }

        BigDecimal totalAmount = new BigDecimal(0);
        List<OrderItem> orderItemList = new ArrayList<>();
        Map<Integer, BookDto> bookDtoMap = new HashMap<>();

        // Recuperar listado de libros
        for (RequestedBook requestedBook : requestedBookList) {
            BookDto bookDto = booksCatalogueFacade.getBook(requestedBook.getId());
            bookDtoMap.put(bookDto.getId(), bookDto);
        }

        for (RequestedBook requestedBook : requestedBookList) {
            BookDto bookDto = bookDtoMap.get(requestedBook.getId());

            // validar que exista stock suficiente
            if (bookDto.getStock() < requestedBook.getQuantity()) {
                throw new IllegalArgumentException("Stock insuficiente para el libro con ID " + requestedBook.getId());
            }
            OrderItem orderItem = OrderItem.builder()
                    .bookId(requestedBook.getId())
                    .quantity(requestedBook.getQuantity())
                    .subTotal(getSubtotal(bookDto.getPrice(), requestedBook.getQuantity()))
                    .build();
            // acumular las ordenes listas
            orderItemList.add(orderItem);

            // acumular total
            totalAmount = totalAmount.add(orderItem.getSubTotal());
        }


        Order order = Order.builder()
                .name("ORDER-" + System.currentTimeMillis())
                .orderDate(LocalDateTime.now())
                .items(orderItemList)
                .total(totalAmount)
                .status(OrderStatus.EN_PROCESO)
                .ownerId(1)// FIXME: cambiar por el usuario ID cuando exista autenticacion
                .build();

        // guardar la orden generada
        orderJpaRepository.save(order);


        for (RequestedBook requestedBook : requestedBookList) {
            BookDto bookDto = bookDtoMap.get(requestedBook.getId());
            int newStock = bookDto.getStock() - requestedBook.getQuantity();
            if (newStock < 0) {
                throw new IllegalArgumentException("Error crítico: el stock resultante sería negativo para el producto ID: " + requestedBook.getId());
            }
            booksCatalogueFacade.updateBookStock(requestedBook.getId(), newStock);

        }

        return CreateOrderResponseDto.builder()
                .name(order.getName())
                .build();
    }

    private BigDecimal getSubtotal(BigDecimal price, Integer quantity) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
