package tv.codealong.tutorials.various.yandex.test6_5_real_java_from_zarubo;


/**
 * Модель данных - сообщение
 */
public record Message(
        String text,
        Channel channel,
        String receiver
) {
}
