package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты для MessageHistoryService с реальной реализацией
 */
@ExtendWith(MockitoExtension.class)
public class MessageHistoryServiceTest {

    @Test
    void testHasDuplicateMessage_Implementation() {
        // Given - создаем реальную реализацию для тестирования логики
        MessageHistoryService service = new MessageHistoryService() {
            private final Set<SentMessage> history = Set.of(
                    new SentMessage("1", "user1", "Hello", Channel.SMS,
                            Instant.now().minus(30, ChronoUnit.MINUTES)),
                    new SentMessage("2", "user1", "World", Channel.EMAIL,
                            Instant.now().minus(90, ChronoUnit.MINUTES))
            );

            @Override
            public Set<SentMessage> getSentMessages(String userId, int limit) {
                return history.stream()
                        .filter(sentMsg -> sentMsg.userId().equals(userId))
                        .limit(limit)
                        .collect(java.util.stream.Collectors.toSet());
            }

            @Override
            public Set<SentMessage> getSentMessages(String userId, Instant fromDate, Instant toDate) {
                return history.stream()
                        .filter(sentMsg -> sentMsg.userId().equals(userId)
                                && sentMsg.sentAt().isAfter(fromDate)
                                && sentMsg.sentAt().isBefore(toDate))
                        .collect(java.util.stream.Collectors.toSet());
            }

            @Override
            public boolean hasDuplicateMessage(String userId, Message message, int timeWindow) {
                Instant timeAgo = Instant.now().minus(timeWindow, ChronoUnit.MINUTES);
                return history.stream()
                        .anyMatch(sentMsg -> sentMsg.userId().equals(userId)
                                && sentMsg.text().equals(message.text())
                                && sentMsg.channel() == message.channel()
                                && sentMsg.sentAt().isAfter(timeAgo));
            }
        };

        // When & Then
        Message duplicateMessage = new Message("Hello", Channel.SMS, "user1");
        Message newMessage = new Message("New", Channel.SMS, "user1");

        assertTrue(service.hasDuplicateMessage("user1", duplicateMessage, 60));
        assertFalse(service.hasDuplicateMessage("user1", duplicateMessage, 20)); // 30 минут назад, окно 20 - не дубликат
        assertFalse(service.hasDuplicateMessage("user1", newMessage, 60));
        assertFalse(service.hasDuplicateMessage("user2", duplicateMessage, 60)); // Другой пользователь
    }
}
