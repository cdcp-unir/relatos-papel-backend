package com.relatosdepapel.comms_service.events.listener;

import com.relatosdepapel.comms_service.events.model.OrderCreatedEvent;
import com.relatosdepapel.comms_service.events.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    private final EmailService emailService;

    @RabbitListener(queues = "mail.orden.creada")
    public void handlerOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            log.info("Recibido evento de pedido creado: {} - EventId {}", event.getBody().getOrderName(), event.getHeader().getEventId());

            emailService.sendOrderCreatedNotification(event);

            log.info("Evento procesado exitosamente para el pedido: {}", event.getBody().getOrderName());
        } catch (Exception ex) {
            log.info("Error al procesar el evento: {} - EventId {}", event.getBody().getOrderName(), event.getHeader().getEventId());
            throw ex;
        }
    }
}
