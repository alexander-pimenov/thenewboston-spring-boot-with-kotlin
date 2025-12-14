package tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record;

import java.math.BigDecimal;

/**
 * Sealed Interfaces + Records для DTO.
 * Sealed hierarchy (Запечатанная иерархия) для разных типов скидок.
 */
public sealed interface DiscountType
        permits PercentageDiscount, FixedDiscount, FreeShipping {

    BigDecimal apply(BigDecimal price);
}

