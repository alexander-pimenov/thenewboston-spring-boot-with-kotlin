package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;

import java.util.Optional;

/**
 * Контракты для внешних интеграций.
 */
public interface UserSettingsService {
    /**
     * Получает настройки пользователя по его идентификатору
     *
     * @param userId идентификатор пользователя
     * @return Optional с настройками пользователя или пустой Optional,
     * если пользователь не найден
     */
    Optional<UserSettings> getUserSettings(String userId);

    /**
     * Проверяет, разрешен ли пользователю указанный канал
     *
     * @param userId  идентификатор пользователя
     * @param channel канал для проверки
     * @return true, если канал разрешен
     */
    default boolean isChannelAllowed(String userId, Channel channel) {
        return getUserSettings(userId)
                .map(settings -> settings.allowedChannels().contains(channel))
                .orElse(false);
    }
}
