package tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DiscountTest {

    @Test
    void constructor_WithValidPercentage_ShouldCreateDiscount() {
        // Act
        Discount discount = new Discount("customer-1", 25);

        // Assert
        assertEquals("customer-1", discount.customerId());
        assertEquals(25, discount.percentage());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    void constructor_WithInvalidPercentage_ShouldThrowException(int invalidPercentage) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Discount("customer-1", invalidPercentage)
        );

        assertTrue(exception.getMessage().contains("Discount percentage must be between 0 and 100"));
    }

    @Test
    void applyTo_ShouldCalculateCorrectDiscountedPrice() {
        // Arrange
        Discount discount = new Discount("customer-1", 25);
        BigDecimal originalPrice = BigDecimal.valueOf(100);

        // Act
        BigDecimal discountedPrice = discount.applyTo(originalPrice);

        // Assert
        // 100 - 25% = 75
        assertEquals(BigDecimal.valueOf(75.00).setScale(2), discountedPrice);
    }

    @Test
    void applyTo_WithZeroDiscount_ShouldReturnSamePrice() {
        // Arrange
        Discount discount = new Discount("customer-1", 0);
        BigDecimal originalPrice = BigDecimal.valueOf(100);

        // Act
        BigDecimal discountedPrice = discount.applyTo(originalPrice);

        // Assert
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), discountedPrice);
    }

    @Test
    void applyTo_ShouldRoundToTwoDecimalPlaces() {
        // Arrange
        Discount discount = new Discount("customer-1", 33);
        BigDecimal originalPrice = BigDecimal.valueOf(100);

        // Act
        BigDecimal discountedPrice = discount.applyTo(originalPrice);
        SafePrinter.println(discount.toString());

        // Assert
        // 100 - 33% = 67, должно быть округлено до 2 знаков
        assertEquals(BigDecimal.valueOf(67.00).setScale(2), discountedPrice);
    }

    @Test
    @DisplayName("Для практики создания объектов с помощью методов with")
    void toCreate_objects_with_with_methods() {

        // Discount
        Discount discount = Discount.of("customer-1", 25);
        System.out.println(discount); //Discount[customerId=customer-1, percentage=25]
        Discount discount1 = discount.withPercentage(50);
        System.out.println(discount1); //Discount[customerId=customer-1, percentage=50]

        assertNotEquals(discount, discount1);

        //Purchase
        Purchase purchase = Purchase.of("product-1", BigDecimal.valueOf(100));
        System.out.println(purchase); //Purchase[productId=product-1, originalPrice=100, finalPrice=100]
        Purchase purchase1 = purchase.withDiscount(BigDecimal.valueOf(80));
        System.out.println(purchase1); //Purchase[productId=product-1, originalPrice=100, finalPrice=80]

        // ShoppingCart, можно реализовать потом.
    }
}