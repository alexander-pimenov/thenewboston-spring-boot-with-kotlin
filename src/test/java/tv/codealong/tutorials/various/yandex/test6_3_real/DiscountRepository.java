package tv.codealong.tutorials.various.yandex.test6_3_real;

import java.util.Optional;

/**
 * Репозиторий для работы со скидками
 */
public interface DiscountRepository {

    Optional<Discount> findDiscountByCustomerId(String customerId);
}
