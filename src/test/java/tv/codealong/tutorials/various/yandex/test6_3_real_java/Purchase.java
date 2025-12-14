package tv.codealong.tutorials.various.yandex.test6_3_real_java;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Покупка.
 * Модели данных (DTO)
 */
public class Purchase {
    private final String productId;         // final - неизменяемое поле
    private final BigDecimal originalPrice; // final - неизменяемое поле
    private BigDecimal finalPrice;          // НЕ final - изменяемое поле!

    public Purchase(String productId, BigDecimal originalPrice) {
        this.productId = productId;
        this.originalPrice = originalPrice;
        this.finalPrice = originalPrice;
    }

    public String getProductId() {
        return productId;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Purchase purchase = (Purchase) o;
        return Objects.equals(productId, purchase.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "productId='" + productId + '\'' +
                ", originalPrice=" + originalPrice +
                ", finalPrice=" + finalPrice +
                '}';
    }
}
