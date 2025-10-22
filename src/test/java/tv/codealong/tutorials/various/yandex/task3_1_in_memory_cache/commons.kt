package tv.codealong.tutorials.various.yandex.task3_1_in_memory_cache

import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * 🎯 **ЗАДАЧА: In-Memory Cache с TTL**
 *
 * 🔍 ШАГ 1: Декомпозиция задачи
 * Давай разобьём на подзадачи:
 *
 *  1. 📦 Хранение данных: как хранить пары ключ-значение + TTL?
 *  2. ⏰ Управление временем жизни: как отслеживать и удалять просроченное?
 *  3. 🔒 Безопасность: как сделать thread-safe?
 *  4. 📊 Метрики: как считать hit/miss?
 *  5. 🗑️ Вытеснение: что делать при переполнении?
 *  6. ⚙️ Конфигурация: настройки TTL, размера, стратегий
 *
 * 🏗️ ШАГ 2: Определение сущностей
 * Вопрос 1: "Что представляет собой запись в кеше?"
 *
 *  Запись должна хранить:
 *  - Значение (любого типа)
 *  - Время создания (для TTL)
 *  - Время последнего доступа (для LRU)
 *  - Размер данных (для ограничения памяти)
 *
 *  ✅ Решение: data class для записи
 * ```kotlin
 * data class CacheEntry<V>(
 *     val value: V,
 *     val createdAt: Instant = Instant.now(),
 *     var lastAccessed: Instant = Instant.now(),
 *     val size: Long = 1 // упрощённо, в реальности можно считать байты
 * )
 * ```
 *
 * Вопрос 2: "Как управлять TTL?"
 *
 * // Нужно периодически проверять и удалять просроченные записи → Отдельный механизм cleanup'а
 *
 *  ✅ Решение: Scheduled executor для фоновой очистки
 *  ```kotlin
 * private val cleanupExecutor = Executors.newScheduledThreadPool(1)
 * ```
 * Вопрос 3: "Как считать метрики thread-safe?"
 *
 * // Множество потоков будет обновлять счётчики → Атомарные счётчики
 *
 *  ✅ Решение: AtomicLong для метрик
 * ```kotlin
 * private val hitCount = AtomicLong(0)
 * private val missCount = AtomicLong(0)
 * ```
 * 🎯 ШАГ 3: Выбор структур данных
 *
 * Вопрос 4: "Как хранить данные для быстрого доступа?"
 *
 * // Нужен быстрый поиск по ключу → HashMap. Но обычный HashMap не thread-safe → ConcurrentHashMap
 *
 *  ✅ Решение:
 * ```kotlin
 * private val storage = ConcurrentHashMap<K, CacheEntry<V>>()
 * ```
 * Вопрос 5: "Как реализовать LRU (Least Recently Used)?"
 *
 * // LRU требует знать порядок доступа к элементам → LinkedHashMap с accessOrder=true или собственная реализация с doubly-linked list
 *
 *  ✅ Решение: используем LinkedHashMap с accessOrder = true
 *  ```kotlin
 * private val accessOrderMap = Collections.synchronizedMap(
 *     LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true) // true = access ordering
 * )
 * ```
 */
// 🎯 1. Сначала определим перечисления для политик в отношении вытеснения и конфигурацию
enum class EvictionPolicy {
    LRU,    // Least Recently Used - вытесняем давно неиспользуемые
    FIFO,   // First In First Out - вытесняем самые старые
    TTL     // Time To Live - вытесняем по истечении времени
}

// 📦 1.1. Конфигурация кеша
//data class CacheConfig(
//    val maxSize: Int = 1000,                    // Максимальное количество записей
//    val defaultTTL: java.time.Duration = java.time.Duration.ofMinutes(30), // TTL по умолчанию
//    val evictionPolicy: EvictionPolicy = EvictionPolicy.LRU, // Стратегия вытеснения
//    val cleanupInterval: java.time.Duration = java.time.Duration.ofSeconds(30), // Интервал очистки
//)

/**
 * Kotlin философия:
 * - val - immutable (предпочтительно)
 * - var - mutable (когда действительно нужно изменять)
 *
 * 📦 1.1. Конфигурация кеша - используем Билдер
 *
 * Используй Builder pattern когда:
 * - Много параметров конфигурации
 * - Хочешь immutable объекты
 * - Нужна читаемость и безопасность
 *
 * maxSize ограничивает количество ЖИВЫХ (непросроченных) записей в кеше:
 * - ✅ LRU/FIFO: строгое ограничение - всегда максимум N записей
 * - ✅ TTL: "мягкое" ограничение - может быть меньше N из-за просроченных записей
 * - ❌ TTL НЕ должен вытеснять записи только потому что достигнут maxSize
 *
 */
class CacheConfig private constructor(
    val maxSize: Int,
    val defaultTTL: java.time.Duration,
    val evictionPolicy: EvictionPolicy,
    val cleanupInterval: java.time.Duration,
) {
    // Builder класс
    // Обязательно поставить дефолтные значения
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

// 📦 2. Запись в кеше
data class CacheEntry<V>(
    val value: V,
    val createdAt: Instant = Instant.now(),
    var lastAccessed: Instant = Instant.now(),
    val size: Long = 1, // В реальном кеше здесь был бы размер в байтах
    val ttl: java.time.Duration? = null, // Индивидуальный TTL, если null - используется default
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
    val maxSize: Int,
) {
    val hitRate: Double
        get() = if (hitCount + missCount > 0) {
            hitCount.toDouble() / (hitCount + missCount)
        } else {
            0.0
        }

    val usagePercentage: Double get() = (currentSize.toDouble() / maxSize.toDouble()) * 100
}

// 🎯 4. ОСНОВНОЙ КЛАСС КЕША
/**
 * 🎯 ВАЖНЫЕ МОМЕНТЫ:
 * 1. Collections.synchronizedMap ≠ полная thread-safe
 * kotlin
 * // synchronizedMap защищает только отдельные операции:
 * val map = Collections.synchronizedMap(LinkedHashMap<String, String>())
 *
 * map["key"] = "value" // ✅ Атомарно
 * map.get("key")       // ✅ Атомарно
 *
 * // НО составные операции не атомарны:
 * if (map.containsKey("key")) {  // ❌ Не атомарно!
 *     map.get("key")             // Между contains и get map может измениться
 * }
 * 2. LinkedHashMap с accessOrder=true особенно уязвим
 * При каждом get() меняется внутренняя структура
 *
 * Без полной синхронизации - гарантированные ConcurrentModificationException
 *
 * 3. Atomic переменные для счётчиков
 * kotlin
 * private val hitCount = AtomicLong(0) // ✅ Не нуждается в synchronized
 * // AtomicLong сам по себе thread-safe
 */
class InMemoryCache<K, V> private constructor(
    private val config: CacheConfig,
) {
    // 📦 Основное Хранилище данных
    // Убираем ConcurrentHashMap - всё в одном месте!
    //private val storage = ConcurrentHashMap<K, CacheEntry<V>>()

    // ✅ ЕДИНСТВЕННОЕ 📦 Основное Хранилище данных - LinkedHashMap с LRU
    //LinkedHashMap с accessOrder = true + removeEldestEntry даёт нам готовую LRU реализацию "из коробки"!
    private val storage = Collections.synchronizedMap(
        object : LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<K, CacheEntry<V>>): Boolean {
                return when (config.evictionPolicy) {
                    EvictionPolicy.LRU -> {
                        val shouldRemove = size > config.maxSize
                        if (shouldRemove && eldest != null) {
                            println("🗑️ LRU вытеснение: ${eldest.key}")
                        }
                        shouldRemove
                    }

                    EvictionPolicy.FIFO -> {
                        val shouldRemove = size > config.maxSize
                        if (shouldRemove && eldest != null) {
                            println("🗑️ LRU вытеснение: ${eldest.key}")
                        }
                        shouldRemove
                    }

                    EvictionPolicy.TTL -> false // TTL не вытесняет по размеру
                }
            }
        }
    )

    // 📊 Метрики
    private val hitCount = AtomicLong(0)
    private val missCount = AtomicLong(0)
    private val evictionCount = AtomicLong(0)

    // ⏰ Механизм очистки
    private val cleanupExecutor = Executors.newScheduledThreadPool(1)


    /**
     * Это описание оставляю для примера, в этой реализации используется одно хранилище и
     * свойства LinkedHashMap с accessOrder в storage.
     *
     * 🔄 Для LRU - отслеживаем порядок доступа (LRU tracking)
     * Цель: Отслеживать порядок доступа к элементам, чтобы знать какой элемент давно не использовался (Least Recently Used)
     * LinkedHashMap с accessOrder = true + removeEldestEntry даёт нам готовую LRU реализацию "из коробки"!
     *
     * * Зачем Collections.synchronizedMap:
     *   Делает LinkedHashMap thread-safe. Без этого в многопоточной среде может произойти:
     * - ConcurrentModificationException
     * - Повреждение внутренней структуры данных
     * - Непредсказуемое поведение
     *
     *  * LinkedHashMap с accessOrder = true
     *  LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true)
     *  //                              ↑      ↑     ↑
     *  //                          размер loadFactor accessOrder
     *  Что это значит:
     *  - 16 - начальная вместимость (как ArrayList capacity). В нашей реализации не используется. Просто нужен для конструктора.
     *  - 0.75f - load factor (когда HashMap увеличивается в размере)
     *  - true - ключевой параметр! Включает ordering по доступу.
     *
     *  * removeEldestEntry - "Волшебный метод"
     *
     *   override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, CacheEntry<V>>?): Boolean {
     *       return size > config.maxSize
     *   }
     *  Как работает:
     *  - Вызывается автоматически при КАЖДОМ добавлении нового элемента
     *  - eldest - самый старый элемент (первый в LinkedHashMap)
     *  - Если возвращаем true - eldest удаляется автоматически
     *
     * Пример использования можно посмотреть тут @see tv/codealong/tutorials/various/accessOrder/LinkedHashMapAccessOrder.kt
     *
     * 🎯 ПРЕИМУЩЕСТВА ЭТОГО ПОДХОДА:
     * ✅ Автоматическое управление порядком
     * - LinkedHashMap сам заботится о порядке
     * - Не нужно вручную перемещать элементы
     *
     * ✅ Эффективность
     * - O(1) для операций get/put
     * - Минимальные накладные расходы
     *
     * ✅ Простота
     * - Всего несколько строк кода
     * - Встроенная в Java/Kotlin функциональность
     *
     * ❌ НЕДОСТАТКИ:
     * - Двойное хранение - одни и те же данные в storage и accessOrderMap
     * - Синхронизация - нужно следить за согласованностью
     * - Память - дополнительная LinkedHashMap
     *
     */
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

    // 🎯 ОСНОВНЫЕ ОПЕРАЦИИ: PUT и GET
    /**
     * ✅ PUT - добавляет запись в кеш или обновляет существующую
     * МОДИФИЦИРУЮТ состояние - нужен synchronized
     *
     * @param key - ключ записи
     * @param ttl - индивидуальный TTL для записи, если null - используется default
     * @return предыдущее значение, если оно было
     */
    fun put(key: K, value: V, ttl: java.time.Duration? = null): V? {
        synchronized(storage) {
            val previousEntry = storage[key]

            val entry = CacheEntry(
                value = value,
                ttl = ttl,
                size = calculateSize(value) // В реальности здесь был бы расчёт размера
            )

            storage[key] = entry // ✅ LinkedHashMap сам вытеснит старые элементы если нужно

            return previousEntry?.value
        }
    }

    /**
     * ✅ GET - возвращает запись из кеша или null
     * МОДИФИЦИРУЮТ состояние в LinkedHashMap с accessOrder = true - нужен synchronized
     */
    fun get(key: K): V? {
        synchronized(storage) {  // ✅ НУЖЕН - может изменить порядок в LinkedHashMap
            //Взяли из хранилища
            val entry = storage[key]
            if (entry != null && !entry.isExpired(config.defaultTTL)) {
                // ✅ LinkedHashMap САМ обновит порядок благодаря accessOrder=true
                // Порядок автоматически обновляется благодаря accessOrder=true
                // Даже get может изменить внутреннее состояние LinkedHashMap!
                // (благодаря accessOrder=true)
                entry.markAccessed()
                hitCount.incrementAndGet()
                return entry.value
            } else {
                if (entry != null) {
                    storage.remove(key) // Удаляем просроченное
                }
                missCount.incrementAndGet()
                return null
            }
        }
    }

    // МОДИФИЦИРУЮТ состояние - нужен synchronized
    fun remove(key: K): V? {
        synchronized(storage) {
            val removed = storage.remove(key)
            return removed?.value
        }
    }

    // МОДИФИЦИРУЮТ состояние - нужен synchronized
    fun clear() {
        synchronized(storage) {
            storage.clear()
            hitCount.set(0)
            missCount.set(0)
            evictionCount.set(0)
        }
    }

    // 📊 МЕТРИКИ

    fun getMetrics(): CacheMetrics {
        synchronized(storage) {
            return CacheMetrics(
                hitCount = hitCount.get(),
                missCount = missCount.get(),
                evictionCount = evictionCount.get(),
                currentSize = storage.size,
                maxSize = config.maxSize
            )
        }
    }

    // 🗑️ ВЫТЕСНЕНИЕ
    //TTL политика не должна вытеснять записи по достижению maxSize! TTL только удаляет просроченные записи.
    private fun evictOneEntry() {
        when (config.evictionPolicy) {
            EvictionPolicy.LRU -> evictLRU()
            EvictionPolicy.FIFO -> evictFIFO()
            EvictionPolicy.TTL -> {
                // ❌ TTL НЕ вытесняет по размеру!
                // Просто не делаем ничего, ждём cleanup для просроченных записей
                return
            }
//            EvictionPolicy.TTL -> cleanupExpiredEntries() // TTL обрабатывается в cleanup
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
    // МОДИФИЦИРУЮТ состояние - нужен synchronized
    private fun cleanupExpiredEntries() {
        synchronized(storage) {
            val iterator = storage.entries.iterator()
            var cleanedCount = 0

            while (iterator.hasNext()) {
                val (key, entry) = iterator.next()
                if (entry.isExpired(config.defaultTTL)) {
                    iterator.remove()
                    cleanedCount++
                    evictionCount.incrementAndGet()
                }
            }

            if (cleanedCount > 0) {
                println("🔄 Очищено $cleanedCount просроченных записей")
            }
        }
    }

    // 🔧 ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ

    private fun calculateSize(value: V): Long {
        // В реальной системе здесь был бы расчёт размера объекта в байтах
        // Для простоты возвращаем 1
        return 1
    }

    fun size(): Int {
        synchronized(storage) {  // ✅ НУЖЕН - читает размер во время модификации
            return storage.size
        }
    }

    // МОДИФИЦИРУЮТ состояние - нужен synchronized, т.к. вызывает get внутри
    fun containsKey(key: K): Boolean {
        synchronized(storage) {  // ✅ НУЖЕН - вызывает get внутри
            return get(key) != null // Используем get для проверки TTL
        }
    }

    fun isEmpty(): Boolean {
        synchronized(storage) {
            return storage.isEmpty()
        }
    }

    //напечатать состав кеша
    fun printCache() {
        println("📦 Состав кеша:")
        storage.forEach { (key, entry) ->
            println("Ключ: $key, Значение: ${entry.value}, TTL: ${entry.ttl}")
        }
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
        // 🚀 СТАТИЧЕСКИЙ ФАБРИЧНЫЙ МЕТОД - если поля var то можно так:
//        fun <K, V> create(block: CacheConfig.() -> Unit = {}): InMemoryCache<K, V> {
//            val config = CacheConfig().apply(block)
//            return InMemoryCache(config)
//        }

        // 🚀 СТАТИЧЕСКИЙ ФАБРИЧНЫЙ МЕТОД - это при использовании Билдера:
        fun <K, V> create(block: CacheConfig.Builder.() -> Unit = {}): InMemoryCache<K, V> {
            val config = CacheConfig.build(block)
            return InMemoryCache(config)
        }
    }
}