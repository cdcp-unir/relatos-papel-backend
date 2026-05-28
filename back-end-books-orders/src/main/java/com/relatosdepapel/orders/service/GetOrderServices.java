package com.relatosdepapel.orders.service;

import com.relatosdepapel.orders.controller.model.GetOrdersResponseDto;
import com.relatosdepapel.orders.controller.model.PurchasedItem;
import com.relatosdepapel.orders.controller.model.RecentOrder;
import com.relatosdepapel.orders.facade.BooksCatalogueFacade;
import com.relatosdepapel.orders.facade.model.BookDto;
import com.relatosdepapel.orders.repository.OrderJpaRepository;
import com.relatosdepapel.orders.repository.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetOrderServices {

    private final OrderJpaRepository orderJpaRepository;
    private final BooksCatalogueFacade booksCatalogueFacade;

    @Transactional
    public GetOrdersResponseDto getOrderByOwnerId(Integer ownerId) {

        List<Order> orders = orderJpaRepository.findByOwnerIdOrderByOrderDateDesc(ownerId);

        List<RecentOrder> recentOrders = orders.stream()
                .map(order -> {
                    List<PurchasedItem> purchasedItems = order.getItems().stream()
                            .map(requestedBook -> {
                                BookDto bookDto = booksCatalogueFacade.getBook(requestedBook.getExternalId());
                                return PurchasedItem.builder()
                                        .name(bookDto != null ? bookDto.getTitle() : "Título no disponible")
                                        .quantity(requestedBook.getQuantity())
                                        .price(bookDto != null ? bookDto.getPrice() : BigDecimal.valueOf(0))
                                        .build();
                            })
                            .toList();

                    return RecentOrder.builder()
                            .id(order.getId())
                            .date(order.getOrderDate())
                            .status(order.getStatus())
                            .total(order.getTotal())
                            .comment(order.getComment())
                            .items(purchasedItems)
                            .build();
                })
                .toList();

        return GetOrdersResponseDto.builder()
                .orders(recentOrders)
                .build();
    }
}
