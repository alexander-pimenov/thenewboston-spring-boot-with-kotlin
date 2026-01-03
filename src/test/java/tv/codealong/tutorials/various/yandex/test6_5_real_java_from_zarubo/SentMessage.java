package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import java.time.Instant;

/**
 * Модель данных - отправленные сообщения
 */
public record SentMessage(
        String messageId,
        String userId,
        String text,
        Channel channel,
        Instant sentAt
) {}
