Давай разберём что такое монитор и почему мы используем `attempts`. 🎯

## 🔍 **ЧТО ТАКОЕ МОНИТОР В synchronized?**

**Монитор** - это объект, который используется как "замок" для синхронизации. 
Когда поток входит в `synchronized(объект)`, он "захватывает" монитор этого объекта.

### Простая аналогия:
```kotlin
// 🚪 Представь комнату с одним ключом:
// - synchronized(room) = взял ключ от комнаты
// - Пока у тебя ключ - другие ждут
// - Выйдя из комнаты - возвращаешь ключ
```

## 🎯 **ПОЧЕМУ ИМЕННО `attempts`?**

### Варианты мониторов:
```kotlin
// 1. ❌ Специальный объект-замок
private val lock = Object()
synchronized(lock) { ... }

// 2. ✅ Сам объект, который защищаем  
synchronized(attempts) { ... }

// 3. ❌ this (опасно!)
synchronized(this) { ... }
```

### Почему `attempts` - хороший выбор:

```kotlin
class SimpleRateLimiter {
    private val attempts = ConcurrentHashMap<String, MutableList<Instant>>()
    
    fun tryAcquire(key: String): Boolean {
        // ✅ ХОРОШО: synchronized на том, что защищаем
        synchronized(attempts) {
            val keyAttempts = attempts.getOrPut(key) { mutableListOf() }
            // ...
        }
    }
}
```

**Преимущества:**
- **Ясность** - сразу видно что защищаем `attempts`
- **Минимальная блокировка** - блокируем только доступ к `attempts`
- **Безопасность** - не блокируем весь объект

## 🚨 **ПРОБЛЕМА С `synchronized(this)`:**

```kotlin
class SimpleRateLimiter {
    private val attempts = ConcurrentHashMap<String, MutableList<Instant>>()
    
    fun tryAcquire(key: String): Boolean {
        // ❌ ОПАСНО: synchronized на this
        synchronized(this) {
            val keyAttempts = attempts.getOrPut(key) { mutableListOf() }
            // ...
        }
    }
    
    fun otherMethod() {
        // Этот метод тоже будет блокироваться!
        // Даже если он не связан с attempts
    }
}
```

**Проблема:** `synchronized(this)` блокирует **весь объект**, а не только доступ к `attempts`!

## 🔄 **КАК ЭТО РАБОТАЕТ НА ПРАКТИКЕ:**

### Сценарий с двумя потоками:
```kotlin
val limiter = SimpleRateLimiter()

// Поток 1:
thread {
    limiter.tryAcquire("user1") // Захватывает монитор attempts
    // Другие потоки ждут пока этот не выйдет из synchronized
}

// Поток 2:  
thread {
    limiter.tryAcquire("user2") // Ждёт пока поток 1 освободит монитор
}
```

## 🧪 **НАГЛЯДНЫЙ ПРИМЕР:**

```kotlin
class BankAccount(val name: String) {
    private val transactions = mutableListOf<String>()
    private val lock = Object() // Специальный объект-замок
    
    fun deposit(amount: Int) {
        synchronized(lock) {  // ✅ Захватываем монитор lock
            transactions.add("Deposit: +$amount")
            println("$name: deposited $amount (thread: ${Thread.currentThread().name})")
            Thread.sleep(100) // Имитируем работу
        }
    }
    
    fun getBalance(): Int {
        synchronized(lock) {  // ✅ Тот же монитор - тот же замок
            return transactions.size * 100 // упрощённо
        }
    }
}

fun main() {
    val account = BankAccount("Alice")
    
    // Два потока пытаются работать с одним счётом
    val thread1 = Thread { repeat(3) { account.deposit(100) } }
    val thread2 = Thread { repeat(3) { account.deposit(200) } }
    
    thread1.start()
    thread2.start()
    
    thread1.join()
    thread2.join()
    
    println("Final balance: ${account.getBalance()}")
}
```

**Вывод:**
```
Alice: deposited 100 (thread: Thread-0)
Alice: deposited 100 (thread: Thread-0) 
Alice: deposited 100 (thread: Thread-0)
Alice: deposited 200 (thread: Thread-1)
Alice: deposited 200 (thread: Thread-1)
Alice: deposited 200 (thread: Thread-1)
Final balance: 600
```

## 🎯 **ВОЗВРАЩАЯСЬ К НАШЕМУ RATE LIMITER:**

```kotlin
class SimpleRateLimiter {
    private val attempts = ConcurrentHashMap<String, MutableList<Instant>>()
    
    override fun tryAcquire(key: String): Boolean {
        val now = Instant.now()
        val windowStart = now.minusSeconds(60)
        
        // 🔒 Захватываем монитор объекта attempts
        synchronized(attempts) {
            // В этом блоке только ОДИН поток может работать с attempts
            val keyAttempts = attempts.getOrPut(key) { mutableListOf() }
            
            // Удаляем старые попытки
            keyAttempts.removeAll { it.isBefore(windowStart) }
            
            // Добавляем текущую попытку
            keyAttempts.add(now)
            
            // Проверяем лимит
            return keyAttempts.size < 5
        }
        // 🔓 Монитор автоматически освобождается при выходе из блока
    }
}
```

## 🔧 **АЛЬТЕРНАТИВНЫЕ ПОДХОДЫ:**

### Вариант 1: **Отдельный lock объект**
```kotlin
class SimpleRateLimiter {
    private val attempts = ConcurrentHashMap<String, MutableList<Instant>>()
    private val lock = Object() // Специальный объект только для синхронизации
    
    fun tryAcquire(key: String): Boolean {
        synchronized(lock) {  // ✅ Чистая синхронизация
            val keyAttempts = attempts.getOrPut(key) { mutableListOf() }
            // ...
        }
    }
}
```

### Вариант 2: **ConcurrentHashMap + compute**
```kotlin
class SimpleRateLimiter {
    private val attempts = ConcurrentHashMap<String, MutableList<Instant>>()
    
    fun tryAcquire(key: String): Boolean {
        // ✅ Без synchronized, используем атомарные операции ConcurrentHashMap
        attempts.compute(key) { _, currentList ->
            val list = currentList ?: mutableListOf()
            list.removeAll { it.isBefore(Instant.now().minusSeconds(60)) }
            list.add(Instant.now())
            list
        }
        
        return attempts[key]!!.size < 5
    }
}
```

## 🎯 **ПОЧЕМУ МЫ ВЫБРАЛИ synchronized(attempts):**

1. **Простота** - легко понять что защищаем
2. **Ясность** - видна связь между защищаемым ресурсом и синхронизацией
3. **Достаточность** - защищает именно то, что нужно
4. **Идиоматичность** - распространённый паттерн в Java/Kotlin

## ⚠️ **ВАЖНЫЕ МОМЕНТЫ:**

### Монитор должен быть **private**!
```kotlin
class BadExample {
    public val lock = Object() // ❌ ОПАСНО!
    
    fun method() {
        synchronized(lock) { ... }
    }
}

// Извне можно сделать:
synchronized(badExample.lock) {
    // Заблокировали весь badExample!
}
```

### Избегай **вложенных synchronized**!
```kotlin
synchronized(lock1) {
    synchronized(lock2) {  // ❌ ОПАСНО! Может привести к deadlock!
        // ...
    }
}
```

## ✅ **ВЫВОД:**

**`synchronized(attempts)` означает:**
- "Захватываю монитор объекта `attempts`"
- "Пока я в этом блоке, другие потоки не могут войти в synchronized с тем же объектом"
- "Защищаю именно доступ к `attempts`, а не ко всему классу"

Понимание мониторов - ключ к написанию корректных многопоточных приложений! 💪

---