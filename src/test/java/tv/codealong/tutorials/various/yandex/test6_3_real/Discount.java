package tv.codealong.tutorials.various.yandex.test6_3_real;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Дисконт.
 * Модели данных (DTO)
 */
public class Discount {
    private final String customerId;
    private final int percentage;

    public Discount(String customerId, int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100. Provided: " + percentage);
        }
        this.customerId = customerId;
        this.percentage = percentage;
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getPercentage() {
        return percentage;
    }

    public BigDecimal applyTo(BigDecimal price) {
        BigDecimal discountMultiplier = BigDecimal.valueOf(1 - percentage / 100.0);
        return price.multiply(discountMultiplier).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discount discount = (Discount) o;
        return percentage == discount.percentage &&
                Objects.equals(customerId, discount.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, percentage);
    }

    @Override
    public String toString() {
        return "Discount{" +
                "customerId='" + customerId + '\'' +
                ", percentage=" + percentage +
                '}';
    }
}
