package tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record;

import java.math.BigDecimal;

public record FixedDiscount(BigDecimal amount) implements DiscountType {
    public BigDecimal apply(BigDecimal price) {
        return price.subtract(amount).max(BigDecimal.ZERO);
    }
}
