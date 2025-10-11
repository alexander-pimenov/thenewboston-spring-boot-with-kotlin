package tv.codealong.tutorials.various.yandex.task2_1


fun main() {
    compareAllAlgorithms()
}


/**
 * 🔄 СРАВНЕНИЕ ВСЕХ ТРЁХ АЛГОРИТМОВ
 */
fun compareAllAlgorithms() {
    println("=== 🔄 СРАВНЕНИЕ ВСЕХ АЛГОРИТМОВ ===")

    val rateLimiter = DistributedRateLimiter()

    // Настраиваем одинаковые лимиты разными алгоритмами
    val configs = mapOf(
        "Token Bucket" to RateLimitConfig(5, java.time.Duration.ofSeconds(4), RateLimitAlgorithm.TOKEN_BUCKET),
        "Fixed Window" to RateLimitConfig(5, java.time.Duration.ofSeconds(4), RateLimitAlgorithm.FIXED_WINDOW),
        "Sliding Window" to RateLimitConfig(5, java.time.Duration.ofSeconds(4), RateLimitAlgorithm.SLIDING_WINDOW)
    )

    configs.forEach { (name, config) ->
        rateLimiter.configureEndpoint("/api/$name", config)
    }

    val userId = "testUser"

    println("🔹 Фаза 1: 6 быстрых запросов")
    repeat(6) { i ->
        println("Запрос ${i + 1}:")
        configs.keys.forEach { algorithmName ->
            val result = rateLimiter.tryAcquire(userId, "/api/$algorithmName")
            println("  $algorithmName: ${if (result.allowed) "✅" else "❌"} (${result.remaining} осталось)")
        }
        println()
        Thread.sleep(100)
    }

    println("⏳ Ждём 2 секунды (половина окна)...")
    Thread.sleep(2000)

    println("🔹 Фаза 2: Запросы после частичного ожидания")
    println("Запрос после ожидания:")
    configs.keys.forEach { algorithmName ->
        val result = rateLimiter.tryAcquire(userId, "/api/$algorithmName")
        println("  $algorithmName: ${if (result.allowed) "✅" else "❌"} (${result.remaining} осталось)")
    }

    println("\n🔹 Фаза 3: Ещё 3 запроса")
    repeat(3) { i ->
        println("Запрос ${i + 1}:")
        configs.keys.forEach { algorithmName ->
            val result = rateLimiter.tryAcquire(userId, "/api/$algorithmName")
            println("  $algorithmName: ${if (result.allowed) "✅" else "❌"} (${result.remaining} осталось)")
        }
        println()
        Thread.sleep(300)
    }
}