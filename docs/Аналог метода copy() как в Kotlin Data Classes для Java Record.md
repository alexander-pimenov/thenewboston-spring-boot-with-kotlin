Отличный вопрос! Давайте подробно разберем, что такое `copy()` в Kotlin и как реализовать аналогичную функциональность в Java Records.

## Метод `copy()` в Kotlin Data Classes

В Kotlin `data class` автоматически генерирует метод `copy()`, который позволяет создавать копию объекта с изменением некоторых свойств:

### **Пример в Kotlin:**

```kotlin
data class Person(val name: String, val age: Int, val city: String)

fun main() {
    val person1 = Person("Alice", 30, "Moscow")
    
    // Копируем с изменением одного поля
    val person2 = person1.copy(city = "London")
    // Person(name=Alice, age=30, city=London)
    
    // Копируем с изменением нескольких полей
    val person3 = person1.copy(age = 31, city = "Berlin")
    // Person(name=Alice, age=31, city=Berlin)
    
    // Полная копия без изменений
    val person4 = person1.copy()
    // Person(name=Alice, age=30, city=Moscow)
}
```

## Почему это полезно?

1. **Иммутабельность** — создаем новые объекты вместо изменения существующих
2. **Удобство** — не нужно переписывать все поля
3. **Безопасность** — оригинальный объект остается неизменным

## В Java Records НЕТ встроенного `copy()`

Java Records не генерируют автоматический метод `copy()`, но мы можем создать его сами несколькими способами:

## Способ 1: Ручное создание "with-методов"

```java
public record Purchase(
    String productId,
    BigDecimal originalPrice,
    BigDecimal finalPrice
) {
    // Конструкторы...
    
    // Методы для копирования с изменениями
    public Purchase withProductId(String newProductId) {
        return new Purchase(newProductId, this.originalPrice, this.finalPrice);
    }
    
    public Purchase withOriginalPrice(BigDecimal newOriginalPrice) {
        return new Purchase(this.productId, newOriginalPrice, this.finalPrice);
    }
    
    public Purchase withFinalPrice(BigDecimal newFinalPrice) {
        return new Purchase(this.productId, this.originalPrice, newFinalPrice);
    }
    
    // Универсальный copy метод (как в Kotlin, но без named parameters)
    public Purchase copy(String productId, BigDecimal originalPrice, BigDecimal finalPrice) {
        return new Purchase(
            productId != null ? productId : this.productId,
            originalPrice != null ? originalPrice : this.originalPrice,
            finalPrice != null ? finalPrice : this.finalPrice
        );
    }
}

// Использование:
Purchase original = new Purchase("laptop", BigDecimal.valueOf(1000), BigDecimal.valueOf(900));

Purchase modified1 = original.withFinalPrice(BigDecimal.valueOf(800));
Purchase modified2 = original.copy(null, BigDecimal.valueOf(1100), null);
```

## Способ 2: Использование Builder Pattern для Records

```java
public record Purchase(
    String productId,
    BigDecimal originalPrice,
    BigDecimal finalPrice
) {
    // Builder для удобного копирования
    public static class Builder {
        private String productId;
        private BigDecimal originalPrice;
        private BigDecimal finalPrice;
        
        private Builder(Purchase source) {
            this.productId = source.productId();
            this.originalPrice = source.originalPrice();
            this.finalPrice = source.finalPrice();
        }
        
        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }
        
        public Builder originalPrice(BigDecimal originalPrice) {
            this.originalPrice = originalPrice;
            return this;
        }
        
        public Builder finalPrice(BigDecimal finalPrice) {
            this.finalPrice = finalPrice;
            return this;
        }
        
        public Purchase build() {
            return new Purchase(productId, originalPrice, finalPrice);
        }
    }
    
    // Метод для создания Builder
    public Builder toBuilder() {
        return new Builder(this);
    }
}

// Использование:
Purchase original = new Purchase("laptop", BigDecimal.valueOf(1000), BigDecimal.valueOf(900));

Purchase modified = original.toBuilder()
    .finalPrice(BigDecimal.valueOf(800))
    .build();
```

## Способ 3: Универсальный `copy()` с varargs (более сложный)

```java
import java.lang.reflect.Constructor;
import java.util.Arrays;

public record Purchase(
    String productId,
    BigDecimal originalPrice,
    BigDecimal finalPrice
) {
    // Универсальный copy метод с reflection (осторожно!)
    public Purchase copy(Object... changes) {
        Object[] currentValues = {productId, originalPrice, finalPrice};
        
        for (int i = 0; i < Math.min(changes.length, currentValues.length); i++) {
            if (changes[i] != null) {
                currentValues[i] = changes[i];
            }
        }
        
        try {
            Constructor<Purchase> constructor = Purchase.class.getDeclaredConstructor(
                String.class, BigDecimal.class, BigDecimal.class
            );
            return constructor.newInstance(currentValues);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create copy", e);
        }
    }
}

// Использование (неудобно):
Purchase modified = original.copy(null, null, BigDecimal.valueOf(800));
```

## Способ 4: Самый элегантный - с использованием Java 16+ и "with-методов"

```java
public record Purchase(
    String productId,
    BigDecimal originalPrice,
    BigDecimal finalPrice
) {
    // Компактный конструктор с валидацией
    public Purchase {
        Objects.requireNonNull(productId, "productId cannot be null");
        Objects.requireNonNull(originalPrice, "originalPrice cannot be null");
        Objects.requireNonNull(finalPrice, "finalPrice cannot be null");
    }
    
    // Статический фабричный метод
    public static Purchase of(String productId, BigDecimal price) {
        return new Purchase(productId, price, price);
    }
    
    // ========== WITH-МЕТОДЫ (аналог copy) ==========
    
    // Для каждого поля создаем with-метод
    public Purchase withProductId(String productId) {
        return new Purchase(productId, this.originalPrice, this.finalPrice);
    }
    
    public Purchase withOriginalPrice(BigDecimal originalPrice) {
        return new Purchase(this.productId, originalPrice, this.finalPrice);
    }
    
    public Purchase withFinalPrice(BigDecimal finalPrice) {
        return new Purchase(this.productId, this.originalPrice, finalPrice);
    }
    
    // Комбинированный with-метод
    public Purchase withDiscount(BigDecimal discount) {
        BigDecimal newFinalPrice = originalPrice.subtract(discount).max(BigDecimal.ZERO);
        return withFinalPrice(newFinalPrice);
    }
    
    // Универсальный with метод (опционально)
    public Purchase with(
            Optional<String> productId,
            Optional<BigDecimal> originalPrice,
            Optional<BigDecimal> finalPrice) {
        return new Purchase(
            productId.orElse(this.productId),
            originalPrice.orElse(this.originalPrice),
            finalPrice.orElse(this.finalPrice)
        );
    }
}

// Использование:
Purchase laptop = Purchase.of("laptop", BigDecimal.valueOf(1000));

// Серия преобразований (fluent interface)
Purchase modified = laptop
    .withFinalPrice(BigDecimal.valueOf(900))
    .withProductId("laptop-pro");
```

## Способ 5: Использование библиотек

### **Lombok с Java Records (экспериментально):**
```java
import lombok.With;

public record Purchase(
    String productId,
    @With BigDecimal originalPrice,  // Генерирует withOriginalPrice()
    @With BigDecimal finalPrice      // Генерирует withFinalPrice()
) {
    // Lombok автоматически сгенерирует with-методы для аннотированных полей
}
```

### **Apache Commons Lang3:**
```java
import org.apache.commons.lang3.builder.Builder;

public record Purchase(
    String productId,
    BigDecimal originalPrice,
    BigDecimal finalPrice
) implements Builder<Purchase> {
    
    @Override
    public Purchase build() {
        return this;
    }
    
    // Можно использовать ReflectionToStringBuilder и др.
}
```

## Полный пример системы с with-методами:

```java
// 1. Discount.java
public record Discount(String customerId, int percentage) {
    public Discount {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Invalid discount: " + percentage);
        }
    }
    
    // with-методы
    public Discount withCustomerId(String customerId) {
        return new Discount(customerId, this.percentage);
    }
    
    public Discount withPercentage(int percentage) {
        return new Discount(this.customerId, percentage);
    }
    
    // Специальный with-метод для бизнес-логики
    public Discount withAdditionalPercentage(int additionalPercent) {
        int newPercentage = Math.min(100, this.percentage + additionalPercent);
        return withPercentage(newPercentage);
    }
}

// 2. Purchase.java  
public record Purchase(
    String productId,
    BigDecimal originalPrice,
    BigDecimal finalPrice
) {
    public Purchase {
        Objects.requireNonNull(productId);
        Objects.requireNonNull(originalPrice);
        Objects.requireNonNull(finalPrice);
    }
    
    public Purchase(String productId, BigDecimal price) {
        this(productId, price, price);
    }
    
    // with-методы
    public Purchase withProductId(String productId) {
        return new Purchase(productId, this.originalPrice, this.finalPrice);
    }
    
    public Purchase withOriginalPrice(BigDecimal originalPrice) {
        return new Purchase(this.productId, originalPrice, this.finalPrice);
    }
    
    public Purchase withFinalPrice(BigDecimal finalPrice) {
        return new Purchase(this.productId, this.originalPrice, finalPrice);
    }
    
    // Специальный with-метод для скидок
    public Purchase withDiscount(Discount discount) {
        BigDecimal newFinalPrice = discount.applyTo(this.originalPrice);
        return withFinalPrice(newFinalPrice);
    }
    
    // Удобный copy (аналог Kotlin, но с билдером)
    public Builder copy() {
        return new Builder(this);
    }
    
    // Вложенный Builder
    public static class Builder {
        private String productId;
        private BigDecimal originalPrice;
        private BigDecimal finalPrice;
        
        private Builder(Purchase source) {
            this.productId = source.productId();
            this.originalPrice = source.originalPrice();
            this.finalPrice = source.finalPrice();
        }
        
        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }
        
        public Builder originalPrice(BigDecimal originalPrice) {
            this.originalPrice = originalPrice;
            return this;
        }
        
        public Builder finalPrice(BigDecimal finalPrice) {
            this.finalPrice = finalPrice;
            return this;
        }
        
        public Purchase build() {
            return new Purchase(productId, originalPrice, finalPrice);
        }
    }
}

// 3. ShoppingCart.java
public record ShoppingCart(String customerId, List<Purchase> purchases) {
    public ShoppingCart {
        purchases = List.copyOf(Objects.requireNonNull(purchases));
    }
    
    public ShoppingCart(String customerId) {
        this(customerId, List.of());
    }
    
    // with-методы
    public ShoppingCart withCustomerId(String customerId) {
        return new ShoppingCart(customerId, this.purchases);
    }
    
    public ShoppingCart withPurchases(List<Purchase> purchases) {
        return new ShoppingCart(this.customerId, purchases);
    }
    
    // Специальные with-методы
    public ShoppingCart withAddedPurchase(Purchase purchase) {
        List<Purchase> newPurchases = new ArrayList<>(this.purchases);
        newPurchases.add(purchase);
        return withPurchases(newPurchases);
    }
    
    public ShoppingCart withRemovedPurchase(String productId) {
        List<Purchase> newPurchases = this.purchases.stream()
            .filter(p -> !p.productId().equals(productId))
            .toList();
        return withPurchases(newPurchases);
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
```

## Обновленный LoyaltyService с with-методами:

```java
public class LoyaltyService {
    private final DiscountRepository discountRepository;
    
    public ShoppingCart applyDiscount(String customerId, ShoppingCart cart) {
        Discount discount = discountRepository.findDiscountByCustomerId(customerId)
            .orElse(new Discount(customerId, 0));
        
        // Функциональный стиль с with-методами
        List<Purchase> discountedPurchases = cart.purchases().stream()
            .map(purchase -> purchase.withDiscount(discount))
            .toList();
        
        return cart.withPurchases(discountedPurchases);
    }
    
    // Или с использованием copy-билдера
    public ShoppingCart applyDiscountWithBuilder(String customerId, ShoppingCart cart) {
        Discount discount = discountRepository.findDiscountByCustomerId(customerId)
            .orElse(new Discount(customerId, 0));
        
        ShoppingCart.CartBuilder builder = cart.copy();
        
        for (Purchase purchase : cart.purchases()) {
            Purchase discounted = purchase.copy()
                .finalPrice(discount.applyTo(purchase.originalPrice()))
                .build();
            builder.addPurchase(discounted);
        }
        
        return builder.build();
    }
}
```

## Ключевые отличия от Kotlin:

| Аспект | Kotlin `copy()` | Java with-методы |
|--------|-----------------|------------------|
| Синтаксис | `obj.copy(prop = value)` | `obj.withProp(value)` |
| Named arguments | Да (удобно) | Нет (в Java нет named args) |
| Null-обработка | Нужно явно передавать | Можно сделать опциональной |
| Генерация | Автоматическая | Ручная или через Lombok |
| Цепочка вызовов | `copy().copy()` | `withX().withY()` |

## Вывод:

**Рекомендую использовать "with-методы"** (Способ 4) для Java Records. Это:
1. **Идиоматично для Java** (следует convention over configuration)
2. **Типобезопасно** — компилятор проверяет типы
3. **Производительно** — нет reflection overhead
4. **Читаемо** — ясно видно, какое поле изменяется

Хотя это требует больше кода, чем Kotlin `copy()`, это дает лучший контроль и явность, что ценится в enterprise Java-разработке.