package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import tv.codealong.tutorials.springboot.thenewboston.utils.SafePrinter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Сервис для фильтрации и отправки сообщений.
 * <p>
 * Почему в исходном коде lock можно и не использовать:
 * - ConcurrentHashMap.newKeySet() возвращает потокобезопасную реализацию Set
 * - computeIfAbsent() атомарно вычисляет значение, если ключ отсутствует
 * - add() на потокобезопасном Set также потокобезопасен
 * <p>
 * Отдельные операции get() и последующий add() могут создать гонку, но в текущей реализации:
 * - isAlreadyProcessed(): только чтение (безопасно)
 * - markAsProcessed(): атомарные операции через computeIfAbsent() + add()
 */
public class MessageFilterService {

    private final UserSettingsService userSettingsService;
    private final MessageHistoryService messageHistoryService;

    // Кэш для защиты от повторной отправки в рамках одного запуска
    private final Map<String, Set<String>> processedMessageCache = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    // Конфигурация
    private static final int DUPLICATE_CHECK_TIME_WINDOW_MINUTES = 60;
    private static final int MAX_HISTORY_CHECK = 100;

    // Синхронизация по receiver
    private final java.util.Map<String, Object> locks = new java.util.HashMap<>();

    /**
     * Важно: HashMap не потокобезопасен, поэтому доступ к нему обёрнут в synchronized (locks), чтобы избежать гонки при создании замков
     *
     * @param receiver
     * @return
     */
    private Object getLock(String receiver) {
        synchronized (locks) {
            return locks.computeIfAbsent(receiver, k -> new Object());
        }
    }

    public MessageFilterService(
            UserSettingsService userSettingsService,
            MessageHistoryService messageHistoryService) {
        this.userSettingsService = Objects.requireNonNull(userSettingsService);
        this.messageHistoryService = Objects.requireNonNull(messageHistoryService);
    }

    /**
     * Основной метод для фильтрации и отправки сообщений
     *
     * @param sender   отправитель (идентификатор системы/сервиса)
     * @param messages коллекция сообщений для обработки
     * @return результат обработки для каждого сообщения
     */
    public List<ProcessingResult> processMessages(String sender, Collection<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        return messages.stream()
                .map(message -> processSingleMessage(sender, message))
                .collect(Collectors.toList());
    }

    /**
     * Основной метод для фильтрации и отправки сообщений
     */
    public List<ProcessingResult> processMessagesOptimized(String sender, Collection<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        return messages.stream()
                .map(message -> processSingleMessage(sender, message))
                .collect(Collectors.toList());
    }

    /**
     * Основной метод для фильтрации и отправки сообщений (ThreadSafe)
     */
    public List<ProcessingResult> processMessagesThreadSafe(String sender, Collection<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        return messages.parallelStream() // Можем использовать parallelStream для параллельной обработки
                .map(message -> processSingleMessageThreadSafe(sender, message))
                .collect(Collectors.toList());
    }

    private ProcessingResult processSingleMessage(String sender, Message message) {
        String receiver = message.receiver();
        // Генерация уникального идентификатора сообщения
        String messageId = generateMessageId(sender, message);

        // 1. Проверка уникальности в рамках текущей сессии
        if (isAlreadyProcessed(receiver, messageId)) {
            return ProcessingResult.duplicate(message, "Message already processed in current session");
        }

        // 2. Проверка настроек пользователя
        Optional<UserSettings> userSettings = userSettingsService.getUserSettings(receiver);
        if (userSettings.isEmpty()) {
            return ProcessingResult.rejected(message, "User not found or has no settings");
        }

        if (!userSettings.get().allowedChannels().contains(message.channel())) {
            return ProcessingResult.rejected(message,
                    String.format("Channel %s is not allowed for user %s",
                            message.channel(), receiver));
        }

        // 3. Проверка истории на дубликаты
        if (messageHistoryService.hasDuplicateMessage(
                receiver, message, DUPLICATE_CHECK_TIME_WINDOW_MINUTES)) {
            markAsProcessed(receiver, messageId);
            return ProcessingResult.duplicate(message, "Duplicate found in message history");
        }

        // 4. Отправка сообщения (имитация)
        boolean sent = sendMessage(message);

        if (sent) {
            markAsProcessed(receiver, messageId);
            return ProcessingResult.sent(message, "Message sent successfully");
        } else {
            return ProcessingResult.failed(message, "Failed to send message");
        }
    }

    private ProcessingResult processSingleMessageOptimized(String sender, Message message) {
        String receiver = message.receiver();
        // Генерация уникального идентификатора сообщения
        String messageId = generateMessageId(sender, message);

        // 1. Проверка уникальности в рамках текущей сессии
        // 1. Атомарная проверка и пометка в кэше
        if (checkAndMarkAsProcessed(receiver, messageId)) {
            return ProcessingResult.duplicate(message, "Message already processed in current session");
        }

        // 2. Проверка настроек пользователя
        Optional<UserSettings> userSettings = userSettingsService.getUserSettings(receiver);
        if (userSettings.isEmpty()) {
            return ProcessingResult.rejected(message, "User not found or has no settings");
        }

        if (!userSettings.get().allowedChannels().contains(message.channel())) {
            return ProcessingResult.rejected(message,
                    String.format("Channel %s is not allowed for user %s",
                            message.channel(), receiver));
        }

        // 3. Проверка истории на дубликаты
        if (messageHistoryService.hasDuplicateMessage(
                receiver, message, DUPLICATE_CHECK_TIME_WINDOW_MINUTES)) {
            markAsProcessed(receiver, messageId);
            return ProcessingResult.duplicate(message, "Duplicate found in message history");
        }

        // 4. Отправка сообщения (имитация)
        boolean sent = sendMessage(message);

        if (sent) {
            markAsProcessed(receiver, messageId);
            return ProcessingResult.sent(message, "Message sent successfully");
        } else {
            return ProcessingResult.failed(message, "Failed to send message");
        }
    }

    /**
     * Потокобезопасная обработка одного сообщения
     */
    private ProcessingResult processSingleMessageThreadSafe(String sender, Message message) {
        String receiver = message.receiver();
        String messageId = generateMessageId(sender, message);

        // 1. АТОМАРНАЯ проверка и пометка в кэше
        if (!markAsProcessedIfAbsent(receiver, messageId)) {
            return ProcessingResult.duplicate(message, "Message already processed in current session");
        }

        // 2. Проверка настроек пользователя
        Optional<UserSettings> userSettings = userSettingsService.getUserSettings(receiver);
        if (userSettings.isEmpty()) {
            return ProcessingResult.rejected(message, "User not found or has no settings");
        }

        if (!userSettings.get().allowedChannels().contains(message.channel())) {
            return ProcessingResult.rejected(message,
                    String.format("Channel %s is not allowed for user %s",
                            message.channel(), receiver));
        }

        // 3. Проверка истории на дубликаты
        if (messageHistoryService.hasDuplicateMessage(
                receiver, message, DUPLICATE_CHECK_TIME_WINDOW_MINUTES)) {
            return ProcessingResult.duplicate(message, "Duplicate found in message history");
        }

        // 4. Отправка сообщения
        boolean sent = sendMessage(message);

        return sent ?
                ProcessingResult.sent(message, "Message sent successfully") :
                ProcessingResult.failed(message, "Failed to send message");
    }

    /**
     * АТОМАРНО добавляет messageId в кэш, если его еще нет
     *
     * @return true если элемент был добавлен, false если уже существовал
     */
    private boolean markAsProcessedIfAbsent(String receiver, String messageId) {
        // Создаем новый потокобезопасный Set для пользователя, если его еще нет
        Set<String> userMessages = processedMessageCache.computeIfAbsent(
                receiver,
                k -> ConcurrentHashMap.newKeySet()
        );

        // Атомарно добавляем messageId, возвращаем true если его не было
        return userMessages.add(messageId);
    }

    /**
     * Альтернативная реализация с использованием putIfAbsent паттерна
     */
    private boolean markAsProcessedIfAbsentV2(String receiver, String messageId) {
        // Используем ConcurrentHashMap для атомарных операций
        return processedMessageCache
                .computeIfAbsent(receiver, k -> ConcurrentHashMap.newKeySet())
                .add(messageId);
    }

    /**
     * Альтернативная реализация с синхронизацией по ключу пользователя
     * (Медленнее, но гарантирует отсутствие гонок при сложных операциях)
     */
    private boolean markAsProcessedIfAbsentV3(String receiver, String messageId) {
        // Синхронизируем по receiver, чтобы разные пользователи не блокировали друг друга
        synchronized (processedMessageCache.computeIfAbsent(
                receiver, k -> ConcurrentHashMap.newKeySet()
        )) {
            Set<String> userMessages = processedMessageCache.get(receiver);
            if (userMessages == null) {
                userMessages = ConcurrentHashMap.newKeySet();
                processedMessageCache.put(receiver, userMessages);
            }

            if (userMessages.contains(messageId)) {
                return false;
            }

            return userMessages.add(messageId);
        }
    }

    /**
     * Генерирует уникальный идентификатор сообщения на основании:
     * - отправителя
     * - получателя
     * - канала
     * - хэша текста сообщения
     */
    private String generateMessageId(String sender, Message message) {
        return String.format("%s-%s-%s-%s",
                sender,
                message.receiver(),
                message.channel(),
                UUID.nameUUIDFromBytes(message.text().getBytes()));
    }

    /**
     * Проверяет, обрабатывалось ли уже это сообщение в текущей сессии
     */
    private boolean isAlreadyProcessed(String receiver, String messageId) {
        Set<String> userMessages = processedMessageCache.get(receiver);
        return userMessages != null && userMessages.contains(messageId);
    }

    /**
     * Помечает сообщение как обработанное
     */
    private void markAsProcessed(String receiver, String messageId) {
        processedMessageCache
                .computeIfAbsent(receiver, k -> ConcurrentHashMap.newKeySet())
                .add(messageId);
    }

    /**
     * Имитация отправки сообщения через соответствующий канал
     */
    private boolean sendMessage(Message message) {
        try {
            // Здесь была бы реальная интеграция с SMS/Email/Push сервисами
            switch (message.channel()) {
                case SMS:
                    // Интеграция с SMS-шлюзом
                    SafePrinter.printf("Sending SMS to %s: %s%n",
                            message.receiver(), message.text());
                    break;
                case EMAIL:
                    // Интеграция с почтовым сервером
                    SafePrinter.printf("Sending Email to %s: %s%n",
                            message.receiver(), message.text());
                    break;
                case PUSH:
                    // Интеграция с push-сервисом
                    SafePrinter.printf("Sending Push to %s: %s%n",
                            message.receiver(), message.text());
                    break;
            }
            return true;
        } catch (Exception e) {
            System.err.printf("Failed to send message via %s: %s%n",
                    message.channel(), e.getMessage());
            return false;
        }
    }

    /**
     * Очищает кэш обработанных сообщений для указанного пользователя
     */
    public void clearCacheForUser(String userId) {
        processedMessageCache.remove(userId);
    }

    /**
     * Очищает весь кэш
     */
    public void clearCache() {
        processedMessageCache.clear();
    }


    /**
     * Атомарная проверка и пометка сообщения как обработанного
     *
     * @return true, если сообщение уже было обработано
     */
    private boolean checkAndMarkAsProcessed(String receiver, String messageId) {
        synchronized (lock) {
            Set<String> userMessages = processedMessageCache.get(receiver);
            if (userMessages != null && userMessages.contains(messageId)) {
                return true; // Уже обработано
            }

            // Создаем/получаем Set для пользователя и добавляем messageId
            processedMessageCache
                    .computeIfAbsent(receiver, k -> ConcurrentHashMap.newKeySet())
                    .add(messageId);
            return false; // Не было обработано, теперь помечено
        }
    }

    // Геттер для тестов
    Map<String, Set<String>> getProcessedMessageCache() {
        return processedMessageCache;
    }
}
