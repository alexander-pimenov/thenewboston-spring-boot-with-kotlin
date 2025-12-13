package tv.codealong.tutorials.various.yandex.test6_3_real;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Корзина с покупками.
 * Модели данных (DTO)
 */
public class ShoppingCart {
    private final String customerId;        // final
    private final List<Purchase> purchases; // final, но список внутри изменяем!

    public ShoppingCart(String customerId) {
        this.customerId = customerId;
        this.purchases = new ArrayList<>(); // Защитная копия!
    }

    public ShoppingCart(String customerId, List<Purchase> purchases) {
        this.customerId = customerId;
        this.purchases = new ArrayList<>(purchases);
    }

    public String getCustomerId() {
        return customerId;
    }

    // Возвращаем защитную копию
    public List<Purchase> getPurchases() {
        return new ArrayList<>(purchases); // Защитная копия!
    }

    public void addPurchase(Purchase purchase) {
        purchases.add(purchase);
    }

    // Если хотим добавить товар - создаем новый объект (соблюдаем иммутабельность)
    public ShoppingCart withAddedPurchase(Purchase purchase) {
        List<Purchase> newPurchases = new ArrayList<>(this.purchases);
        newPurchases.add(purchase);
        return new ShoppingCart(this.customerId, newPurchases);
    }

    public BigDecimal calculateTotalOriginalPrice() {
        return purchases.stream()
                .map(Purchase::getOriginalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalFinalPrice() {
        return purchases.stream()
                .map(Purchase::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingCart that = (ShoppingCart) o;
        return Objects.equals(customerId, that.customerId) &&
                Objects.equals(purchases, that.purchases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, purchases);
    }

    @Override
    public String toString() {
        return "ShoppingCart{" +
                "customerId='" + customerId + '\'' +
                ", purchases=" + purchases +
                '}';
    }
}
