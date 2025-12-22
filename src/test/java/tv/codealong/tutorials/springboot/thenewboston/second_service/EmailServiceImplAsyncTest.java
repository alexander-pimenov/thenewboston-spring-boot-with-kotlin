package tv.codealong.tutorials.springboot.thenewboston.second_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

/**
 * тестировать реальную асинхронную логику
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplAsyncTest {

    @Mock
    private KafkaTemplate<String, EmailMessage> kafkaTemplate;

    @Test
    void testWithMockCompletableFuture() throws Exception {
        // Arrange
        EmailServiceImpl service = new EmailServiceImpl(kafkaTemplate);

        // Устанавливаем поля через reflection
        var field = EmailServiceImpl.class.getDeclaredField("emailTopic");
        field.setAccessible(true);
        field.set(service, "test-topic");

        // Создаем успешный CompletableFuture
        SendResult<String, EmailMessage> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, EmailMessage>> future =
                CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any(EmailMessage.class)))
                .thenReturn(future);

        // Act
        service.sendWelcomeEmail("test@example.com");

        // Даем время на выполнение
        Thread.sleep(100);

        // Assert
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }
}
