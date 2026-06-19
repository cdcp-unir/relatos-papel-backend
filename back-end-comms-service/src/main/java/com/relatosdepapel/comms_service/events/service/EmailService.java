package com.relatosdepapel.comms_service.events.service;

import com.relatosdepapel.comms_service.events.listener.OrderEventListener;
import com.relatosdepapel.comms_service.events.model.OrderCreatedEvent;
import com.relatosdepapel.comms_service.events.model.OrderItemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("michael3125@gmail.com")
    private String emailFrom;

    public void sendOrderCreatedNotification(OrderCreatedEvent event){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.getBody().getEmail());
            message.setFrom(emailFrom);
            message.setSubject("Nuevo pedido creado ");
            message.setText(buildEmailContent(event));
            mailSender.send(message);
            log.info("Notificacion Enviada por correo exitosamente para el pedido");
        }catch (Exception ex){
            log.error("No se envio la notificacion Enviada por correo exitosamente para el pedido", ex);
        }
    }

    private  String buildEmailContent(OrderCreatedEvent event){
        StringBuilder content = new StringBuilder();

        content.append("Se ha creado un nuevo pedido en el sistema.\n\n");
        content.append("Detalles del Pedido:\n");
        content.append("==================\n");
        content.append("Nombre del Pedido: ").append(event.getBody().getOrderName()).append("\n");
        content.append("ID del Propietario: ").append(event.getBody().getOwnerId()).append("\n");
        content.append("Fecha del Pedido: ").append(
                event.getBody().getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ).append("\n");
        content.append("Estado: ").append(event.getBody().getStatus()).append("\n");
        content.append("Total: ").append(
                NumberFormat.getCurrencyInstance(new Locale("es", "ES"))
                        .format(event.getBody().getTotal())
        ).append("\n\n");

        content.append("Artículos del Pedido:\n");
        content.append("====================\n");

        for (OrderItemEvent item : event.getBody().getOrderItems()) {
            content.append("- ID Catálogo: ").append(item.getId()).append("\n");
            content.append("  Cantidad: ").append(item.getQuantity()).append("\n");
            content.append("  Subtotal: ").append(
                    NumberFormat.getCurrencyInstance(new Locale("es", "ES"))
                            .format(item.getSubTotal())
            ).append("\n\n");
        }

        content.append("Información del Evento:\n");
        content.append("======================\n");
        content.append("ID del Evento: ").append(event.getHeader().getEventId()).append("\n");
        content.append("Tipo de Evento: ").append(event.getHeader().getEventType()).append("\n");
        content.append("Versión: ").append(event.getHeader().getVersion()).append("\n");
        content.append("Timestamp: ").append(
                event.getHeader().getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        ).append("\n\n");

        content.append("Este es un mensaje automático del sistema de suministros.\n");

        return content.toString();
    }
}
