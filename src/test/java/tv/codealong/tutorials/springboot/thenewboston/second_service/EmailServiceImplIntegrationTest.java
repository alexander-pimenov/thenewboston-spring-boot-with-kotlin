package tv.codealong.tutorials.springboot.thenewboston.second_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Вариант с использованием @SpyBean в Spring Boot тесте
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=localhost:9092",
        "spring.kafka.topics.email=test-email-topic",
        "spring.kafka.topics.notification=test-notification-topic"
})
class EmailServiceImplIntegrationTest {

    @Autowired
    private EmailServiceImpl emailService;

    @SpyBean
    private KafkaTemplate<String, EmailMessage> kafkaTemplate;

    @Test
    void sendWelcomeEmail_integrationTest() {
        // Arrange
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        doReturn(future).when(kafkaTemplate)
                .send(anyString(), anyString(), any(EmailMessage.class));

        // Act
        emailService.sendWelcomeEmail("test@example.com");

        // Assert
        verify(kafkaTemplate, timeout(5000).times(1))
                .send(eq("test-email-topic"), anyString(), any(EmailMessage.class));
    }
}
