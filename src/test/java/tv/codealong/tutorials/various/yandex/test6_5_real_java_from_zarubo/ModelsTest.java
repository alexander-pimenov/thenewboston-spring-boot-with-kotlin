package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для моделей данных (Record)
 */
public class ModelsTest {
    @Test
    void testMessageRecord() {
        // Given
        Message message = new Message("Test text", Channel.SMS, "user123");

        // Then
        assertEquals("Test text", message.text());
        assertEquals(Channel.SMS, message.channel());
        assertEquals("user123", message.receiver());

        // Test equals and hashCode
        Message sameMessage = new Message("Test text", Channel.SMS, "user123");
        Message differentMessage = new Message("Different", Channel.EMAIL, "user123");

        assertEquals(message, sameMessage);
        assertNotEquals(message, differentMessage);
        assertEquals(message.hashCode(), sameMessage.hashCode());
    }

    @Test
    void testUserSettingsRecord() {
        // Given
        Instant now = Instant.now();
        Set<Channel> allowedChannels = Set.of(Channel.SMS, Channel.EMAIL);
        UserSettings settings = new UserSettings("user123", allowedChannels, now);

        // Then
        assertEquals("user123", settings.userId());
        assertEquals(allowedChannels, settings.allowedChannels());
        assertEquals(now, settings.updatedAt());
        assertTrue(settings.allowedChannels().contains(Channel.SMS));
        assertFalse(settings.allowedChannels().contains(Channel.PUSH));
    }

    @Test
    void testProcessingResultFactoryMethods() {
        // Given
        Message message = new Message("Test", Channel.SMS, "user123");

        // When
        ProcessingResult sent = ProcessingResult.sent(message, "Success");
        ProcessingResult rejected = ProcessingResult.rejected(message, "Not allowed");
        ProcessingResult duplicate = ProcessingResult.duplicate(message, "Duplicate");
        ProcessingResult failed = ProcessingResult.failed(message, "Failed");

        // Then
        assertEquals(ProcessingResult.Status.SENT, sent.status());
        assertEquals(ProcessingResult.Status.REJECTED, rejected.status());
        assertEquals(ProcessingResult.Status.DUPLICATE, duplicate.status());
        assertEquals(ProcessingResult.Status.FAILED, failed.status());
        assertNotNull(sent.processedAt());
    }
}
