package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import java.time.Instant;
import java.util.Set;

/**
 * Контракты для внешних интеграций.
 */
public interface MessageHistoryService {
    /**
     * Получает историю отправленных сообщений для пользователя
     *
     * @param userId идентификатор пользователя
     * @param limit  максимальное количество возвращаемых сообщений
     * @return коллекция отправленных сообщений (от новых к старым)
     */
    Set<SentMessage> getSentMessages(String userId, int limit);

    /**
     * Получает историю отправленных сообщений за указанный период
     *
     * @param userId   идентификатор пользователя
     * @param fromDate начало периода
     * @param toDate   конец периода
     * @return коллекция отправленных сообщений
     */
    Set<SentMessage> getSentMessages(String userId, Instant fromDate, Instant toDate);

    /**
     * Проверяет, было ли уже отправлено аналогичное сообщение
     *
     * @param userId     идентификатор пользователя
     * @param message    сообщение для проверки
     * @param timeWindow временное окно для проверки дубликатов (в минутах)
     * @return true, если дубликат найден
     */
    boolean hasDuplicateMessage(String userId, Message message, int timeWindow);

}
