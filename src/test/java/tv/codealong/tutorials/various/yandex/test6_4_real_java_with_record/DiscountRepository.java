package tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record;

import java.util.Optional;

/**
 * Репозиторий для работы со скидками
 */
public interface DiscountRepository {

    Optional<Discount> findDiscountByCustomerId(String customerId);
}
