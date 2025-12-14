package tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Дисконт.
 * Модели данных (DTO).
 * Records — это специальный тип класса, который предназначен именно для DTO (Data Transfer Objects).
 * Это неизменяемые (immutable) классы, которые автоматически генерируют:
 * - Конструктор со всеми полями
 * - Геттеры (но без префикса get)
 * - Методы equals(), hashCode(), toString()
 * <p>
 *  Пример системы с with-методами для создания новых объектов, аналогично методу copy() в Kotlin.
 */
public record Discount(String customerId, int percentage) {

    // Compact constructor для валидации
    // Компактный конструктор - нет параметров!
    // Они уже определены в заголовке record
    public Discount {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100. Provided: " + percentage);
        }
        // Неявно: this.customerId = customerId; this.percentage = percentage;
    }

    public BigDecimal applyTo(BigDecimal price) {
        BigDecimal discountMultiplier = BigDecimal.valueOf(1 - percentage / 100.0);
        return price.multiply(discountMultiplier).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    // Статический фабричный метод
    public static Discount of(String customerId, int percentage) {
        return new Discount(customerId, percentage);
    }

    // ========== WITH-МЕТОДЫ (аналог copy) ==========
    public Discount withCustomerId(String customerId) {
        return new Discount(customerId, this.percentage);
    }

    public Discount withPercentage(int percentage) {
        return new Discount(this.customerId, percentage);
    }

    // Специальный with-метод для бизнес-логики
    public Discount withAdditionalPercentage(int additionalPercent) {
        int newPercentage = Math.min(100, this.percentage + additionalPercent);
        return withPercentage(newPercentage);
    }
}
