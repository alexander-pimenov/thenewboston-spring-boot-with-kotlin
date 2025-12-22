package tv.codealong.tutorials.springboot.thenewboston.second_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final KafkaTemplate<String, EmailMessage> emailKafkaTemplate;

    @Value("${spring.kafka.topics.email:email-events}")
    private String emailTopic;

    @Value("${spring.kafka.topics.notification:notification-events}")
    private String notificationTopic;

    public EmailServiceImpl(KafkaTemplate<String, EmailMessage> emailKafkaTemplate,
                            @Value("${kafka.topics.email}") String emailTopic,
                            @Value("${kafka.topics.notification}") String notificationTopic) {
        this.emailKafkaTemplate = emailKafkaTemplate;
        this.emailTopic = emailTopic;
        this.notificationTopic = notificationTopic;
    }

    @Override
    @Async
    public void sendWelcomeEmail(String email) {
        log.info("Sending welcome email to Kafka for user: {}", email);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("username", extractUsernameFromEmail(email));
        templateData.put("welcomeMessage", "Добро пожаловать в наше приложение!");

        EmailMessage message = EmailMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(EmailMessage.EmailType.WELCOME.name())
                .to(email)
                .subject("Добро пожаловать!")
                .templateName("welcome-template")
                .templateData(templateData)
                .timestamp(LocalDateTime.now())
                .build();

        sendToKafka(emailTopic, message, "welcome-email");
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String email, String token) {
        log.info("Sending password reset email to Kafka for user: {}", email);

        String resetLink = "https://example.com/reset-password?token=" + token;

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("resetLink", resetLink);
        templateData.put("token", token);
        templateData.put("expirationTime", "24 часа");

        EmailMessage message = EmailMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(EmailMessage.EmailType.PASSWORD_RESET.name())
                .to(email)
                .subject("Сброс пароля")
                .templateName("password-reset-template")
                .templateData(templateData)
                .timestamp(LocalDateTime.now())
                .build();

        sendToKafka(emailTopic, message, "password-reset");
    }

    @Override
    @Async
    public void sendNotification(String email, String subject, String messageContent) {
        log.info("Sending notification to Kafka: {}, subject: {}", email, subject);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("content", messageContent);
        templateData.put("notificationType", "GENERAL");

        EmailMessage message = EmailMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(EmailMessage.EmailType.NOTIFICATION.name())
                .to(email)
                .subject(subject)
                .templateName("notification-template")
                .templateData(templateData)
                .timestamp(LocalDateTime.now())
                .build();

        sendToKafka(notificationTopic, message, "notification");
    }

    private void sendToKafka(String topic, EmailMessage message, String key) {
        try {
            CompletableFuture<SendResult<String, EmailMessage>> future =
                    emailKafkaTemplate.send(topic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Message sent successfully to topic {}: {}", topic,
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send message to topic {}: {}", topic, ex.getMessage(), ex);
                    // Здесь можно добавить логику повторной отправки или сохранения в БД
                }
            });
        } catch (Exception e) {
            log.error("Error sending message to Kafka topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to send email via Kafka", e);
        }
    }

    private String extractUsernameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "Пользователь";
        }
        return email.substring(0, email.indexOf("@"));
    }

    // Дополнительные методы для работы с Kafka

    @Async
    public void sendRawEmailEvent(String eventType, String email, Map<String, Object> data) {
        EmailMessage message = EmailMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(eventType)
                .to(email)
                .templateData(data)
                .timestamp(LocalDateTime.now())
                .build();

        sendToKafka(emailTopic, message, eventType + "-" + email);
    }

//    public CompletableFuture<SendResult<String, EmailMessage>> sendWithCallback(
//            String topic, EmailMessage message) {
//
//        return emailKafkaTemplate.send(topic, message.getMessageId(), message)
//                .completable()
//                .whenComplete((result, ex) -> {
//                    if (ex != null) {
//                        log.error("Failed to send message: {}", ex.getMessage());
//                    }
//                });
//    }
}