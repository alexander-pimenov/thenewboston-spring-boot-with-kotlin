package tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {
    @Mock
    private DiscountRepository discountRepository;

    private LoyaltyService loyaltyService;

    @BeforeEach
    void setUp() {
        loyaltyService = new LoyaltyService(discountRepository);
    }

    @Test
    void applyDiscount_WhenCustomerHasDiscount_ShouldApplyDiscountToAllPurchases() {
        // Arrange
        String customerId = "customer-1";
        Discount discount = new Discount(customerId, 10);

        Purchase purchase1 = new Purchase("product-1", BigDecimal.valueOf(100));
        Purchase purchase2 = new Purchase("product-2", BigDecimal.valueOf(50));
        ShoppingCart cart = new ShoppingCart(customerId, Arrays.asList(purchase1, purchase2));

        when(discountRepository.findDiscountByCustomerId(customerId))
                .thenReturn(Optional.of(discount));

        // Act
        ShoppingCart result = loyaltyService.applyDiscount(customerId, cart);

        // Assert
        assertEquals(2, result.purchases().size());

        // Проверяем скидку на первый товар: 100 - 10% = 90
        assertEquals(BigDecimal.valueOf(90.00).setScale(2),
                result.purchases().get(0).finalPrice().setScale(2));

        // Проверяем скидку на второй товар: 50 - 10% = 45
        assertEquals(BigDecimal.valueOf(45.00).setScale(2),
                result.purchases().get(1).finalPrice());

        // Проверяем что оригинальные цены не изменились
        assertEquals(BigDecimal.valueOf(100.00).setScale(2),
                result.purchases().get(0).originalPrice().setScale(2));
    }

    @Test
    void applyDiscount_WhenCustomerHasNoDiscount_ShouldApplyZeroDiscount() {
        // Arrange
        String customerId = "customer-without-discount";
        Purchase purchase = new Purchase("product-1", BigDecimal.valueOf(100));
        ShoppingCart cart = new ShoppingCart(customerId, List.of(purchase));

        when(discountRepository.findDiscountByCustomerId(customerId))
                .thenReturn(Optional.empty());

        // Act
        ShoppingCart result = loyaltyService.applyDiscount(customerId, cart);

        // Assert
        assertEquals(BigDecimal.valueOf(100.00).setScale(2),
                result.purchases().get(0).finalPrice().setScale(2));
    }

    @Test
    void applyDiscount_WhenDiscountIs100Percent_ShouldMakeEverythingFree() {
        // Arrange
        String customerId = "lucky-customer";
        Discount discount = new Discount(customerId, 100);
        Purchase purchase = new Purchase("product-1", BigDecimal.valueOf(100));
        ShoppingCart cart = new ShoppingCart(customerId, Arrays.asList(purchase));

        when(discountRepository.findDiscountByCustomerId(customerId))
                .thenReturn(Optional.of(discount));

        // Act
        ShoppingCart result = loyaltyService.applyDiscount(customerId, cart);

        // Assert
        assertEquals(BigDecimal.ZERO.setScale(2),
                result.purchases().get(0).finalPrice().setScale(2));
    }

    @Test
    void applyDiscount_WhenCustomerIdMismatch_ShouldThrowException() {
        // Arrange
        String customerId = "customer-1";
        String differentCustomerId = "customer-2";
        ShoppingCart cart = new ShoppingCart(differentCustomerId);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loyaltyService.applyDiscount(customerId, cart)
        );

        assertTrue(exception.getMessage().contains("Customer ID does not match"));
    }

    @Test
    void calculateTotalDiscount_ShouldReturnCorrectDiscountAmount() {
        // Arrange
        String customerId = "customer-1";
        Discount discount = new Discount(customerId, 20); // 20% скидка

        Purchase purchase1 = new Purchase("product-1", BigDecimal.valueOf(100));
        Purchase purchase2 = new Purchase("product-2", BigDecimal.valueOf(50));
        ShoppingCart cart = new ShoppingCart(customerId, Arrays.asList(purchase1, purchase2));

        when(discountRepository.findDiscountByCustomerId(customerId))
                .thenReturn(Optional.of(discount));

        // Act
        BigDecimal totalDiscount = loyaltyService.calculateTotalDiscount(customerId, cart);

        // Assert
        // Общая стоимость: 150, скидка 20% = 30
        assertEquals(BigDecimal.valueOf(30.00).setScale(2), totalDiscount.setScale(2));
    }

    @Test
    void applyDiscount_WithMultiplePurchases_ShouldMaintainPurchaseOrder() {
        // Arrange
        String customerId = "customer-1";
        Discount discount = new Discount(customerId, 10);

        Purchase purchase1 = new Purchase("product-1", BigDecimal.valueOf(100));
        Purchase purchase2 = new Purchase("product-2", BigDecimal.valueOf(200));
        Purchase purchase3 = new Purchase("product-3", BigDecimal.valueOf(300));
        ShoppingCart cart = new ShoppingCart(customerId, Arrays.asList(purchase1, purchase2, purchase3));

        when(discountRepository.findDiscountByCustomerId(customerId))
                .thenReturn(Optional.of(discount));

        // Act
        ShoppingCart result = loyaltyService.applyDiscount(customerId, cart);

        // Assert
        assertEquals("product-1", result.purchases().get(0).productId());
        assertEquals("product-2", result.purchases().get(1).productId());
        assertEquals("product-3", result.purchases().get(2).productId());
    }

}