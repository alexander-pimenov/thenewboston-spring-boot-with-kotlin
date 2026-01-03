package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Тесты граничных случаев и обработки ошибок
 */
public class EdgeCasesTest {
    @Test
    void testMessageWithSpecialCharacters() {
        // Given
        String textWithSpecialChars = "Сообщение с кириллицей и emoji 😀 и спецсимволами: @#$%";
        Message message = new Message(textWithSpecialChars, Channel.SMS, "user1");

        // Then
        assertEquals(textWithSpecialChars, message.text());
        // Проверяем, что UUID генерируется корректно (не падает с исключением)
        assertDoesNotThrow(() ->
                java.util.UUID.nameUUIDFromBytes(textWithSpecialChars.getBytes()));
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        // Given
        UserSettingsService settingsService = mock(UserSettingsService.class);
        MessageHistoryService historyService = mock(MessageHistoryService.class);

        when(settingsService.getUserSettings(anyString()))
                .thenReturn(Optional.of(new UserSettings(
                        "user",
                        Set.of(Channel.SMS),
                        java.time.Instant.now()
                )));
        when(historyService.hasDuplicateMessage(anyString(), any(Message.class), anyInt()))
                .thenReturn(false);

        MessageFilterService service = new MessageFilterService(settingsService, historyService);

        int threadCount = 10;
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger();

        // When - запускаем несколько потоков одновременно
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                Message message = new Message("Message from thread", Channel.SMS, "user");
                List<ProcessingResult> results = service.processMessages("sender", List.of(message));
                if (results.get(0).status() == ProcessingResult.Status.SENT) {
                    successCount.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        // Then - только одно сообщение должно быть отправлено, остальные - дубликаты
        assertEquals(1, successCount.get());
    }

    @Test
    void testLargeNumberOfMessages() {
        // Given
        UserSettingsService settingsService = mock(UserSettingsService.class);
        MessageHistoryService historyService = mock(MessageHistoryService.class);

        when(settingsService.getUserSettings(anyString()))
                .thenReturn(Optional.of(new UserSettings(
                        "user",
                        Set.of(Channel.EMAIL),
                        java.time.Instant.now()
                )));
        when(historyService.hasDuplicateMessage(anyString(), any(Message.class), anyInt()))
                .thenReturn(false);

        MessageFilterService service = new MessageFilterService(settingsService, historyService);

        // Создаем 1000 сообщений
        java.util.List<Message> messages = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            messages.add(new Message("Message " + i, Channel.EMAIL, "user"));
        }

        // When
        long startTime = System.currentTimeMillis();
        List<ProcessingResult> results = service.processMessages("bulk-sender", messages);
        long endTime = System.currentTimeMillis();

        // Then
        assertEquals(100, results.size());
        assertTrue((endTime - startTime) < 5000, "Обработка должна быть быстрой");

        // Проверяем, что все сообщения обработаны
        long sentCount = results.stream()
                .filter(processingResult -> processingResult.status() == ProcessingResult.Status.SENT)
                .count();
        assertEquals(1, sentCount, "Только первое сообщение должно быть отправлено");
    }
}
