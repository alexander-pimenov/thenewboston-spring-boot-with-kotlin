## Что такое Records в Java?

**Records** — это специальный тип класса, представленный в Java 16 (JEP 395), который предназначен именно для DTO (Data Transfer Objects). Это **неизменяемые** (immutable) классы, которые автоматически генерируют:
- Конструктор со всеми полями
- Геттеры (но без префикса `get`)
- Методы `equals()`, `hashCode()`, `toString()`

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