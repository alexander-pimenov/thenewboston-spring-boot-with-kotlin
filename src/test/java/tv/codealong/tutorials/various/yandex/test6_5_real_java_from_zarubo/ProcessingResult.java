package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import java.time.Instant;

/**
 * Результат обработки сообщения
 */
public record ProcessingResult(
        Message message,
        Status status,
        String reason,
        Instant processedAt
) {
    public enum Status {
        SENT,       // Сообщение отправлено
        REJECTED,   // Сообщение отклонено (настройки)
        DUPLICATE,  // Сообщение дубликат
        FAILED      // Ошибка при отправке
    }

    // Фабричные методы для удобного создания результатов
    public static ProcessingResult sent(Message message, String reason) {
        return new ProcessingResult(message, Status.SENT, reason, Instant.now());
    }

    public static ProcessingResult rejected(Message message, String reason) {
        return new ProcessingResult(message, Status.REJECTED, reason, Instant.now());
    }

    public static ProcessingResult duplicate(Message message, String reason) {
        return new ProcessingResult(message, Status.DUPLICATE, reason, Instant.now());
    }

    public static ProcessingResult failed(Message message, String reason) {
        return new ProcessingResult(message, Status.FAILED, reason, Instant.now());
    }
}
