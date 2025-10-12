## Инструментация (Instrumentation)
**Инструментация** (Instrumentation) - это очень мощная и интересная техника, которая лежит в основе многих библиотек, включая MockK.

## 🎯 Что такое инструментация?

**Инструментация** - это возможность модифицировать байткод Java-классов во время их загрузки или выполнения.

Представьте, что вы можете "переписать" код классов прямо во время работы программы!

## 🔧 Как это работает:

```
`Исходный класс` → `Байткод` → `[Инструментация]` → `Модифицированный байткод` → `JVM`
```

## 🎯 Конкретно для MockK:

### **Без инструментации:**
```java
// Ваш оригинальный класс
class UserService {
    public User findUser(String id) {
        // Реальная логика
        return database.findUser(id);
    }
}
```

### **С инструментацией MockK:**
```java
// После инструментации (условно)
class UserService$MockK {
    public User findUser(String id) {
        if (MockK.isMock(this)) {
            return MockK.handleMethodCall(this, "findUser", id); // Перехват вызова!
        } else {
            // Оригинальная логика
            return database.findUser(id);
        }
    }
}
```

## 🛠️ Основные подходы к инструментации:

### 1. **Java Agent** (самый мощный)
```kotlin
// MockK использует именно этот подход
class MockKAgent : ClassFileTransformer {
    override fun transform(
        loader: ClassLoader?,
        className: String?,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain?,
        classfileBuffer: ByteArray
    ): ByteArray {
        // Анализируем и модифицируем байткод
        return transformBytecode(classfileBuffer)
    }
}
```

### 2. **Byte Buddy / ASM** (библиотеки для манипуляции байткодом)
```java
// Пример как MockK мог бы создавать моки
new ByteBuddy()
    .subclass(UserService.class)
    .method(ElementMatchers.any())
    .intercept(MethodDelegation.to(MockKInterceptor.class))
    .make()
    .load(classLoader, ClassLoadingStrategy.Default.INJECTION);
```

## 🔍 Что конкретно делает MockK при инструментации:

### **1. Создание классов-заглушек**
```java
// Было:
class RealService {
    String doWork() { return "real"; }
}

// Стало (условно):
class RealService$MockK extends RealService {
    @Override
    String doWork() {
        // Перехватываем вызов и возвращаем mock-результат
        return MockKRegistry.getMockBehavior(this, "doWork");
    }
}
```

### **2. Подмена вызовов методов**
```kotlin
// Ваш тест
val mock = mockk<MyService>()
every { mock.someMethod() } returns "mocked result"

// При вызове mock.someMethod() происходит:
// 1. Перехват вызова инструментированным кодом
// 2. Проверка: "а не mock ли это?"
// 3. Поиск зарегистрированного поведения
// 4. Возврат mocked результата
```

## 🎯 Почему это приводит к дедлокам в параллельных тестах:

### **Проблема:**
```kotlin
// Поток 1 начинает создавать mock класса A
// Поток 2 начинает создавать mock класса B

// Оба потока пытаются:
// 1. Загрузить класс через ClassLoader ✓
// 2. Инструментировать класс (модифицировать байткод) ❌ БЛОКИРОВКА!
//    ↓
// ClassLoader синхронизирован внутри!
```

### **Визуализация дедлока:**
```
Поток 1: [ClassLoader A] → Ждет [ClassLoader B] ❌
Поток 2: [ClassLoader B] → Ждет [ClassLoader A] ❌
              ↓
          ДЕДЛОК!
```

## 🛠️ Как ваше решение помогает:

```kotlin
object MockMonitor 

inline fun <reified T> syncMockK(): T {
    synchronized(MockMonitor) { // ← СИНХРОНИЗАЦИЯ!
        return mockk() // Инструментация происходит внутри synchronized
    }
}
```

**Теперь:**
```
Поток 1: [synchronized] → [ClassLoader A] → ✅ Готово
Поток 2: Ждет → [synchronized] → [ClassLoader B] → ✅ Готово
```

## 💡 Другие примеры инструментации:

### **1. Логирование вызовов**
```java
// Было:
public void businessMethod() {
    // бизнес-логика
}

// После инструментации:
public void businessMethod() {
    logger.debug("businessMethod started");
    // бизнес-логика  
    logger.debug("businessMethod finished");
}
```

### **2. Мониторинг производительности**
```java
public void slowMethod() {
    long start = System.nanoTime();
    // оригинальный код
    long duration = System.nanoTime() - start;
    metrics.record("slowMethod", duration);
}
```

### **3. Транзакции**
```java
@Transactional
public void updateData() {
    // После инструментации:
    // TransactionManager.begin();
    // оригинальный код
    // TransactionManager.commit();
}
```

## 🏆 Итог:

**Инструментация** - это "магия", которая позволяет библиотекам вроде MockK перехватывать и модифицировать поведение ваших 
классов на лету. Это мощно, но требует осторожности с многопоточностью - именно поэтому ваше решение с `synchronized` так эффективно! 🚀

---
