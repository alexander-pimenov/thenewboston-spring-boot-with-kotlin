Спроектируем систему кеширования с TTL. Это прекрасная возможность показать процесс мышления при проектировании. 🧠

## 🎯 **ЗАДАЧА: In-Memory Cache с TTL**

```kotlin
/**
 * Требования:
 * 1. Хранение пар ключ-значение в памяти
 * 2. Автоматическое удаление просроченных записей (TTL)
 * 3. Thread-safe доступ
 * 4. Метрики hit/miss
 * 5. Поддержка разных стратегий вытеснения (LRU, FIFO)
 * 6. Ограничение по размеру памяти
 */
```

## 🔍 **ШАГ 1: Декомпозиция задачи**

Давай разобьём на подзадачи:

```kotlin
// 1. 📦 Хранение данных: как хранить пары ключ-значение + TTL?
// 2. ⏰ Управление временем жизни: как отслеживать и удалять просроченное?
// 3. 🔒 Безопасность: как сделать thread-safe?
// 4. 📊 Метрики: как считать hit/miss?
// 5. 🗑️ Вытеснение: что делать при переполнении?
// 6. ⚙️ Конфигурация: настройки TTL, размера, стратегий
```

## 🏗️ **ШАГ 2: Определение сущностей**

### Вопрос 1: **"Что представляет собой запись в кеше?"**

```kotlin
// Запись должна хранить:
// - Значение (любого типа)
// - Время создания (для TTL)
// - Время последнего доступа (для LRU)
// - Размер данных (для ограничения памяти)

// ✅ Решение: data class для записи
data class CacheEntry<V>(
    val value: V,
    val createdAt: Instant = Instant.now(),
    var lastAccessed: Instant = Instant.now(),
    val size: Long = 1 // упрощённо, в реальности можно считать байты
)
```

### Вопрос 2: **"Как управлять TTL?"**

```kotlin
// Нужно периодически проверять и удалять просроченные записи
// → Отдельный механизм cleanup'а

// ✅ Решение: Scheduled executor для фоновой очистки
private val cleanupExecutor = Executors.newScheduledThreadPool(1)
```

### Вопрос 3: **"Как считать метрики thread-safe?"**

```kotlin
// Множество потоков будет обновлять счётчики
// → Атомарные счётчики

// ✅ Решение: AtomicLong для метрик
private val hitCount = AtomicLong(0)
private val missCount = AtomicLong(0)
```

## 🎯 **ШАГ 3: Выбор структур данных**

### Вопрос 4: **"Как хранить данные для быстрого доступа?"**

```kotlin
// Нужен быстрый поиск по ключу → HashMap
// Но обычный HashMap не thread-safe → ConcurrentHashMap

// ✅ Решение: 
private val storage = ConcurrentHashMap<K, CacheEntry<V>>()
```

### Вопрос 5: **"Как реализовать LRU (Least Recently Used)?"**

```kotlin
// LRU требует знать порядок доступа к элементам
// → LinkedHashMap или собственная реализация с doubly-linked list

// ✅ Решение: используем LinkedHashMap с accessOrder = true
private val accessOrderMap = Collections.synchronizedMap(
    LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true) // true = access ordering
)
```

## 🔨 **НАЧНЁМ РЕАЛИЗАЦИЮ!**

```kotlin
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

// 🎯 1. Сначала определим перечисления и конфигурацию

enum class EvictionPolicy {
    LRU,    // Least Recently Used - вытесняем давно неиспользуемые
    FIFO,   // First In First Out - вытесняем самые старые
    TTL     // Time To Live - вытесняем по истечении времени
}

data class CacheConfig(
    val maxSize: Int = 1000,                    // Максимальное количество записей
    val defaultTTL: java.time.Duration = java.time.Duration.ofMinutes(30), // TTL по умолчанию
    val evictionPolicy: EvictionPolicy = EvictionPolicy.LRU, // Стратегия вытеснения
    val cleanupInterval: java.time.Duration = java.time.Duration.ofSeconds(30) // Интервал очистки
)

// 📦 2. Запись в кеше
data class CacheEntry<V>(
    val value: V,
    val createdAt: Instant = Instant.now(),
    var lastAccessed: Instant = Instant.now(),
    val size: Long = 1, // В реальном кеше здесь был бы размер в байтах
    val ttl: java.time.Duration? = null // Индивидуальный TTL, если null - используется default
) {
    fun isExpired(defaultTTL: java.time.Duration): Boolean {
        val effectiveTTL = ttl ?: defaultTTL
        return Instant.now().isAfter(createdAt.plus(effectiveTTL))
    }
    
    fun markAccessed() {
        lastAccessed = Instant.now()
    }
}

// 📊 3. Метрики кеша
data class CacheMetrics(
    val hitCount: Long,
    val missCount: Long,
    val evictionCount: Long,
    val currentSize: Int,
    val maxSize: Int
) {
    val hitRate: Double get() = if (hitCount + missCount > 0) {
        hitCount.toDouble() / (hitCount + missCount)
    } else { 0.0 }
    
    val usagePercentage: Double get() = (currentSize.toDouble() / maxSize.toDouble()) * 100
}

// 🎯 4. ОСНОВНОЙ КЛАСС КЕША
class InMemoryCache<K, V> private constructor(
    private val config: CacheConfig
) {
    // 📦 Хранилище данных
    private val storage = ConcurrentHashMap<K, CacheEntry<V>>()
    
    // 📊 Метрики
    private val hitCount = AtomicLong(0)
    private val missCount = AtomicLong(0)
    private val evictionCount = AtomicLong(0)
    
    // ⏰ Механизм очистки
    private val cleanupExecutor = Executors.newScheduledThreadPool(1)
    
    // 🔄 Для LRU - отслеживаем порядок доступа
    private val accessOrderMap = Collections.synchronizedMap(
        object : LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, CacheEntry<V>>?): Boolean {
                // Автоматически удаляем самый старый элемент при превышении размера
                return size > config.maxSize
            }
        }
    )
    
    init {
        // Запускаем периодическую очистку просроченных записей
        cleanupExecutor.scheduleAtFixedRate(
            { cleanupExpiredEntries() },
            config.cleanupInterval.toMillis(),
            config.cleanupInterval.toMillis(),
            TimeUnit.MILLISECONDS
        )
    }
    
    // 🎯 ОСНОВНЫЕ ОПЕРАЦИИ
    
    fun put(key: K, value: V, ttl: java.time.Duration? = null): V? {
        val previousValue = storage[key]?.value
        
        val entry = CacheEntry(
            value = value,
            ttl = ttl,
            size = calculateSize(value) // В реальности здесь был бы расчёт размера
        )
        
        storage[key] = entry
        
        // Для LRU обновляем порядок доступа
        if (config.evictionPolicy == EvictionPolicy.LRU) {
            synchronized(accessOrderMap) {
                accessOrderMap[key] = entry
            }
        }
        
        // Проверяем не превысили ли лимит
        if (storage.size > config.maxSize) {
            evictOneEntry()
        }
        
        return previousValue
    }
    
    fun get(key: K): V? {
        val entry = storage[key]
        
        return if (entry != null && !entry.isExpired(config.defaultTTL)) {
            // Запись найдена и не просрочена
            entry.markAccessed()
            hitCount.incrementAndGet()
            
            // Обновляем порядок доступа для LRU
            if (config.evictionPolicy == EvictionPolicy.LRU) {
                synchronized(accessOrderMap) {
                    accessOrderMap[key] = entry
                }
            }
            
            entry.value
        } else {
            // Запись не найдена или просрочена
            if (entry != null) {
                // Удаляем просроченную запись
                storage.remove(key)
                synchronized(accessOrderMap) { accessOrderMap.remove(key) }
            }
            missCount.incrementAndGet()
            null
        }
    }
    
    fun remove(key: K): V? {
        val removed = storage.remove(key)
        synchronized(accessOrderMap) { accessOrderMap.remove(key) }
        return removed?.value
    }
    
    fun clear() {
        storage.clear()
        synchronized(accessOrderMap) { accessOrderMap.clear() }
        hitCount.set(0)
        missCount.set(0)
        evictionCount.set(0)
    }
    
    // 📊 МЕТРИКИ
    
    fun getMetrics(): CacheMetrics {
        return CacheMetrics(
            hitCount = hitCount.get(),
            missCount = missCount.get(),
            evictionCount = evictionCount.get(),
            currentSize = storage.size,
            maxSize = config.maxSize
        )
    }
    
    // 🗑️ ВЫТЕСНЕНИЕ
    
    private fun evictOneEntry() {
        when (config.evictionPolicy) {
            EvictionPolicy.LRU -> evictLRU()
            EvictionPolicy.FIFO -> evictFIFO()
            EvictionPolicy.TTL -> cleanupExpiredEntries() // TTL обрабатывается в cleanup
        }
        evictionCount.incrementAndGet()
    }
    
    private fun evictLRU() {
        synchronized(accessOrderMap) {
            val eldest = accessOrderMap.entries.firstOrNull()
            if (eldest != null) {
                storage.remove(eldest.key)
                accessOrderMap.remove(eldest.key)
            }
        }
    }
    
    private fun evictFIFO() {
        // Для FIFO удаляем первую добавленную запись
        val eldest = storage.entries.minByOrNull { it.value.createdAt }
        if (eldest != null) {
            storage.remove(eldest.key)
            synchronized(accessOrderMap) { accessOrderMap.remove(eldest.key) }
        }
    }
    
    // ⏰ ОЧИСТКА ПРОСРОЧЕННЫХ ЗАПИСЕЙ
    
    private fun cleanupExpiredEntries() {
        val iterator = storage.entries.iterator()
        var cleanedCount = 0
        
        while (iterator.hasNext()) {
            val (key, entry) = iterator.next()
            if (entry.isExpired(config.defaultTTL)) {
                iterator.remove()
                synchronized(accessOrderMap) { accessOrderMap.remove(key) }
                cleanedCount++
                evictionCount.incrementAndGet()
            }
        }
        
        if (cleanedCount > 0) {
            println("🔄 Очищено $cleanedCount просроченных записей")
        }
    }
    
    // 🔧 ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    
    private fun calculateSize(value: V): Long {
        // В реальной системе здесь был бы расчёт размера объекта в байтах
        // Для простоты возвращаем 1
        return 1
    }
    
    fun size(): Int = storage.size
    
    fun containsKey(key: K): Boolean {
        return get(key) != null // Используем get для проверки TTL
    }
    
    // 🛑 ОСТАНОВКА
    
    fun shutdown() {
        cleanupExecutor.shutdown()
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            cleanupExecutor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
    
    companion object {
        fun <K, V> create(block: CacheConfig.() -> Unit = {}): InMemoryCache<K, V> {
            val config = CacheConfig().apply(block)
            return InMemoryCache(config)
        }
    }
}
```

## 🧪 **ТЕСТИРУЕМ НАШ КЕШ**

```kotlin
fun main() {
    println("=== 🧪 ТЕСТИРОВАНИЕ СИСТЕМЫ КЕШИРОВАНИЯ ===")
    
    // Создаём кеш с настройками
    val cache = InMemoryCache.create<String, String> {
        maxSize = 5
        defaultTTL = java.time.Duration.ofSeconds(10)
        evictionPolicy = EvictionPolicy.LRU
        cleanupInterval = java.time.Duration.ofSeconds(5)
    }
    
    // Тест 1: Базовые операции
    println("🔹 ТЕСТ 1: Базовые операции put/get")
    cache.put("key1", "value1")
    cache.put("key2", "value2", java.time.Duration.ofSeconds(3)) // Короткий TTL
    
    println("key1: ${cache.get("key1")}") // ✅ value1
    println("key2: ${cache.get("key2")}") // ✅ value2
    
    // Тест 2: TTL
    println("\n🔹 ТЕСТ 2: TTL")
    println("Ждём 4 секунды...")
    Thread.sleep(4000)
    println("key1: ${cache.get("key1")}") // ✅ value1 (TTL 10 секунд)
    println("key2: ${cache.get("key2")}") // ❌ null (TTL 3 секунды истёк)
    
    // Тест 3: LRU вытеснение
    println("\n🔹 ТЕСТ 3: LRU вытеснение")
    cache.put("key3", "value3")
    cache.put("key4", "value4") 
    cache.put("key5", "value5")
    cache.put("key6", "value6") // Должен вытеснить самый старый
    
    // Доступ к key3 чтобы он не был LRU
    cache.get("key3")
    
    cache.put("key7", "value7") // Должен вытеснить key1 (самый старый неиспользуемый)
    
    println("key1: ${cache.get("key1")}") // ❌ null (вытеснен)
    println("key3: ${cache.get("key3")}") // ✅ value3 (использовался недавно)
    
    // Тест 4: Метрики
    println("\n🔹 ТЕСТ 4: Метрики")
    val metrics = cache.getMetrics()
    println("""
        📊 Метрики кеша:
        • Hit/Miss: ${metrics.hitCount}/${metrics.missCount}
        • Hit Rate: ${"%.1f".format(metrics.hitRate * 100)}%
        • Размер: ${metrics.currentSize}/${metrics.maxSize}
        • Вытеснено: ${metrics.evictionCount}
    """.trimIndent())
    
    // Останавливаем кеш
    cache.shutdown()
}
```

## 🎯 **ОБЪЯСНЕНИЕ КЛЮЧЕВЫХ РЕШЕНИЙ:**

### 1. **Почему ConcurrentHashMap?**
```kotlin
private val storage = ConcurrentHashMap<K, CacheEntry<V>>()
```
- **Thread-safe** из коробки
- **Высокая производительность** при чтении
- **Подходит для частых get/put операций**

### 2. **Почему AtomicLong для метрик?**
```kotlin
private val hitCount = AtomicLong(0)
```
- **Атомарные операции** без блокировок
- **Высокая производительность** при частых обновлениях
- **Проще чем synchronized**

### 3. **Почему отдельный ScheduledExecutor?**
```kotlin
private val cleanupExecutor = Executors.newScheduledThreadPool(1)
```
- **Не блокирует основные операции**
- **Гарантированная периодичность** очистки
- **Контроль над потоком** очистки

### 4. **Почему LinkedHashMap для LRU?**
```kotlin
private val accessOrderMap = LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true)
```
- **accessOrder = true** автоматически обновляет порядок при доступе
- **removeEldestEntry** автоматически удаляет старые элементы
- **Synchronized wrapper** для thread-safety

## 🚀 **ЧТО МОЖНО УЛУЧШИТЬ:**

1. **Размер в байтах** - реальный расчёт размера объектов
2. **Persistent storage** - сохранение на диск
3. **Distributed cache** - несколько инстансов
4. **Event listeners** - уведомления о событиях
5. **Statistics** - более детальная статистика

Здесь представлен весь процесс проектирования, от декомпозиции до выбора конкретных структур данных! 💪

---


---

Проблема в том, что в Kotlin параметры `data class` по умолчанию **immutable** (неизменяемые) через `val`, и мы не можем их переприсваивать в lambda. Давай исправим! 🔧

## 🎯 **ПРИЧИНА ПРОБЛЕМЫ**

В `CacheConfig` все поля объявлены как `val`:

```kotlin
data class CacheConfig(
    val maxSize: Int = 1000,                    // ❌ val - неизменяемое!
    val defaultTTL: java.time.Duration = java.time.Duration.ofMinutes(30),
    val evictionPolicy: EvictionPolicy = EvictionPolicy.LRU,
    val cleanupInterval: java.time.Duration = java.time.Duration.ofSeconds(30)
)
```

## 🔨 **СПОСОБ 1: Использование var (простой)**

```kotlin
// ✅ Изменяем val на var
data class CacheConfig(
    var maxSize: Int = 1000,                    // ✅ var - изменяемое!
    var defaultTTL: java.time.Duration = java.time.Duration.ofMinutes(30),
    var evictionPolicy: EvictionPolicy = EvictionPolicy.LRU,
    var cleanupInterval: java.time.Duration = java.time.Duration.ofSeconds(30)
)
```

## 🎯 **СПОСОБ 2: Builder pattern (более идиоматичный)**

```kotlin
// ✅ Более Kotlin-way: используем Builder pattern
class CacheConfig private constructor(
    val maxSize: Int,
    val defaultTTL: java.time.Duration,
    val evictionPolicy: EvictionPolicy,
    val cleanupInterval: java.time.Duration
) {
    // Builder класс
    class Builder {
        var maxSize: Int = 1000
        var defaultTTL: java.time.Duration = java.time.Duration.ofMinutes(30)
        var evictionPolicy: EvictionPolicy = EvictionPolicy.LRU
        var cleanupInterval: java.time.Duration = java.time.Duration.ofSeconds(30)
        
        fun build(): CacheConfig {
            return CacheConfig(maxSize, defaultTTL, evictionPolicy, cleanupInterval)
        }
    }
    
    companion object {
        fun build(block: Builder.() -> Unit = {}): CacheConfig {
            return Builder().apply(block).build()
        }
    }
}
```

## 🎯 **СПОСОБ 3: Copy pattern (функциональный подход)**

```kotlin
// ✅ Функциональный подход: создаём копию с изменёнными полями
data class CacheConfig(
    val maxSize: Int = 1000,
    val defaultTTL: java.time.Duration = java.time.Duration.ofMinutes(30),
    val evictionPolicy: EvictionPolicy = EvictionPolicy.LRU,
    val cleanupInterval: java.time.Duration = java.time.Duration.ofSeconds(30)
) {
    companion object {
        fun create(block: CacheConfig.() -> CacheConfig = { this }): CacheConfig {
            return CacheConfig().block()
        }
    }
    
    // Добавляем методы-помощники для удобного копирования
    fun withMaxSize(size: Int): CacheConfig = copy(maxSize = size)
    fun withDefaultTTL(ttl: java.time.Duration): CacheConfig = copy(defaultTTL = ttl)
    fun withEvictionPolicy(policy: EvictionPolicy): CacheConfig = copy(evictionPolicy = policy)
    fun withCleanupInterval(interval: java.time.Duration): CacheConfig = copy(cleanupInterval = interval)
}
```

## 🚀 **РЕКОМЕНДУЕМЫЙ СПОСОБ: Builder pattern**

Давай используем **Способ 2** (Builder pattern), так как он самый читаемый и идиоматичный для Kotlin:

### 🔨 **ИСПРАВЛЕННЫЙ КОД:**

```kotlin
// 1. ИСПРАВЛЯЕМ CacheConfig
class CacheConfig private constructor(
    val maxSize: Int,
    val defaultTTL: java.time.Duration,
    val evictionPolicy: EvictionPolicy,
    val cleanupInterval: java.time.Duration
) {
    // Builder класс
    class Builder {
        var maxSize: Int = 1000
        var defaultTTL: java.time.Duration = java.time.Duration.ofMinutes(30)
        var evictionPolicy: EvictionPolicy = EvictionPolicy.LRU
        var cleanupInterval: java.time.Duration = java.time.Duration.ofSeconds(30)
        
        fun build(): CacheConfig {
            return CacheConfig(maxSize, defaultTTL, evictionPolicy, cleanupInterval)
        }
    }
    
    companion object {
        fun build(block: Builder.() -> Unit = {}): CacheConfig {
            return Builder().apply(block).build()
        }
    }
}

// 2. ИСПРАВЛЯЕМ создание кеша в InMemoryCache
companion object {
    fun <K, V> create(block: CacheConfig.Builder.() -> Unit = {}): InMemoryCache<K, V> {
        val config = CacheConfig.build(block)
        return InMemoryCache(config)
    }
}

// 3. ТЕПЕРЬ ЭТО РАБОТАЕТ!
fun main() {
    val cache = InMemoryCache.create<String, String> {
        maxSize = 5
        defaultTTL = java.time.Duration.ofSeconds(10)
        evictionPolicy = EvictionPolicy.LRU
        cleanupInterval = java.time.Duration.ofSeconds(5)
    }
    
    // Остальной код без изменений...
    cache.put("key1", "value1")
    println("key1: ${cache.get("key1")}") // ✅ value1
}
```

## 🎯 **АЛЬТЕРНАТИВА: Простой способ с var**

Если хочешь более простое решение, можно использовать **Способ 1**:

```kotlin
// ✅ Простой способ: изменяемые поля
data class CacheConfig(
    var maxSize: Int = 1000,
    var defaultTTL: java.time.Duration = java.time.Duration.ofMinutes(30),
    var evictionPolicy: EvictionPolicy = EvictionPolicy.LRU,
    var cleanupInterval: java.time.Duration = java.time.Duration.ofSeconds(30)
)

// В InMemoryCache оставляем как было:
companion object {
    fun <K, V> create(block: CacheConfig.() -> Unit = {}): InMemoryCache<K, V> {
        val config = CacheConfig().apply(block)
        return InMemoryCache(config)
    }
}
```

## 🤔 **ПОЧЕМУ ТАК ПРОИСХОДИТ?**

### Kotlin философия:
- **`val`** - immutable (предпочтительно)
- **`var`** - mutable (когда действительно нужно изменять)

### В нашем случае:
```kotlin
// ❌ Не работает с val:
val config = CacheConfig().apply {
    maxSize = 5  // Ошибка: нельзя изменить val поле
}

// ✅ Работает с Builder:
val config = CacheConfig.Builder().apply {
    maxSize = 5  // ОК: поле Builder'а - var
}.build()
```

## 🎯 **ВЫВОД:**

**Используй Builder pattern** когда:
- Много параметров конфигурации
- Хочешь immutable объекты
- Нужна читаемость и безопасность

**Используй var** когда:
- Мало параметров
- Простота важнее иммутабельности
- Внутренний код (не публичное API)

Рекомендую **Builder pattern** для нашего кеша - это более профессионально! 🚀

 💪

---

Отличный вопрос! Ты правильно заметил важный нюанс. Давай разберём подробно. 🔍

## 🎯 **Что означает `maxSize = 5`?**

**`maxSize = 5` означает, что кеш может содержать максимум 5 записей ОДНОВРЕМЕННО.**

Но тут есть важные детали в зависимости от политики вытеснения:

## 🔄 **Как работает с разными политиками:**

### 1. **С политикой LRU (Least Recently Used)**
```kotlin
val cache = InMemoryCache.create<String, String> {
    maxSize = 3
    evictionPolicy = EvictionPolicy.LRU
}

cache.put("key1", "value1")
cache.put("key2", "value2") 
cache.put("key3", "value3")  // ✅ 3 записи - лимит достигнут

// Теперь при добавлении новой записи:
cache.put("key4", "value4")  // 🗑️ Вытеснит key1 (самую давно неиспользуемую)

println(cache.get("key1")) // ❌ null (вытеснена)
println(cache.get("key4")) // ✅ value4
```

### 2. **С политикой FIFO (First In First Out)**
```kotlin
val cache = InMemoryCache.create<String, String> {
    maxSize = 3
    evictionPolicy = EvictionPolicy.FIFO
}

cache.put("key1", "value1") // 🏷️ Самая старая
cache.put("key2", "value2")
cache.put("key3", "value3")

cache.put("key4", "value4") // 🗑️ Вытеснит key1 (самую первую добавленную)
```

### 3. **С политикой TTL (Time To Live)**
```kotlin
val cache = InMemoryCache.create<String, String> {
    maxSize = 3
    evictionPolicy = EvictionPolicy.TTL
    defaultTTL = java.time.Duration.ofSeconds(5)
}

cache.put("key1", "value1")
cache.put("key2", "value2") 
cache.put("key3", "value3")

// Ждём 6 секунд...
Thread.sleep(6000)

// Все записи просрочены, но всё ещё в кеше до следующего cleanup
println(cache.size()) // ⚠️ Может показать 3, но get вернёт null

// При добавлении новой записи:
cache.put("key4", "value4") // Сначала cleanup удалит просроченные, потом добавит новую
```

## 🚨 **ПРОБЛЕМА В НАШЕЙ РЕАЛИЗАЦИИ**

Ты абсолютно прав задать этот вопрос! В нашей текущей реализации есть **логическая ошибка**:

```kotlin
private fun evictOneEntry() {
    when (config.evictionPolicy) {
        EvictionPolicy.LRU -> evictLRU()
        EvictionPolicy.FIFO -> evictFIFO()
        EvictionPolicy.TTL -> cleanupExpiredEntries() // ❌ TTL НЕ должен вытеснять по размеру!
    }
    evictionCount.incrementAndGet()
}
```

**TTL политика не должна вытеснять записи по достижению maxSize!** TTL только удаляет просроченные записи.

## 🔧 **ИСПРАВЛЕННАЯ ЛОГИКА**

Давай исправим эту ошибку:

```kotlin
private fun evictOneEntry() {
    when (config.evictionPolicy) {
        EvictionPolicy.LRU -> evictLRU()
        EvictionPolicy.FIFO -> evictFIFO()
        EvictionPolicy.TTL -> {
            // ❌ TTL НЕ вытесняет по размеру!
            // Просто не делаем ничего, ждём cleanup для просроченных записей
            return
        }
    }
    evictionCount.incrementAndGet()
}

// И в методе put исправляем условие:
fun put(key: K, value: V, ttl: java.time.Duration? = null): V? {
    // ... существующий код ...
    
    // Проверяем не превысили ли лимит (только для LRU и FIFO)
    if (storage.size > config.maxSize && 
        config.evictionPolicy in setOf(EvictionPolicy.LRU, EvictionPolicy.FIFO)) {
        evictOneEntry()
    }
    
    return previousValue
}
```

## 🎯 **ПРАВИЛЬНОЕ ПОВЕДЕНИЕ:**

### **LRU/FIFO + maxSize = 3:**
```
До: [key1, key2, key3]
put(key4, value4)
После: [key2, key3, key4] // key1 удалён
```

### **TTL + maxSize = 3:**
```
До: [key1, key2, key3] (все просрочены)
put(key4, value4)
После: [key4] // просроченные удалены cleanup'ом, новая добавлена
Размер: 1 (не 4!)
```

## 🧪 **ТЕСТ ДЛЯ ПРОВЕРКИ:**

```kotlin
fun testMaxSizeBehavior() {
    println("=== 🧪 ТЕСТИРУЕМ ПОВЕДЕНИЕ maxSize ===")
    
    // Тест 1: LRU с maxSize
    println("🔹 ТЕСТ 1: LRU с maxSize = 3")
    val lruCache = InMemoryCache.create<String, String> {
        maxSize = 3
        evictionPolicy = EvictionPolicy.LRU
    }
    
    lruCache.put("1", "one")
    lruCache.put("2", "two")
    lruCache.put("3", "three")   // ✅ 3 записи
    lruCache.put("4", "four")    // 🗑️ Должна вытеснить "1"
    
    println("Размер: ${lruCache.size()}") // Должно быть 3
    println("key1: ${lruCache.get("1")}") // Должен быть null
    println("key4: ${lruCache.get("4")}") // Должен быть "four"
    
    // Тест 2: TTL с maxSize  
    println("\n🔹 ТЕСТ 2: TTL с maxSize = 3")
    val ttlCache = InMemoryCache.create<String, String> {
        maxSize = 3
        evictionPolicy = EvictionPolicy.TTL
        defaultTTL = java.time.Duration.ofSeconds(2)
    }
    
    ttlCache.put("1", "one")
    ttlCache.put("2", "two") 
    ttlCache.put("3", "three")   // ✅ 3 записи
    
    // Ждём пока записи просрочатся
    Thread.sleep(3000)
    
    // Добавляем новую запись
    ttlCache.put("4", "four")    // 🧹 Должна очистить просроченные, потом добавить
    
    println("Размер: ${ttlCache.size()}") // Должно быть 1 (только "4")
    println("key1: ${ttlCache.get("1")}") // Должен быть null (просрочен)
    println("key4: ${ttlCache.get("4")}") // Должен быть "four"
}
```

## 🎯 **ВЫВОД:**

**`maxSize` ограничивает количество ЖИВЫХ (непросроченных) записей в кеше:**

- ✅ **LRU/FIFO**: строгое ограничение - всегда максимум N записей
- ✅ **TTL**: "мягкое" ограничение - может быть меньше N из-за просроченных записей
- ❌ **TTL НЕ должен** вытеснять записи только потому что достигнут maxSize

Спасибо за этот важный вопрос! Ты нашёл реальную архитектурную ошибку в нашей системе! 🎯

Теперь понимаешь разницу в поведении? Это критически важно для правильной работы кеша! 💪

---


