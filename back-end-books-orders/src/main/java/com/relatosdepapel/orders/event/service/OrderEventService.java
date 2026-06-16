package com.relatosdepapel.orders.event;

import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

public class OrderEventService {
    @Value("${rabbitmq.exchange.orders}")
    private String ordersExchange;

    @Value("${rabbitmq.routing.key.order.created}")
    private String orderCreatedRoutingKey;


    public void PublishOrderCreatedEvent(Order order){
        try {

        }catch (Exception ex){

        }

    }

    private OrderCreatedEvent buildOrderCreatedEvent(Order order){
        String eventID = UUID.randomUUID().toString();

        EventHeader header = Even
    }
}
