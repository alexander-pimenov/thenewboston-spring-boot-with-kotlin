package tv.codealong.tutorials.various.yandex.task3_1

fun main() {
    testConcurrentAccess()
}
//🧪 ТЕСТ НА МНОГОПОТОЧНОСТЬ:
fun testConcurrentAccess() {
    println("=== 🧪 ТЕСТ МНОГОПОТОЧНОГО ДОСТУПА ===")

    val cache = InMemoryCache.create<String, Int> {
        maxSize = 100
        defaultTTL = java.time.Duration.ofSeconds(10)
    }

    val threads = List(10) { threadId ->
        Thread {
            repeat(100) { i ->
                val key = "key-${threadId}-$i"
                println("Положим в storage данные под ключ: $key")
                cache.put(key, threadId * 100 + i)

                // Параллельные чтения и записи
                if (i % 3 == 0) {
                    cache.get(key)
                }

                if (i % 5 == 0) {
                    cache.containsKey(key)
                }

                if (i % 7 == 0) {
                    cache.remove(key)
                }
            }
        }
    }

    threads.forEach { it.start() }
    threads.forEach { it.join() }

    println("✅ Все потоки завершились без ошибок")
    println("Финальный размер кеша: ${cache.size()}")

    cache.shutdown()
}