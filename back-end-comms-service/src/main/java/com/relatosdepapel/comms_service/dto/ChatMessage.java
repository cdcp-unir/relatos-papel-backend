package com.relatosdepapel.comms_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private String userId;
    private String message;
    private MessageType type;
    private LocalDateTime timestamp;
    private String sender;

    public enum MessageType {
        USER_MESSAGE,
        BOT_RESPONSE,
        SYSTEM_MESSAGE
    }
}