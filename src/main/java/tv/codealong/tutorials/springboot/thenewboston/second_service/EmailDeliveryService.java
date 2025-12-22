package tv.codealong.tutorials.springboot.thenewboston.second_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Сервис для отслеживания отправки (опционально)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDeliveryService {

    @KafkaListener(
            topics = "${spring.kafka.topics.email:email-events}",
            groupId = "email-delivery-group",
            containerFactory = "emailListenerContainerFactory"
    )
    public void consumeEmailEvent(EmailMessage message, Acknowledgment ack) {
        try {
            log.info("Received email event: {} for user: {}", message.getType(), message.getTo());

            // Здесь логика обработки email:
            // 1. Отправка через SMTP (SendGrid, Amazon SES и т.д.)
            // 2. Логирование
            // 3. Обновление статуса в БД

            processEmail(message);

            ack.acknowledge(); // Подтверждаем обработку
        } catch (Exception e) {
            log.error("Failed to process email event: {}", message.getMessageId(), e);
            // Можно добавить логику повторной обработки или Dead Letter Queue
        }
    }

    private void processEmail(EmailMessage message) {
        // Реальная реализация отправки email
        switch (EmailMessage.EmailType.valueOf(message.getType())) {
            case WELCOME:
                sendWelcomeEmailImpl(message);
                break;
            case PASSWORD_RESET:
                sendPasswordResetEmailImpl(message);
                break;
            case NOTIFICATION:
                sendNotificationImpl(message);
                break;
            default:
                log.warn("Unknown email type: {}", message.getType());
        }
    }

    private void sendWelcomeEmailImpl(EmailMessage message) {
        // Реальная отправка welcome email
        log.info("Sending welcome email to: {}", message.getTo());
    }

    private void sendPasswordResetEmailImpl(EmailMessage message) {
        // Реальная отправка password reset email
        log.info("Sending password reset email to: {}", message.getTo());
    }

    private void sendNotificationImpl(EmailMessage message) {
        // Реальная отправка notification
        log.info("Sending notification to: {}", message.getTo());
    }
}