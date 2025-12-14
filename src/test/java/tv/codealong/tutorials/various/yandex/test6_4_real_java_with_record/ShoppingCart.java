package tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Корзина с покупками.
 * Модели данных (DTO)
 * ShoppingCart - особая ситуация при использовании Records.
 * Для ShoppingCart ситуация сложнее, так как он содержит список.
 * Records хорошо подходят для простых DTO, но для более сложных объектов с коллекциями нужно быть осторожнее.
 */
public record ShoppingCart(String customerId, List<Purchase> purchases) {

    // Защитная копия в конструкторе
    // Компактный конструктор - нет параметров!
    // Они уже определены в заголовке record
    public ShoppingCart {
        // Создаем защитную неизменяемую копию
        purchases = List.copyOf(purchases);

        // Неявно: this.x = x; this.y = y;
    }

    public ShoppingCart(String customerId) {
        this(customerId, List.of());
    }

    public void addPurchase(Purchase purchase) {
        purchases.add(purchase);
    }

    // Если хотим добавить товар - создаем новый объект (соблюдаем иммутабельность) - Специальные with-методы
    public ShoppingCart withAddedPurchase(Purchase purchase) {
        List<Purchase> newPurchases = new ArrayList<>(this.purchases);
        newPurchases.add(purchase);
        return new ShoppingCart(this.customerId, newPurchases);
    }

    public ShoppingCart withRemovedPurchase(String productId) {
        List<Purchase> newPurchases = this.purchases.stream()
                .filter(p -> !p.productId().equals(productId))
                .toList();
        return withPurchases(newPurchases);
    }

    public BigDecimal calculateTotalOriginalPrice() {
        return purchases.stream()
                .map(Purchase::originalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalFinalPrice() {
        return purchases.stream()
                .map(Purchase::finalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ========== WITH-МЕТОДЫ (аналог copy) ==========
    public ShoppingCart withCustomerId(String customerId) {
        return new ShoppingCart(customerId, this.purchases);
    }

    public ShoppingCart withPurchases(List<Purchase> newPurchases) {
        return new ShoppingCart(this.customerId, newPurchases);
    }

    // Copy билдер
    public CartBuilder copy() {
        return new CartBuilder(this);
    }

    public static class CartBuilder {
        private String customerId;
        private List<Purchase> purchases;

        private CartBuilder(ShoppingCart source) {
            this.customerId = source.customerId();
            this.purchases = new ArrayList<>(source.purchases());
        }

        public CartBuilder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public CartBuilder purchases(List<Purchase> purchases) {
            this.purchases = new ArrayList<>(purchases);
            return this;
        }

        public CartBuilder addPurchase(Purchase purchase) {
            this.purchases.add(purchase);
            return this;
        }

        public ShoppingCart build() {
            return new ShoppingCart(customerId, purchases);
        }
    }

}
