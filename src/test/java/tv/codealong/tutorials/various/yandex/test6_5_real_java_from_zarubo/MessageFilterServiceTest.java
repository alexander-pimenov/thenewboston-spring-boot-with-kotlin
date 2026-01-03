package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для MessageFilterService
 */
@ExtendWith(MockitoExtension.class)
public class MessageFilterServiceTest {
    @Mock
    private UserSettingsService userSettingsService;

    @Mock
    private MessageHistoryService messageHistoryService;

    private MessageFilterService messageFilterService;

    private final String testSender = "test-sender";
    private final String testUserId = "user-123";
    private final Message testMessage = new Message("Test message", Channel.SMS, testUserId);
    private final UserSettings testUserSettings = new UserSettings(
            testUserId,
            Set.of(Channel.SMS, Channel.EMAIL),
            Instant.now()
    );

    @BeforeEach
    void setUp() {
        messageFilterService = new MessageFilterService(userSettingsService, messageHistoryService);
    }

    @Test
    void processMessages_WithEmptyCollection_ReturnsEmptyList() {
        // When
        List<ProcessingResult> results = messageFilterService.processMessages(testSender, List.of());

        // Then
        assertTrue(results.isEmpty());
        verifyNoInteractions(userSettingsService);
        verifyNoInteractions(messageHistoryService);
    }

    @Test
    void processMessages_WhenUserNotFound_ReturnsRejected() {
        // Given
        when(userSettingsService.getUserSettings(testUserId)).thenReturn(Optional.empty());

        // When
        List<ProcessingResult> results = messageFilterService.processMessages(
                testSender, List.of(testMessage));

        // Then
        assertEquals(1, results.size());
        ProcessingResult result = results.get(0);
        assertEquals(ProcessingResult.Status.REJECTED, result.status());
        assertTrue(result.reason().contains("User not found"));
        verify(userSettingsService).getUserSettings(testUserId);
        verifyNoInteractions(messageHistoryService);
    }

    @Test
    void processMessages_WhenChannelNotAllowed_ReturnsRejected() {
        // Given
        Message pushMessage = new Message("Test", Channel.PUSH, testUserId);
        when(userSettingsService.getUserSettings(testUserId))
                .thenReturn(Optional.of(testUserSettings));

        // When
        List<ProcessingResult> results = messageFilterService.processMessages(
                testSender, List.of(pushMessage));

        // Then
        assertEquals(1, results.size());
        ProcessingResult result = results.get(0);
        assertEquals(ProcessingResult.Status.REJECTED, result.status());
        assertTrue(result.reason().contains("not allowed"));
        verify(userSettingsService).getUserSettings(testUserId);
    }

    @Test
    void processMessages_WhenDuplicateInHistory_ReturnsDuplicate() {
        // Given
        when(userSettingsService.getUserSettings(testUserId))
                .thenReturn(Optional.of(testUserSettings));
        when(messageHistoryService.hasDuplicateMessage(
                eq(testUserId), any(Message.class), anyInt()))
                .thenReturn(true);

        // When
        List<ProcessingResult> results = messageFilterService.processMessages(
                testSender, List.of(testMessage));

        // Then
        assertEquals(1, results.size());
        ProcessingResult result = results.get(0);
        assertEquals(ProcessingResult.Status.DUPLICATE, result.status());
        assertTrue(result.reason().contains("Duplicate found"));
        verify(messageHistoryService).hasDuplicateMessage(
                eq(testUserId), any(Message.class), eq(60));
    }

    @Test
    void processMessages_WhenValidMessage_ReturnsSent() {
        // Given
        when(userSettingsService.getUserSettings(testUserId))
                .thenReturn(Optional.of(testUserSettings));
        when(messageHistoryService.hasDuplicateMessage(
                eq(testUserId), any(Message.class), anyInt()))
                .thenReturn(false);

        // When
        List<ProcessingResult> results = messageFilterService.processMessages(
                testSender, List.of(testMessage));

        // Then
        assertEquals(1, results.size());
        ProcessingResult result = results.get(0);
        assertEquals(ProcessingResult.Status.SENT, result.status());
        assertTrue(result.reason().contains("successfully"));
    }

    @Test
    void processMessages_WithMultipleMessages_ProcessesAll() {
        // Given
        Message message1 = new Message("Message 1", Channel.SMS, testUserId);
        Message message2 = new Message("Message 2", Channel.EMAIL, testUserId);
        Message message3 = new Message("Message 3", Channel.PUSH, "user-456");

        UserSettings user2Settings = new UserSettings(
                "user-456",
                Set.of(Channel.PUSH),
                Instant.now()
        );

        when(userSettingsService.getUserSettings(testUserId))
                .thenReturn(Optional.of(testUserSettings));
        when(userSettingsService.getUserSettings("user-456"))
                .thenReturn(Optional.of(user2Settings));
        when(messageHistoryService.hasDuplicateMessage(anyString(), any(Message.class), anyInt()))
                .thenReturn(false);

        // When
        List<ProcessingResult> results = messageFilterService.processMessages(
                testSender, List.of(message1, message2, message3));

        // Then
        assertEquals(3, results.size());
        long sentCount = results.stream()
                .filter(r -> r.status() == ProcessingResult.Status.SENT)
                .count();
        assertEquals(3, sentCount);
    }

    @Test
    void processMessages_WhenDuplicateInSameSession_ReturnsDuplicate() {
        // Given
        when(userSettingsService.getUserSettings(testUserId))
                .thenReturn(Optional.of(testUserSettings));
        when(messageHistoryService.hasDuplicateMessage(
                eq(testUserId), any(Message.class), anyInt()))
                .thenReturn(false);

        Message duplicateMessage = new Message("Test message", Channel.SMS, testUserId);

        // When - отправляем одинаковые сообщения в одной сессии
        List<ProcessingResult> results = messageFilterService.processMessages(
                testSender, List.of(testMessage, duplicateMessage));

        // Then
        assertEquals(2, results.size());
        assertEquals(ProcessingResult.Status.SENT, results.get(0).status());
        assertEquals(ProcessingResult.Status.DUPLICATE, results.get(1).status());
        assertTrue(results.get(1).reason().contains("current session"));
    }

    @Test
    void clearCacheForUser_WhenCalled_RemovesUserFromCache() {
        // Given
        when(userSettingsService.getUserSettings(testUserId))
                .thenReturn(Optional.of(testUserSettings));
        when(messageHistoryService.hasDuplicateMessage(
                eq(testUserId), any(Message.class), anyInt()))
                .thenReturn(false);

        // Первая отправка
        messageFilterService.processMessages(testSender, List.of(testMessage));

        // When - очищаем кэш
        messageFilterService.clearCacheForUser(testUserId);

        // Then - сообщение можно отправить снова
        List<ProcessingResult> results = messageFilterService.processMessages(
                testSender, List.of(testMessage));

        assertEquals(ProcessingResult.Status.SENT, results.get(0).status());
    }

    @Test
    void processMessages_WithNullCollection_ReturnsEmptyList() {
        // When
        List<ProcessingResult> results = messageFilterService.processMessages(testSender, null);

        // Then
        assertTrue(results.isEmpty());
        verifyNoInteractions(userSettingsService);
        verifyNoInteractions(messageHistoryService);
    }

    @Test
    void testIsChannelAllowedDefaultMethod() {
        // Given
        UserSettingsService customService = new UserSettingsService() {
            @Override
            public Optional<UserSettings> getUserSettings(String userId) {
                if (testUserId.equals(userId)) {
                    return Optional.of(testUserSettings);
                }
                return Optional.empty();
            }
        };

        // When & Then
        assertTrue(customService.isChannelAllowed(testUserId, Channel.SMS));
        assertFalse(customService.isChannelAllowed(testUserId, Channel.PUSH));
        assertFalse(customService.isChannelAllowed("non-existent", Channel.SMS));
    }

    //Тест для проверки потокобезопасности: TODO - пока не работает, нужно разобраться.
    @Test
    void testThreadSafetyWithoutLock() throws InterruptedException {
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

        // When - запускаем 10 потоков, пытающихся обработать одинаковые сообщения
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger sentCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                Message message = new Message("Same message", Channel.SMS, "user");
                List<ProcessingResult> results = service.processMessagesOptimized("test", List.of(message));
                if (results.get(0).status() == ProcessingResult.Status.SENT) {
                    sentCount.incrementAndGet();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        // Then - должно быть отправлено только одно сообщение
        assertEquals(1, sentCount.get(),
                "Только одно сообщение должно быть отправлено, остальные - дубликаты");
    }

}
