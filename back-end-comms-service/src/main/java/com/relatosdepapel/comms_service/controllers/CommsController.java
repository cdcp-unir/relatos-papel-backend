package com.relatosdepapel.comms_service.controllers;

import com.relatosdepapel.comms_service.dto.ChatMessage;
import com.relatosdepapel.comms_service.service.GeminiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CommsController {

    private final GeminiChatService geminiChatService;

    @MessageMapping("/chat/message")
    @SendTo("/topic/chat")
    public ChatMessage handleMessage(ChatMessage message) {

        ChatMessage response = new ChatMessage();

        response.setUserId(message.getUserId());
        response.setSender("RELATOS_BOT");
        response.setType(ChatMessage.MessageType.BOT_RESPONSE);
        response.setTimestamp(java.time.LocalDateTime.now());

        response.setMessage(
                geminiChatService.generateResponse(
                        message.getMessage()
                )
        );

        System.out.println("Mensaje recibido: " + message.getMessage());
        System.out.println("Usuario: " + message.getUserId());

        return response;
    }
}