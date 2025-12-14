Отличный вопрос! Вы абсолютно правы — в Java 21 есть гораздо более современные способы создания DTO, и главный из них — **Records** (запись, протокол), которые очень похожи на Kotlin `data class`.

## Что такое Records в Java?

**Records** — это специальный тип класса, представленный в Java 16 (JEP 395), который предназначен именно для DTO (Data Transfer Objects). Это **неизменяемые** (immutable) классы, которые автоматически генерируют:
- Конструктор со всеми полями
- Геттеры (но без префикса `get`)
- Методы `equals()`, `hashCode()`, `toString()`

## Сравним старый подход и Records:

### **1. Discount - старая версия vs Record**

**Старая версия (25 строк):**
```java
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

    public String getCustomerId() { return customerId; }
    public int getPercentage() { return percentage; }
    
    // + equals(), hashCode(), toString() ...
}
```

**Record версия (8 строк!):**
```java
public record Discount(String customerId, int percentage) {
    // Compact constructor для валидации
    public Discount {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException(
                "Discount percentage must be between 0 and 100, got: " + percentage
            );
        }
    }
    
    // Метод остается тем же
    public BigDecimal applyTo(BigDecimal price) {
        BigDecimal discountMultiplier = BigDecimal.valueOf(1 - percentage / 100.0);
        return price.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
```

### **2. Purchase - старая версия vs Record**

**Старая версия (40+ строк):**
```java
public class Purchase {
    private final String productId;
    private final BigDecimal originalPrice;
    private BigDecimal finalPrice;
    
    // Конструкторы, геттеры, сеттер для finalPrice,
    // equals, hashCode, toString...
}
```

**Record версия с паттерном "with-метод" (15 строк):**
```java
public record Purchase(
    String productId,
    BigDecimal originalPrice,
    BigDecimal finalPrice
) {
    // Компактный конструктор
    public Purchase {
        Objects.requireNonNull(productId, "productId cannot be null");
        Objects.requireNonNull(originalPrice, "originalPrice cannot be null");
        Objects.requireNonNull(finalPrice, "finalPrice cannot be null");
        
        if (originalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Final price cannot be negative");
        }
    }
    
    // Конструктор без скидки (finalPrice = originalPrice)
    public Purchase(String productId, BigDecimal originalPrice) {
        this(productId, originalPrice, originalPrice);
    }
    
    // "with-метод" для иммутабельного обновления
    public Purchase withDiscount(BigDecimal newFinalPrice) {
        return new Purchase(this.productId, this.originalPrice, newFinalPrice);
    }
}
```

### **3. ShoppingCart - особая ситуация**

Для `ShoppingCart` ситуация сложнее, так как он содержит список. Records хорошо подходят для **простых DTO**, но для более сложных объектов с коллекциями нужно быть осторожнее:

**Record версия ShoppingCart:**
```java
public record ShoppingCart(String customerId, List<Purchase> purchases) {
    // Защитная копия в конструкторе
    public ShoppingCart {
        purchases = List.copyOf(purchases); // Создаем неизменяемую копию
    }
    
    // Дополнительные методы
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
    
    // Метод для добавления покупки (создает новый объект)
    public ShoppingCart withAddedPurchase(Purchase purchase) {
        List<Purchase> newPurchases = new ArrayList<>(this.purchases);
        newPurchases.add(purchase);
        return new ShoppingCart(this.customerId, newPurchases);
    }
}
```

## Полная переписанная система с Records:

### **Record-версия всей системы:**

```java
// 1. Discount.java
package com.onlinestore.loyalty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Discount(String customerId, int percentage) {
    public Discount {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException(
                "Discount percentage must be between 0 and 100, got: " + percentage
            );
        }
    }
    
    public BigDecimal applyTo(BigDecimal price) {
        BigDecimal discountMultiplier = BigDecimal.valueOf(1 - percentage / 100.0);
        return price.multiply(discountMultiplier)
                   .setScale(2, RoundingMode.HALF_UP);
    }
}

// 2. Purchase.java
package com.onlinestore.loyalty;

import java.math.BigDecimal;
import java.util.Objects;

public record Purchase(
    String productId,
    BigDecimal originalPrice,
    BigDecimal finalPrice
) {
    public Purchase {
        Objects.requireNonNull(productId);
        Objects.requireNonNull(originalPrice);
        Objects.requireNonNull(finalPrice);
        
        if (originalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Final price cannot be negative");
        }
    }
    
    public Purchase(String productId, BigDecimal originalPrice) {
        this(productId, originalPrice, originalPrice);
    }
    
    public Purchase withDiscount(BigDecimal newFinalPrice) {
        return new Purchase(this.productId, this.originalPrice, newFinalPrice);
    }
}

// 3. ShoppingCart.java
package com.onlinestore.loyalty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record ShoppingCart(String customerId, List<Purchase> purchases) {
    public ShoppingCart {
        // Создаем защитную неизменяемую копию
        purchases = List.copyOf(Objects.requireNonNull(purchases));
    }
    
    public ShoppingCart(String customerId) {
        this(customerId, List.of());
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
    
    public ShoppingCart withAddedPurchase(Purchase purchase) {
        List<Purchase> newPurchases = new ArrayList<>(this.purchases);
        newPurchases.add(purchase);
        return new ShoppingCart(this.customerId, newPurchases);
    }
    
    public ShoppingCart withPurchases(List<Purchase> newPurchases) {
        return new ShoppingCart(this.customerId, newPurchases);
    }
}
```

### **Обновленный LoyaltyService для работы с Records:**

```java
package com.onlinestore.loyalty;

import java.math.BigDecimal;
import java.util.List;

public class LoyaltyService {
    private final DiscountRepository discountRepository;

    public LoyaltyService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }
    
    public ShoppingCart applyDiscount(String customerId, ShoppingCart shoppingCart) {
        if (!customerId.equals(shoppingCart.customerId())) {
            throw new IllegalArgumentException(
                "Customer ID does not match shopping cart customer ID"
            );
        }
        
        Discount discount = discountRepository.findDiscountByCustomerId(customerId)
                .orElse(new Discount(customerId, 0));
        
        // Преобразуем purchases с помощью Stream API
        List<Purchase> discountedPurchases = shoppingCart.purchases().stream()
                .map(purchase -> applyDiscountToPurchase(purchase, discount))
                .toList();  // Java 16+ вместо collect(Collectors.toList())
        
        // Создаем новый ShoppingCart record
        return shoppingCart.withPurchases(discountedPurchases);
    }
    
    private Purchase applyDiscountToPurchase(Purchase purchase, Discount discount) {
        BigDecimal discountedPrice = discount.applyTo(purchase.originalPrice());
        return purchase.withDiscount(discountedPrice);
    }
    
    public BigDecimal calculateTotalDiscount(String customerId, ShoppingCart shoppingCart) {
        ShoppingCart discountedCart = applyDiscount(customerId, shoppingCart);
        return shoppingCart.calculateTotalOriginalPrice()
                .subtract(discountedCart.calculateTotalFinalPrice());
    }
}
```

## Особенности Records:

### **1. Автоматически генерируются:**
```java
Discount discount = new Discount("user1", 10);

// Геттеры без 'get':
String id = discount.customerId();  // не getCustomerId()!
int percent = discount.percentage();

// Автоматические equals/hashCode:
Discount d1 = new Discount("user1", 10);
Discount d2 = new Discount("user1", 10);
System.out.println(d1.equals(d2));  // true!

// Автоматический toString():
System.out.println(discount); 
// Discount[customerId=user1, percentage=10]
```

### **2. Компактный конструктор (compact constructor):**
```java
public record Point(int x, int y) {
    // Компактный конструктор - нет параметров!
    // Они уже определены в заголовке record
    public Point {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Coordinates must be positive");
        }
        // Неявно: this.x = x; this.y = y;
    }
}
```

### **3. Records — это final классы:**
```java
public record Discount(String customerId, int percentage) {
    // Нельзя наследоваться от records
    // Нельзя наследовать records от других классов (кроме java.lang.Record)
}
```

### **4. Можно добавлять статические методы и поля:**
```java
public record Discount(String customerId, int percentage) {
    // Статическое поле
    private static final int MAX_DISCOUNT = 50;
    
    // Статический фабричный метод
    public static Discount of(String customerId, int percentage) {
        return new Discount(customerId, Math.min(percentage, MAX_DISCOUNT));
    }
    
    // Статический метод
    public static boolean isValidPercentage(int percentage) {
        return percentage >= 0 && percentage <= 100;
    }
}
```

## Новые фичи Java 21 для DTO:

### **1. Pattern Matching для Records (Java 21):**
```java
// Pattern Matching в switch
public String getDiscountDescription(Discount discount) {
    return switch (discount) {
        case Discount(String id, int p) when p == 0 -> 
            "No discount for customer " + id;
        case Discount(String id, int p) when p > 0 && p <= 10 -> 
            "Small discount for customer " + id;
        case Discount(String id, int p) when p > 10 -> 
            "Large discount for customer " + id;
        default -> "Unknown discount";
    };
}

// Deconstruction pattern
if (discount instanceof Discount(String id, int percent)) {
    System.out.println("Customer " + id + " has " + percent + "% discount");
}
```

### **2. Sealed Interfaces + Records для DTO:**
```java
// Sealed hierarchy для разных типов скидок
public sealed interface DiscountType 
    permits PercentageDiscount, FixedDiscount, FreeShipping {
    
    BigDecimal apply(BigDecimal price);
}

public record PercentageDiscount(int percentage) implements DiscountType {
    public BigDecimal apply(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(1 - percentage / 100.0));
    }
}

public record FixedDiscount(BigDecimal amount) implements DiscountType {
    public BigDecimal apply(BigDecimal price) {
        return price.subtract(amount).max(BigDecimal.ZERO);
    }
}

public record FreeShipping() implements DiscountType {
    public BigDecimal apply(BigDecimal price) {
        return price; // Бесплатная доставка обрабатывается отдельно
    }
}
```

### **3. Records с локальными классами (Java 16+):**
```java
public class LoyaltyService {
    // Локальный record внутри метода
    public ShoppingCart applyComplexDiscount(String customerId, ShoppingCart cart) {
        // Временный DTO для расчета
        record DiscountCalculation(Purchase purchase, BigDecimal discountAmount) {
            BigDecimal finalPrice() {
                return purchase.originalPrice().subtract(discountAmount);
            }
        }
        
        List<DiscountCalculation> calculations = cart.purchases().stream()
            .map(p -> new DiscountCalculation(p, calculateDiscountFor(p)))
            .toList();
        
        // ...
    }
}
```

## Сравнение с Kotlin Data Classes:

| Feature | Java Record | Kotlin Data Class |
|---------|-------------|-------------------|
| Неизменяемость | Все поля final | Можно изменяемые |
| Наследование | Нельзя наследовать | Можно наследовать |
| Геттеры | `discount.percentage()` | `discount.percentage` |
| Copy метод | Нет (нужно вручную) | Есть: `copy()` |
| Дефолтные значения | Нет | Есть |
| Компонентные функции | Автоматически | Автоматически |
| Валидация | В компактном конструкторе | В init блоке |

## Когда использовать Records:

**✅ Идеально для:**
- Простых DTO (как в нашей системе лояльности)
- Value Objects (неизменяемые объекты-значения)
- Результаты методов
- Ключи в Map
- Данные для передачи между слоями

**❌ Не подходят для:**
- Сложных объектов с бизнес-логикой
- Изменяемых объектов (Mutable)
- Объектов с наследованием
- Когда нужны сеттеры
- JPA Entity (не все фреймворки поддерживают)

## Вывод:

Для вашей системы лояльности **Records — идеальный выбор!** Они:
1. **Уменьшают код в 2-3 раза**
2. **Гарантируют иммутабельность**
3. **Автоматически реализуют equals/hashCode/toString**
4. **Более выразительный и безопасный код**
5. **Лучше для многопоточности**

Переход на Records сделает ваш код чище, безопаснее и современнее!