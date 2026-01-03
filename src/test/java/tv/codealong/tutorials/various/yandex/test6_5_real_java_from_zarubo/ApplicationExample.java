package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record.SafePrinter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Пример использования сервиса
 */
public class ApplicationExample {
    public static void main(String[] args) {
        // Создаем заглушки для внешних сервисов
        UserSettingsService settingsService = new UserSettingsService() {
            @Override
            public Optional<UserSettings> getUserSettings(String userId) {
                if ("user1".equals(userId)) {
                    return Optional.of(new UserSettings(
                            userId,
                            Set.of(Channel.SMS, Channel.EMAIL),
                            java.time.Instant.now()
                    ));
                }
                return Optional.empty();
            }
        };

        MessageHistoryService historyService = new MessageHistoryService() {
            @Override
            public boolean hasDuplicateMessage(String userId, Message message, int timeWindow) {
                // Проверка дубликатов в истории
                return false;
            }

            @Override
            public Set<SentMessage> getSentMessages(String userId, int limit) {
                return Set.of();
            }

            @Override
            public Set<SentMessage> getSentMessages(String userId,
                                                    java.time.Instant fromDate,
                                                    java.time.Instant toDate) {
                return Set.of();
            }
        };

        // Создаем сервис фильтрации
        MessageFilterService filterService = new MessageFilterService(
                settingsService, historyService
        );

        // Подготавливаем тестовые сообщения
        List<Message> messages = List.of(
                new Message("Ваш заказ готов", Channel.SMS, "user1"),
                new Message("Ваш заказ готов", Channel.EMAIL, "user1"), // Дубликат по содержанию
                new Message("Новое уведомление", Channel.PUSH, "user1"), // Канал не разрешен
                new Message("Приветствие", Channel.EMAIL, "user2") // Пользователь не найден
        );

        // Обрабатываем сообщения
        List<ProcessingResult> results = filterService.processMessages(
                "order-service", messages
        );

        // Анализируем результаты
        results.forEach(result -> {
            SafePrinter.printf("%s - %s: %s%n",
                    result.status(),
                    result.message().channel(),
                    result.reason()
            );
        });
    }
}
