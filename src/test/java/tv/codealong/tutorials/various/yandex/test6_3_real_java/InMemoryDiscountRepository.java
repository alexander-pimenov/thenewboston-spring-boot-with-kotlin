package tv.codealong.tutorials.various.yandex.test6_3_real_java;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryDiscountRepository implements DiscountRepository {

    /**
     * Локальное хранилище скидок.
     */
    private final Map<String, Discount> discounts = new HashMap<>();

    public InMemoryDiscountRepository() {
        // Пример данных для демонстрации
        discounts.put("customer-1", new Discount("customer-1", 10));
        discounts.put("customer-2", new Discount("customer-2", 15));
        discounts.put("customer-3", new Discount("customer-3", 20));
        discounts.put("customer-4", new Discount("customer-4", 17));
        discounts.put("customer-5", new Discount("customer-5", 12));
        discounts.put("vip-customer", new Discount("vip-customer", 30));
    }

    @Override
    public Optional<Discount> findDiscountByCustomerId(String customerId) {
        return Optional.empty();
    }

    public void addDiscount(Discount discount) {
        discounts.put(discount.getCustomerId(), discount);
    }

    @Override
    public String toString() {
        return "InMemoryDiscountRepository{" +
                "discounts=" + discounts +
                '}';
    }
}
