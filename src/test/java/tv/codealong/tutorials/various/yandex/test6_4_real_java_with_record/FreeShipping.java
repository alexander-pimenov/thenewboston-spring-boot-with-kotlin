package tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record;

import java.math.BigDecimal;

public record FreeShipping() implements DiscountType {
    public BigDecimal apply(BigDecimal price) {
        return price; // Бесплатная доставка обрабатывается отдельно
    }
}
