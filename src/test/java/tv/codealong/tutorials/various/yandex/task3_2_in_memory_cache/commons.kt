package tv.codealong.tutorials.various.yandex.task3_2_in_memory_cache

import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * 🎯 **ЗАДАЧА: In-Memory Cache с TTL**
 *
 * 🔍 ШАГ 1: Декомпозиция задачи
 * Давай разобьём на подзадачи:
 *
 * kotlin
 * // 1. 📦 Хранение данных: как хранить пары ключ-значение + TTL?
 * // 2. ⏰ Управление временем жизни: как отслеживать и удалять просроченное?
 * // 3. 🔒 Безопасность: как сделать thread-safe?
 * // 4. 📊 Метрики: как считать hit/miss?
 * // 5. 🗑️ Вытеснение: что делать при переполнении?
 * // 6. ⚙️ Конфигурация: настройки TTL, размера, стратегий
 * 🏗️ ШАГ 2: Определение сущностей
 * Вопрос 1: "Что представляет собой запись в кеше?"
 * kotlin
 * // Запись должна хранить:
 * // - Значение (любого типа)
 * // - Время создания (для TTL)
 * // - Время последнего доступа (для LRU)
 * // - Размер данных (для ограничения памяти)
 *
 * // ✅ Решение: data class для записи
 * data class CacheEntry<V>(
 *     val value: V,
 *     val createdAt: Instant = Instant.now(),
 *     var lastAccessed: Instant = Instant.now(),
 *     val size: Long = 1 // упрощённо, в реальности можно считать байты
 * )
 * Вопрос 2: "Как управлять TTL?"
 * kotlin
 * // Нужно периодически проверять и удалять просроченные записи
 * // → Отдельный механизм cleanup'а
 *
 * // ✅ Решение: Scheduled executor для фоновой очистки
 * private val cleanupExecutor = Executors.newScheduledThreadPool(1)
 * Вопрос 3: "Как считать метрики thread-safe?"
 * kotlin
 * // Множество потоков будет обновлять счётчики
 * // → Атомарные счётчики
 *
 * // ✅ Решение: AtomicLong для метрик
 * private val hitCount = AtomicLong(0)
 * private val missCount = AtomicLong(0)
 * 🎯 ШАГ 3: Выбор структур данных
 * Вопрос 4: "Как хранить данные для быстрого доступа?"
 * kotlin
 * // Нужен быстрый поиск по ключу → HashMap
 * // Но обычный HashMap не thread-safe → ConcurrentHashMap
 *
 * // ✅ Решение:
 * private val storage = ConcurrentHashMap<K, CacheEntry<V>>()
 * Вопрос 5: "Как реализовать LRU (Least Recently Used)?"
 * kotlin
 * // LRU требует знать порядок доступа к элементам
 * // → LinkedHashMap или собственная реализация с doubly-linked list
 *
 * // ✅ Решение: используем LinkedHashMap с accessOrder = true
 * private val accessOrderMap = Collections.synchronizedMap(
 *     LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true) // true = access ordering
 * )
 */
// 🎯 1. Сначала определим перечисления для политик в отношении вытеснения и конфигурацию
enum class EvictionPolicy2 {
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
class CacheConfig2 private constructor(
    val maxSize: Int,
    val defaultTTL: java.time.Duration,
    val evictionPolicy: EvictionPolicy2,
    val cleanupInterval: java.time.Duration,
) {
    // Builder класс
    // Обязательно поставить дефолтные значения
    class Builder2 {
        var maxSize: Int = 1000
        var defaultTTL: java.time.Duration = java.time.Duration.ofMinutes(30)
        var evictionPolicy: EvictionPolicy2 = EvictionPolicy2.LRU
        var cleanupInterval: java.time.Duration = java.time.Duration.ofSeconds(30)

        fun build(): CacheConfig2 {
            return CacheConfig2(maxSize, defaultTTL, evictionPolicy, cleanupInterval)
        }
    }

    companion object {
        fun build(block: Builder2.() -> Unit = {}): CacheConfig2 {
            return Builder2().apply(block).build()
        }
    }
}

// 📦 2. Запись в кеше
data class CacheEntry2<V>(
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
data class CacheMetrics2(
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

// 🎯 4. ОСНОВНОЙ КЛАСС КЕША.
// Синхронизировать оба хранилища storage и accessOrderMap (сложнее).
// Лучше использовать пример: @see tv.codealong.tutorials.various.yandex.task3_1.InMemoryCache
class InMemoryCache2<K, V> private constructor(
    private val config: CacheConfig2,
) {
    // 📦 Основное Хранилище данных
    private val storage = ConcurrentHashMap<K, CacheEntry2<V>>()

    // ✅ ЕДИНСТВЕННОЕ хранилище - LinkedHashMap с LRU в пример: @see tv.codealong.tutorials.various.yandex.task3_1.InMemoryCache
    // это лучшая стратегия, т.к. с LinkedHashMap работает LRU из коробки.
//    private val storage = Collections.synchronizedMap(
//        object : LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true) {
//            override fun removeEldestEntry(eldest: Map.Entry<K, CacheEntry<V>>): Boolean {
//                val shouldRemove = size > config.maxSize
//                if (shouldRemove && eldest != null) {
//                    println("🗑️ LRU вытеснение: ${eldest.key}")
//                }
//                return shouldRemove
//            }
//        }
//    )

    // 📊 Метрики
    private val hitCount = AtomicLong(0)
    private val missCount = AtomicLong(0)
    private val evictionCount = AtomicLong(0)

    // ⏰ Механизм очистки
    private val cleanupExecutor = Executors.newScheduledThreadPool(1)


    /**
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
        object : LinkedHashMap<K, CacheEntry2<V>>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, CacheEntry2<V>>?): Boolean {
                // Автоматически удаляем самый старый элемент при превышении размера
                if (size > config.maxSize && eldest != null) {
                    // ✅ СИНХРОНИЗИРУЕМ: удаляем из ОБОИХ хранилищ
                    storage.remove(eldest.key)
                    println("🗑️ Вытеснено: ${eldest.key}")
                    return true
                }
                return false
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
     * @param key - ключ записи
     * @param ttl - индивидуальный TTL для записи, если null - используется default
     * @return предыдущее значение, если оно было
     */
    fun put(key: K, value: V, ttl: java.time.Duration? = null): V? {
        val previousValue = storage[key]?.value

        val entry = CacheEntry2(
            value = value,
            ttl = ttl,
            size = calculateSize(value) // В реальности здесь был бы расчёт размера
        )

        // ✅ СИНХРОНИЗИРУЕМ: добавляем в ОБА хранилища
        storage[key] = entry
        accessOrderMap[key] = entry // ⚠️ Может вызвать removeEldestEntry и удалить из storage!


//        // Для LRU обновляем порядок доступа
//        if (config.evictionPolicy == EvictionPolicy.LRU) {
//            synchronized(accessOrderMap) {
//                accessOrderMap[key] = entry
//                // ⚠️ removeEldestEntry может автоматически удалить самый старый элемент!
//            }
//        }
//
//        // Проверяем не превысили ли лимит (только для LRU и FIFO)
//        if (storage.size > config.maxSize &&
//            config.evictionPolicy in setOf(EvictionPolicy.LRU, EvictionPolicy.FIFO)
//        ) {
//            evictOneEntry()
//        }

        return previousValue
    }

    /**
     * ✅ GET - возвращает запись из кеша или null
     */
    fun get(key: K): V? {
        //Взяли из хранилища
        val entry = storage[key] ?: return null
        if (!entry.isExpired(config.defaultTTL)) {
            // ✅ СИНХРОНИЗИРУЕМ: обновляем порядок в ОБОИХ хранилищах
            entry.markAccessed()
            accessOrderMap[key] = entry // Обновляет порядок в LinkedHashMap
            hitCount.incrementAndGet()
            return entry.value
        } else {
            storage.remove(key)
            accessOrderMap.remove(key)
            missCount.incrementAndGet()
            return null
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

    fun getMetrics(): CacheMetrics2 {
        return CacheMetrics2(
            hitCount = hitCount.get(),
            missCount = missCount.get(),
            evictionCount = evictionCount.get(),
            currentSize = storage.size,
            maxSize = config.maxSize
        )
    }

    // 🗑️ ВЫТЕСНЕНИЕ
    //TTL политика не должна вытеснять записи по достижению maxSize! TTL только удаляет просроченные записи.
    private fun evictOneEntry() {
        when (config.evictionPolicy) {
            EvictionPolicy2.LRU -> evictLRU()
            EvictionPolicy2.FIFO -> evictFIFO()
            EvictionPolicy2.TTL -> {
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
        fun <K, V> create(block: CacheConfig2.Builder2.() -> Unit = {}): InMemoryCache2<K, V> {
            val config = CacheConfig2.build(block)
            return InMemoryCache2(config)
        }
    }
}