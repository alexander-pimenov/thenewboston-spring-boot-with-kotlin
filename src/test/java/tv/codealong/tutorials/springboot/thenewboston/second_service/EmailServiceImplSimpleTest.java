package tv.codealong.tutorials.springboot.thenewboston.second_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Упрощенный тест без асинхронности
 * Если не хотите работать с CompletableFuture в тестах
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplSimpleTest {

    @Mock
    private KafkaTemplate<String, EmailMessage> emailKafkaTemplate;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<EmailMessage> emailMessageCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "emailTopic", "email-events");
        ReflectionTestUtils.setField(emailService, "notificationTopic", "notification-events");

        // Мокаем send, чтобы возвращать успешный CompletableFuture
        CompletableFuture<Void> successfulFuture = CompletableFuture.completedFuture(null);
        when(emailKafkaTemplate.send(anyString(), anyString(), any(EmailMessage.class)))
                .thenReturn(mock(CompletableFuture.class));
    }

    @Test
    void sendWelcomeEmail_shouldPrepareCorrectMessage() {
        // Arrange
        String email = "test@example.com";

        // Act
        emailService.sendWelcomeEmail(email);

        // Даем время на асинхронное выполнение
        try { Thread.sleep(50); } catch (InterruptedException e) {}

        // Assert
        verify(emailKafkaTemplate, times(1))
                .send(eq("email-events"), anyString(), emailMessageCaptor.capture());

        EmailMessage message = emailMessageCaptor.getValue();
        assertThat(message.getTo()).isEqualTo(email);
        assertThat(message.getType()).isEqualTo("WELCOME");
        assertThat(message.getTemplateName()).isEqualTo("welcome-template");
        assertThat(message.getTemplateData())
                .containsKeys("username", "welcomeMessage");
    }
}
