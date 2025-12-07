package com.logisco.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private String sender;
    private String content;
    private String room = "public";
    private LocalDateTime timestamp = LocalDateTime.now();
}
