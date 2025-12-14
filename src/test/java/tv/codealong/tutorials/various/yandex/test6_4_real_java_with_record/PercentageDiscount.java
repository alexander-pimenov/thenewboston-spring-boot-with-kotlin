package tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record;

import java.math.BigDecimal;

public record PercentageDiscount(int percentage) implements DiscountType {
    public BigDecimal apply(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(1 - percentage / 100.0));
    }
}
