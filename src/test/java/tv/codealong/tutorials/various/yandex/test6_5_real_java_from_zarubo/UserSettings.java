package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import java.time.Instant;
import java.util.Set;

/**
 * Модель данных - пользовательские настройки, хранимые в БД или в кэше.
 */
public record UserSettings(
        String userId,
        Set<Channel> allowedChannels,
        Instant updatedAt
) {}
