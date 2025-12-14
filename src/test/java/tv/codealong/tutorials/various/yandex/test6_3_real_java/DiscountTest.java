package tv.codealong.tutorials.various.yandex.test6_3_real_java;

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
        assertEquals("customer-1", discount.getCustomerId());
        assertEquals(25, discount.getPercentage());
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

        // Assert
        // 100 - 33% = 67, должно быть округлено до 2 знаков
        assertEquals(BigDecimal.valueOf(67.00).setScale(2), discountedPrice);
    }

}