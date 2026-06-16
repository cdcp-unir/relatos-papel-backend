package com.relatosdepapel.orders.event.service;

import com.relatosdepapel.orders.event.model.EventBody;
import com.relatosdepapel.orders.event.model.EventHeader;
import com.relatosdepapel.orders.event.model.OrderCreatedEvent;
import com.relatosdepapel.orders.event.model.OrderItemEvent;
import com.relatosdepapel.orders.repository.model.Order;
import com.relatosdepapel.orders.repository.model.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.orders}")
    private String ordersExchange;

    @Value("${rabbitmq.routing.key.order.created}")
    private String orderCreatedRoutingKey;


    public void PublishOrderCreatedEvent(OrderItem order) {
        try {
            OrderCreatedEvent event = buildOrderCreatedEvent(order);
           rabbitTemplate.convertAndSend(ordersExchange, orderCreatedRoutingKey, event);
           log.info("Evento de pedido creado publicado exitosamente. Order:{}, EventId: {}",
            order.getOrder().getName(), event.getHeader().getEventId());
        } catch (Exception ex) {
            log.error("Error al publicar evento de pedido creado para order: {}", order.getOrder().getName(), ex);
        }

    }

    private OrderCreatedEvent buildOrderCreatedEvent(OrderItem order) {
        String eventID = UUID.randomUUID().toString();

        EventHeader header = EventHeader.builder()
                .eventId(eventID)
                .version("1.0")
                .eventType("ORDER_CREATED")
                .timestamp(LocalDateTime.now())
                .build();

        List<OrderItemEvent> orderItemEvents = order.getOrder().getItems().stream()
                .map(this::mapToOrderItemEvent)
                .toList();

        EventBody body = EventBody.builder()
                .orderItems(orderItemEvents)
                .orderName(order.getOrder().getName())
                .orderDate(order.getOrder().getOrderDate())
                .total(order.getOrder().getTotal())
                .build();

        return OrderCreatedEvent.builder()
                .body(body)
                .header(header)
                .build();
    }

    private OrderItemEvent mapToOrderItemEvent(OrderItem orderItem) {
        return OrderItemEvent.builder()
                .id(orderItem.getId())
                .quantity(orderItem.getQuantity())
                .subTotal(orderItem.getSubTotal())
                .build();
    }
}
