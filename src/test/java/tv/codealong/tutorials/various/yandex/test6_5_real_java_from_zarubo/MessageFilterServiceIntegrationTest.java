package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Параметризованные тесты для различных сценариев
 */
public class MessageFilterServiceIntegrationTest {

    static Stream<Arguments> provideTestScenarios() {
        return Stream.of(
                // user1, channel, expected status
                Arguments.of("user1", Channel.SMS, ProcessingResult.Status.SENT),
                Arguments.of("user1", Channel.EMAIL, ProcessingResult.Status.SENT),
                Arguments.of("user1", Channel.PUSH, ProcessingResult.Status.REJECTED),
                Arguments.of("user2", Channel.SMS, ProcessingResult.Status.REJECTED) // user not found
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestScenarios")
    void testDifferentScenarios(String userId, Channel channel, ProcessingResult.Status expectedStatus) {
        // Given
        UserSettingsService settingsService = mock(UserSettingsService.class);
        MessageHistoryService historyService = mock(MessageHistoryService.class);

        MessageFilterService service = new MessageFilterService(settingsService, historyService);

        // Настройка моков
        if ("user1".equals(userId)) {
            UserSettings user1Settings = new UserSettings(
                    "user1",
                    Set.of(Channel.SMS, Channel.EMAIL),
                    java.time.Instant.now()
            );
            when(settingsService.getUserSettings("user1")).thenReturn(Optional.of(user1Settings));
        } else {
            when(settingsService.getUserSettings("user2")).thenReturn(Optional.empty());
        }

        when(historyService.hasDuplicateMessage(anyString(), any(Message.class), anyInt()))
                .thenReturn(false);

        Message message = new Message("Test", channel, userId);

        // When
        List<ProcessingResult> results = service.processMessages("test-sender", List.of(message));

        // Then
        assertEquals(1, results.size());
        assertEquals(expectedStatus, results.get(0).status());
    }

    @Test
    void testFullIntegrationScenario() {
        // Given - симуляция реального использования
        UserSettingsService realSettingsService = userId -> {
            if ("alice".equals(userId)) {
                return Optional.of(new UserSettings(
                        "alice",
                        Set.of(Channel.EMAIL, Channel.PUSH),
                        java.time.Instant.now()
                ));
            }
            return Optional.empty();
        };

        MessageHistoryService realHistoryService = new MessageHistoryService() {
            private final java.util.Set<SentMessage> storage =
                    new java.util.HashSet<>();

            @Override
            public java.util.Set<SentMessage> getSentMessages(String userId, int limit) {
                return storage.stream()
                        .filter(sentMessage -> sentMessage.userId().equals(userId))
                        .limit(limit)
                        .collect(java.util.stream.Collectors.toSet());
            }

            @Override
            public java.util.Set<SentMessage> getSentMessages(
                    String userId, java.time.Instant fromDate, java.time.Instant toDate) {
                return storage.stream()
                        .filter(sentMessage -> sentMessage.userId().equals(userId)
                                && sentMessage.sentAt().isAfter(fromDate)
                                && sentMessage.sentAt().isBefore(toDate))
                        .collect(java.util.stream.Collectors.toSet());
            }

            @Override
            public boolean hasDuplicateMessage(String userId, Message message, int timeWindow) {
                java.time.Instant timeAgo = java.time.Instant.now()
                        .minus(timeWindow, java.time.temporal.ChronoUnit.MINUTES);
                return storage.stream()
                        .anyMatch(sentMessage -> sentMessage.userId().equals(userId)
                                && sentMessage.text().equals(message.text())
                                && sentMessage.channel() == message.channel()
                                && sentMessage.sentAt().isAfter(timeAgo));
            }
        };

        MessageFilterService service = new MessageFilterService(
                realSettingsService, realHistoryService
        );

        // When - выполняем серию действий
        Message emailToAlice = new Message("Welcome", Channel.EMAIL, "alice");
        Message pushToAlice = new Message("Alert", Channel.PUSH, "alice");
        Message smsToAlice = new Message("SMS", Channel.SMS, "alice"); // Не разрешено
        Message emailToBob = new Message("Welcome", Channel.EMAIL, "bob"); // Пользователь не найден

        List<ProcessingResult> results = service.processMessages(
                "system",
                List.of(emailToAlice, pushToAlice, smsToAlice, emailToBob)
        );

        // Then - проверяем результаты
        assertEquals(4, results.size());

        // Индекс 0: emailToAlice - должно быть отправлено
        assertEquals(ProcessingResult.Status.SENT, results.get(0).status());

        // Индекс 1: pushToAlice - должно быть отправлено
        assertEquals(ProcessingResult.Status.SENT, results.get(1).status());

        // Индекс 2: smsToAlice - должно быть отклонено
        assertEquals(ProcessingResult.Status.REJECTED, results.get(2).status());
        assertTrue(results.get(2).reason().contains("not allowed"));

        // Индекс 3: emailToBob - должно быть отклонено
        assertEquals(ProcessingResult.Status.REJECTED, results.get(3).status());
        assertTrue(results.get(3).reason().contains("not found"));
    }
}
