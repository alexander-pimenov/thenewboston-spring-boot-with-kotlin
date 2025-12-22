package tv.codealong.tutorials.springboot.thenewboston.second_service;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для Kafka сообщений
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {
    private String messageId;
    private String type; // "WELCOME", "PASSWORD_RESET", "NOTIFICATION"
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> templateData;
    private LocalDateTime timestamp;

    public enum EmailType {
        WELCOME,
        PASSWORD_RESET,
        NOTIFICATION
    }
}