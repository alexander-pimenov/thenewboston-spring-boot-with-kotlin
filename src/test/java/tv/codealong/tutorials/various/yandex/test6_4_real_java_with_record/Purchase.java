package tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Покупка.
 * Модели данных (DTO)
 */
public record Purchase(
        String productId,
        BigDecimal originalPrice,
        BigDecimal finalPrice
) {

    // Compact constructor для валидации
    // Компактный конструктор - нет параметров!
    // Они уже определены в заголовке record
    public Purchase {
        Objects.requireNonNull(productId, "productId cannot be null");
        Objects.requireNonNull(originalPrice, "originalPrice cannot be null");
        Objects.requireNonNull(finalPrice, "finalPrice cannot be null");
        if (originalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Final price cannot be negative");
        }

        // Неявно: this.x = x; this.y = y;
    }

    // Конструктор без скидки (finalPrice = originalPrice)
    public Purchase(String productId, BigDecimal originalPrice) {
        this(productId, originalPrice, originalPrice);
    }

    // Статический фабричный метод, finalPrice не задаем, он будет равен originalPrice
    public static Purchase of(String productId, BigDecimal originalPrice) {
        return new Purchase(productId, originalPrice, originalPrice);
    }

    // "with-метод" для иммутабельного обновления - Специальный with-метод для скидок
    public Purchase withDiscount(BigDecimal newFinalPrice) {
        return new Purchase(this.productId, this.originalPrice, newFinalPrice);
    }

    // ========== WITH-МЕТОДЫ (аналог copy) ==========
    public Purchase withProductId(String productId) {
        return new Purchase(productId, this.originalPrice, this.finalPrice);
    }

    public Purchase withOriginalPrice(BigDecimal originalPrice) {
        return new Purchase(this.productId, originalPrice, this.finalPrice);
    }

    public Purchase withFinalPrice(BigDecimal finalPrice) {
        return new Purchase(this.productId, this.originalPrice, finalPrice);
    }

    // Удобный copy (аналог Kotlin, но с билдером)
    public Builder copy() {
        return new Builder(this);
    }

    // Вложенный Builder
    public static class Builder {
        private String productId;
        private BigDecimal originalPrice;
        private BigDecimal finalPrice;

        private Builder(Purchase source) {
            this.productId = source.productId();
            this.originalPrice = source.originalPrice();
            this.finalPrice = source.finalPrice();
        }

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder originalPrice(BigDecimal originalPrice) {
            this.originalPrice = originalPrice;
            return this;
        }

        public Builder finalPrice(BigDecimal finalPrice) {
            this.finalPrice = finalPrice;
            return this;
        }

        public Purchase build() {
            return new Purchase(productId, originalPrice, finalPrice);
        }
    }
}
