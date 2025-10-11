package tv.codealong.tutorials.various.yandex.task3_1

fun main() {
    println("=== 🧪 ТЕСТИРОВАНИЕ СИСТЕМЫ КЕШИРОВАНИЯ ===")

    // Создаём кеш с настройками
    val cache = InMemoryCache.create<String, String> {
        maxSize = 5  // Максимальный размер кеша, если будет больше этого числа, то будет удален тот что был добавлен первым
        defaultTTL = java.time.Duration.ofSeconds(10)
        evictionPolicy = EvictionPolicy.LRU
        cleanupInterval = java.time.Duration.ofSeconds(5)
    }

    // Тест 1: Базовые операции
    println("🔹 ТЕСТ 1: Базовые операции put/get")
    cache.put("key1", "value1")
    cache.put("key2", "value2", java.time.Duration.ofSeconds(3)) // Короткий TTL, чтобы не ждать 10 секунд, задаём своё.

    println("key1: ${cache.get("key1")}") // ✅ value1
    println("key2: ${cache.get("key2")}") // ✅ value2

    // Тест 2: TTL
    println("\n🔹 ТЕСТ 2: TTL")
    println("Ждём 4 секунды...")
    Thread.sleep(4000)
    println("key1: ${cache.get("key1")}") // ✅ value1 (TTL 10 секунд)
    println("key2: ${cache.get("key2")}") // ❌ null (TTL 3 секунды истёк)
    println("cache size: ${cache.size()}")
    cache.printCache()

    // Тест 3: LRU вытеснение
    println("\n🔹 ТЕСТ 3: LRU вытеснение")
    cache.put("key3", "value3")
    cache.put("key4", "value4")
    cache.put("key5", "value5")
    cache.put("key6", "value6") // Должен вытеснить самый старый, т.к. maxSize=5

    // Доступ к key3 (показываем, что мы с ним работаем), чтобы он не был LRU, после этого он автоматом переместиться в конец
    cache.get("key3")

    cache.put("key7", "value7") // Должен вытеснить key1 (самый старый неиспользуемый)
    println("cache size: ${cache.size()}")
    cache.printCache()

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