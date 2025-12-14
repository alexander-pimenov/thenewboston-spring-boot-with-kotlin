Недавно был на собеседовании в компании, где меня попросили реализовать функционал описанный ниже:
```
 Вы — backend-разработчик в интернет-магазине.
 Дела идут в гору и магазин решил повысить лояльность покупателей, предоставляя им персональные скидки.
 К вам обратился product owner с задачей создать простую систему лояльности,
 которая предоставляет процентную скидку на корзину.
 Размер скидки зависит от покупателя.
 Аналитики уже определили, какие скидки должны быть предоставлены покупателям.

 ## Определения

 Корзина - список покупок покупателя.

 Покупка:
 - id товара
 - цена
 - итоговая стоимость c учетом скидки

 Скидка. Для покупателя может быть задан % скидки (целое число).
 ## Задача
 Написать часть новой системы лояльности, которая:
 - на вход получает id покупателя и корзину
 - вычисляет и применяет скидки
 - возвращает корзину, в которой учтены скидки. Скидка учитывается в стоимости покупки
```
Это задание на Java. Тесты на функциональность должны быть реализованы с помощью JUnit5, Mock и др.

-----

Давайте реализуем систему лояльности с чистым кодом и тестами. Я создам полноценное решение с учетом принципов SOLID.

## Структура решения:

1. **Модели данных** (DTO)
2. **Сервис для работы со скидками**
3. **Репозиторий для получения данных о скидках** (с интерфейсом для гибкости)
4. **Основной сервис корзины**
5. **Тесты с использованием JUnit 5 и Mockito**

## Реализация:

### 1. Модели данных

```java
// Purchase.java
package com.onlinestore.loyalty;

import java.math.BigDecimal;
import java.util.Objects;

public class Purchase {
    private final String productId;
    private final BigDecimal originalPrice;
    private BigDecimal finalPrice;

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
```

```java
// ShoppingCart.java
package com.onlinestore.loyalty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShoppingCart {
    private final String customerId;
    private final List<Purchase> purchases;

    public ShoppingCart(String customerId) {
        this.customerId = customerId;
        this.purchases = new ArrayList<>();
    }

    public ShoppingCart(String customerId, List<Purchase> purchases) {
        this.customerId = customerId;
        this.purchases = new ArrayList<>(purchases);
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<Purchase> getPurchases() {
        return new ArrayList<>(purchases);
    }

    public void addPurchase(Purchase purchase) {
        purchases.add(purchase);
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
}
```

```java
// Discount.java
package com.onlinestore.loyalty;

import java.math.BigDecimal;
import java.util.Objects;

public class Discount {
    private final String customerId;
    private final int percentage;

    public Discount(String customerId, int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        this.customerId = customerId;
        this.percentage = percentage;
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getPercentage() {
        return percentage;
    }

    public BigDecimal applyTo(BigDecimal price) {
        BigDecimal discountMultiplier = BigDecimal.valueOf(1 - percentage / 100.0);
        return price.multiply(discountMultiplier).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discount discount = (Discount) o;
        return percentage == discount.percentage &&
                Objects.equals(customerId, discount.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, percentage);
    }
}
```

### 2. Репозиторий для работы со скидками

```java
// DiscountRepository.java
package com.onlinestore.loyalty;

import java.util.Optional;

public interface DiscountRepository {
    Optional<Discount> findDiscountByCustomerId(String customerId);
}
```

```java
// InMemoryDiscountRepository.java
package com.onlinestore.loyalty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryDiscountRepository implements DiscountRepository {
    private final Map<String, Discount> discounts = new HashMap<>();

    public InMemoryDiscountRepository() {
        // Пример данных для демонстрации
        discounts.put("customer-1", new Discount("customer-1", 10));
        discounts.put("customer-2", new Discount("customer-2", 15));
        discounts.put("customer-3", new Discount("customer-3", 20));
        discounts.put("vip-customer", new Discount("vip-customer", 30));
    }

    @Override
    public Optional<Discount> findDiscountByCustomerId(String customerId) {
        return Optional.ofNullable(discounts.get(customerId));
    }

    public void addDiscount(Discount discount) {
        discounts.put(discount.getCustomerId(), discount);
    }
}
```

### 3. Основной сервис лояльности

```java
// LoyaltyService.java
package com.onlinestore.loyalty;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class LoyaltyService {
    private final DiscountRepository discountRepository;

    public LoyaltyService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    /**
     * Применяет скидку к корзине покупателя
     * 
     * @param customerId ID покупателя
     * @param shoppingCart Корзина покупок
     * @return Корзина с примененными скидками
     */
    public ShoppingCart applyDiscount(String customerId, ShoppingCart shoppingCart) {
        if (!customerId.equals(shoppingCart.getCustomerId())) {
            throw new IllegalArgumentException("Customer ID does not match shopping cart customer ID");
        }

        Discount discount = discountRepository.findDiscountByCustomerId(customerId)
                .orElse(new Discount(customerId, 0)); // Если скидки нет, применяем 0%

        List<Purchase> discountedPurchases = shoppingCart.getPurchases().stream()
                .map(purchase -> applyDiscountToPurchase(purchase, discount))
                .collect(Collectors.toList());

        return new ShoppingCart(customerId, discountedPurchases);
    }

    private Purchase applyDiscountToPurchase(Purchase originalPurchase, Discount discount) {
        Purchase discountedPurchase = new Purchase(
                originalPurchase.getProductId(),
                originalPurchase.getOriginalPrice()
        );
        
        BigDecimal discountedPrice = discount.applyTo(originalPurchase.getOriginalPrice());
        discountedPurchase.setFinalPrice(discountedPrice);
        
        return discountedPurchase;
    }

    /**
     * Рассчитывает общую сумму скидки для корзины
     */
    public BigDecimal calculateTotalDiscount(String customerId, ShoppingCart shoppingCart) {
        ShoppingCart discountedCart = applyDiscount(customerId, shoppingCart);
        return shoppingCart.calculateTotalOriginalPrice()
                .subtract(discountedCart.calculateTotalFinalPrice());
    }
}
```

### 4. Тесты с JUnit 5 и Mockito

```java
// LoyaltyServiceTest.java
package com.onlinestore.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        assertEquals(2, result.getPurchases().size());
        
        // Проверяем скидку на первый товар: 100 - 10% = 90
        assertEquals(BigDecimal.valueOf(90.00), 
                result.getPurchases().get(0).getFinalPrice());
        
        // Проверяем скидку на второй товар: 50 - 10% = 45
        assertEquals(BigDecimal.valueOf(45.00), 
                result.getPurchases().get(1).getFinalPrice());
        
        // Проверяем что оригинальные цены не изменились
        assertEquals(BigDecimal.valueOf(100.00), 
                result.getPurchases().get(0).getOriginalPrice());
    }

    @Test
    void applyDiscount_WhenCustomerHasNoDiscount_ShouldApplyZeroDiscount() {
        // Arrange
        String customerId = "customer-without-discount";
        Purchase purchase = new Purchase("product-1", BigDecimal.valueOf(100));
        ShoppingCart cart = new ShoppingCart(customerId, Arrays.asList(purchase));

        when(discountRepository.findDiscountByCustomerId(customerId))
                .thenReturn(Optional.empty());

        // Act
        ShoppingCart result = loyaltyService.applyDiscount(customerId, cart);

        // Assert
        assertEquals(BigDecimal.valueOf(100.00), 
                result.getPurchases().get(0).getFinalPrice());
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
                result.getPurchases().get(0).getFinalPrice());
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
        assertEquals(BigDecimal.valueOf(30.00), totalDiscount);
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
        assertEquals("product-1", result.getPurchases().get(0).getProductId());
        assertEquals("product-2", result.getPurchases().get(1).getProductId());
        assertEquals("product-3", result.getPurchases().get(2).getProductId());
    }
}
```

```java
// DiscountTest.java
package com.onlinestore.loyalty;

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
        assertEquals(BigDecimal.valueOf(75.00), discountedPrice);
    }

    @Test
    void applyTo_WithZeroDiscount_ShouldReturnSamePrice() {
        // Arrange
        Discount discount = new Discount("customer-1", 0);
        BigDecimal originalPrice = BigDecimal.valueOf(100);

        // Act
        BigDecimal discountedPrice = discount.applyTo(originalPrice);

        // Assert
        assertEquals(BigDecimal.valueOf(100.00), discountedPrice);
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
        assertEquals(BigDecimal.valueOf(67.00), discountedPrice);
    }
}
```

### 5. Пример использования

```java
// MainExample.java
package com.onlinestore.loyalty;

import java.math.BigDecimal;
import java.util.Arrays;

public class MainExample {
    public static void main(String[] args) {
        // Создаем репозиторий со скидками
        DiscountRepository discountRepository = new InMemoryDiscountRepository();
        
        // Создаем сервис лояльности
        LoyaltyService loyaltyService = new LoyaltyService(discountRepository);
        
        // Создаем корзину для покупателя
        String customerId = "vip-customer";
        
        Purchase laptop = new Purchase("laptop-123", BigDecimal.valueOf(1500));
        Purchase mouse = new Purchase("mouse-456", BigDecimal.valueOf(50));
        Purchase keyboard = new Purchase("keyboard-789", BigDecimal.valueOf(100));
        
        ShoppingCart cart = new ShoppingCart(customerId, 
                Arrays.asList(laptop, mouse, keyboard));
        
        System.out.println("Оригинальная корзина:");
        System.out.println("Общая стоимость: " + cart.calculateTotalOriginalPrice());
        
        // Применяем скидку
        ShoppingCart discountedCart = loyaltyService.applyDiscount(customerId, cart);
        
        System.out.println("\nКорзина со скидкой:");
        for (Purchase purchase : discountedCart.getPurchases()) {
            System.out.printf("Товар: %s, Оригинальная цена: %.2f, Цена со скидкой: %.2f%n",
                    purchase.getProductId(),
                    purchase.getOriginalPrice(),
                    purchase.getFinalPrice());
        }
        
        System.out.println("Общая стоимость со скидкой: " + discountedCart.calculateTotalFinalPrice());
        
        // Рассчитываем общую скидку
        BigDecimal totalDiscount = loyaltyService.calculateTotalDiscount(customerId, cart);
        System.out.println("Общая скидка: " + totalDiscount);
    }
}
```

## Ключевые моменты реализации:

1. **Принципы SOLID**:
    - `DiscountRepository` - интерфейс для инверсии зависимостей
    - Каждый класс имеет одну ответственность
    - Легко расширять функциональность

2. **Иммутабельность**:
    - Модели данных иммутабельны где это возможно
    - Создаются новые объекты при применении скидок

3. **Точность вычислений**:
    - Используется `BigDecimal` для финансовых расчетов
    - Округление до 2 знаков после запятой

4. **Тестируемость**:
    - Используется Mockito для мокирования зависимостей
    - Параметризованные тесты
    - Покрыты граничные случаи

5. **Безопасность**:
    - Валидация входных данных
    - Проверка соответствия customerId

Это решение демонстрирует хорошие практики разработки и может быть легко расширено (например, добавив разные типы скидок, историю применения и т.д.).

---

Отличный вопрос! Давайте подробно разберем, как реализована иммутабельность в этом решении.

## Что такое иммутабельность?

**Иммутабельный объект** — это объект, состояние которого нельзя изменить после создания. Любая модификация приводит к созданию нового объекта.

## Как реализована иммутабельность в нашем коде:

### 1. **Иммутабельные модели данных (частично)**

```java
public class Purchase {
    private final String productId;          // final - неизменяемое поле
    private final BigDecimal originalPrice;  // final - неизменяемое поле
    private BigDecimal finalPrice;           // НЕ final - изменяемое поле!
    
    // ... остальной код
}
```

Здесь есть **проблема** — поле `finalPrice` не является `final`, и у него есть сеттер. Это **нарушает принцип иммутабельности**. Давайте исправим это:

### Исправленная версия с полной иммутабельностью:

```java
// ImmutablePurchase.java
public class Purchase {
    private final String productId;
    private final BigDecimal originalPrice;
    private final BigDecimal finalPrice;  // Теперь final!

    // Конструктор принимает оба значения
    public Purchase(String productId, BigDecimal originalPrice, BigDecimal finalPrice) {
        this.productId = productId;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
    }

    // Альтернативный конструктор для создания без скидки
    public Purchase(String productId, BigDecimal originalPrice) {
        this(productId, originalPrice, originalPrice);
    }

    // Метод для применения скидки - возвращает НОВЫЙ объект
    public Purchase withDiscount(BigDecimal discountedPrice) {
        return new Purchase(this.productId, this.originalPrice, discountedPrice);
    }

    // Убираем сеттер!
    // public void setFinalPrice(BigDecimal finalPrice) {
    //     this.finalPrice = finalPrice;
    // }

    // Только геттеры
    public String getProductId() { return productId; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public BigDecimal getFinalPrice() { return finalPrice; }
}
```

### 2. **Иммутабельный ShoppingCart**

```java
public class ShoppingCart {
    private final String customerId;          // final
    private final List<Purchase> purchases;   // final, но список внутри изменяем!

    // Конструктор создает защитную копию
    public ShoppingCart(String customerId, List<Purchase> purchases) {
        this.customerId = customerId;
        this.purchases = new ArrayList<>(purchases);  // Защитная копия!
    }
    
    // Возвращаем защитную копию
    public List<Purchase> getPurchases() {
        return new ArrayList<>(purchases);  // Защитная копия!
    }
    
    // Если хотим добавить товар - создаем новый объект
    public ShoppingCart withAddedPurchase(Purchase purchase) {
        List<Purchase> newPurchases = new ArrayList<>(this.purchases);
        newPurchases.add(purchase);
        return new ShoppingCart(this.customerId, newPurchases);
    }
}
```

### 3. **Ключевой момент в LoyaltyService - создание новых объектов**

Давайте посмотрим на исправленный метод в сервисе:

```java
public class LoyaltyService {
    // ...
    
    public ShoppingCart applyDiscount(String customerId, ShoppingCart shoppingCart) {
        // ... валидация
        
        Discount discount = discountRepository.findDiscountByCustomerId(customerId)
                .orElse(new Discount(customerId, 0));

        // ВАЖНО: создаем новый список с НОВЫМИ объектами Purchase
        List<Purchase> discountedPurchases = shoppingCart.getPurchases().stream()
                .map(purchase -> applyDiscountToPurchase(purchase, discount))
                .collect(Collectors.toList());

        // Возвращаем НОВЫЙ объект ShoppingCart
        return new ShoppingCart(customerId, discountedPurchases);
    }

    private Purchase applyDiscountToPurchase(Purchase originalPurchase, Discount discount) {
        // ВАЖНО: не изменяем исходный объект!
        // Вместо этого создаем новый с примененной скидкой
        
        BigDecimal discountedPrice = discount.applyTo(originalPurchase.getOriginalPrice());
        
        // Используем наш метод withDiscount (в исправленной версии)
        return originalPurchase.withDiscount(discountedPrice);
        
        // ИЛИ в старой версии:
        // Purchase discountedPurchase = new Purchase(
        //     originalPurchase.getProductId(),
        //     originalPurchase.getOriginalPrice()
        // );
        // discountedPurchase.setFinalPrice(discountedPrice);
        // return discountedPurchase;
    }
}
```

## Визуализация процесса:

```
Исходная корзина:
ShoppingCart {
  customerId = "user1"
  purchases = [
    Purchase {id="A", original=100, final=100},
    Purchase {id="B", original=200, final=200}
  ]
}

После применения скидки 10%:
ShoppingCart {
  customerId = "user1"
  purchases = [
    Purchase {id="A", original=100, final=90},     ← НОВЫЙ объект
    Purchase {id="B", original=200, final=180}    ← НОВЫЙ объект
  ]
}                                                  ← НОВЫЙ объект

Исходная корзина остается неизменной! ✅
```

## Преимущества такого подхода:

### 1. **Потокобезопасность**
```java
// Можем безопасно использовать в многопоточном окружении
ShoppingCart cart1 = loyaltyService.applyDiscount(userId, originalCart);
ShoppingCart cart2 = loyaltyService.applyDiscount(userId, originalCart);
// Никаких конфликтов, каждый поток работает со своей копией
```

### 2. **Предсказуемость**
```java
ShoppingCart original = createCart();
ShoppingCart discounted = loyaltyService.applyDiscount("user", original);

System.out.println(original.calculateTotalOriginalPrice()); // 300
System.out.println(discounted.calculateTotalFinalPrice());   // 270
// Оригинальная корзина не изменилась!
```

### 3. **Легкое тестирование**
```java
@Test
void testDiscountApplication() {
    ShoppingCart original = createTestCart();
    ShoppingCart result = service.applyDiscount("user", original);
    
    // Гарантируем что оригинал не изменился
    assertEquals(BigDecimal.valueOf(300), original.calculateTotalOriginalPrice());
    assertNotSame(original, result); // Это разные объекты!
}
```

### 4. **Откат изменений**
```java
ShoppingCart original = loadCartFromDatabase();
ShoppingCart withDiscount = loyaltyService.applyDiscount(userId, original);

// Пользователь передумал - просто используем оригинал
saveCartToDatabase(original); // Все как было
```

## Полностью иммутабельная версия сервиса:

```java
public class ImmutableLoyaltyService {
    private final DiscountRepository discountRepository;
    
    // Сервис тоже иммутабельный - поле final
    public ImmutableLoyaltyService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }
    
    // Метод возвращает новый объект, ничего не меняя
    public ShoppingCart applyDiscount(String customerId, ShoppingCart shoppingCart) {
        // ... реализация
    }
    
    // Pure function - зависит только от входных параметров
    public BigDecimal calculatePotentialDiscount(String customerId, ShoppingCart cart) {
        ShoppingCart discounted = applyDiscount(customerId, cart);
        return cart.calculateTotalOriginalPrice()
                .subtract(discounted.calculateTotalFinalPrice());
    }
}
```

## Когда НЕ стоит делать объекты иммутабельными:

1. **Высокопроизводительные системы** — создание множества объектов может быть накладным
2. **Большие объекты** — копирование всего объекта может быть дорогим
3. **Частые обновления** — если объект часто меняется, иммутабельность может создать overhead

## Паттерны для работы с иммутабельными объектами:

### 1. **Builder Pattern** для сложных объектов:
```java
ShoppingCart cart = ShoppingCart.builder()
    .customerId("user1")
    .purchase(purchase1)
    .purchase(purchase2)
    .build();
```

### 2. **Copy-on-write коллекции**:
```java
// Вместо этого (создает копию всего списка):
public List<Purchase> getPurchases() {
    return new ArrayList<>(purchases);
}

// Можно использовать специальные коллекции:
public List<Purchase> getPurchases() {
    return Collections.unmodifiableList(purchases);
}
```

## Вывод:

В нашем решении **иммутабельность реализована через создание новых объектов при любых изменениях**. Это обеспечивает:
- **Безопасность** в многопоточных средах
- **Предсказуемость** поведения
- **Легкость отладки** (нет побочных эффектов)
- **Чистоту функций** (методы не имеют side effects)

Ключевая идея: **"Не изменяй то, что уже существует. Создавай новое на основе старого"**. 
Это фундаментальный принцип функционального программирования, который делает код более надежным и понятным.

---


