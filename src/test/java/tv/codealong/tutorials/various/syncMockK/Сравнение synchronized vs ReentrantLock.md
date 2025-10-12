🎯 Что такое ReentrantLock?
- `ReentrantLock` - это класс из пакета `java.util.concurrent.locks`, который предоставляет расширенные возможности по сравнению с `synchronized`.

### 📊 Сравнение synchronized vs ReentrantLock

| Возможность               | `synchronized` | `ReentrantLock` |
|---------------------------|----------------|------------------|
| Базовая синхронизация     | ✅             | ✅               |
| Reentrancy (повторное вхождение) | ✅         | ✅               |
| Таймауты                  | ❌             | ✅               |
| Прерываемость             | ❌             | ✅               |
| Честность (fairness)      | ❌             | ✅               |
| Условные переменные       | ✅ (wait/notify)| ✅ (более гибко) |
| Try-lock                  | ❌             | ✅               |

### 🚀 Когда использовать ReentrantLock?
#### 1. Когда нужны таймауты (избежание дедлоков)
```kotlin
import java.util.concurrent.locks.ReentrantLock

val lock = ReentrantLock()

fun tryDoSomething(): Boolean {
    return if (lock.tryLock(1, TimeUnit.SECONDS)) { // Ждем максимум 1 секунду
        try {
            // Критическая секция
            true // Успех
        } finally {
            lock.unlock()
        }
    } else {
        false // Не удалось получить lock
    }
}
```

#### 2. Прерываемые блокировки
```kotlin
fun interruptibleWork() {
    try {
        lock.lockInterruptibly() // Можно прервать ожидание
        try {
            // Критическая секция
        } finally {
            lock.unlock()
        }
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        // Обработка прерывания
    }
}
```
#### 3. Честные (fair) блокировки
```kotlin
val fairLock = ReentrantLock(true) // Честный lock (FIFO)

fun fairWork() {
    fairLock.lock()
    try {
        // Критическая секция - потоки получают доступ в порядке очереди
    } finally {
        fairLock.unlock()
    }
}
```
#### 4. Неблокирующие попытки
```kotlin
fun nonBlockingTry() {
    if (lock.tryLock()) { // Пытаемся получить lock без ожидания
        try {
            // Критическая секция
        } finally {
            lock.unlock()
        }
    } else {
        // Делаем что-то другое если lock занят
    }
}
```

---

### ⚠️ Важные предупреждения!
#### 1. Всегда используйте try-finally!
```kotlin
// ❌ ОПАСНО! Если выбросится исключение - lock никогда не освободится
lock.lock()
// код...
lock.unlock()

// ✅ ПРАВИЛЬНО
lock.lock()
try {
    // код...
} finally {
    lock.unlock()
}
```
#### 2. Не забывайте про unlock()
```kotlin
// ❌ Забудете unlock() - будет дедлок
fun dangerous() {
    lock.lock()
    // что-то делаем...
    // забыли unlock() - ресурс заблокирован навсегда
}

// ✅ Используйте withLock extension в Kotlin
fun safeWithExtension() {
    lock.withLock { // Автоматически unlock в finally
        // код...
    }
}
```

---

### 🎯 Когда выбрать что?
#### ✅ synchronized - когда:
- Простая синхронизация
- Не нужны advanced фичи
- Хочется простоты и читаемости
- Автоматическое управление памятью

#### ✅ ReentrantLock - когда:
- Нужны таймауты (избежание дедлоков)
- Нужна прерываемость ожидания
- Требуется честное распределение lock'ов
- Нужен try-lock без блокировки
- Сложная логика с условными переменными

---

### 💡 Kotlin-way: extension-функции
```kotlin
// Создаем удобные extension-функции
fun <T> ReentrantLock.withLock(action: () -> T): T {
    lock()
    try {
        return action()
    } finally {
        unlock()
    }
}

// Использование
fun example() {
    MockLock.lock.withLock {
        val mock = mockk<MyService>()
        // работа с mock
    }
}
```

---

