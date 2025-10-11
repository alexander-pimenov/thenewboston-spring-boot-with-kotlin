package tv.codealong.tutorials.various.yandex.task2_1

fun main() {
    println("=== 🪣 ТЕСТИРОВАНИЕ TOKEN BUCKET ===")
    val rateLimiter = DistributedRateLimiter()

    // Настраиваем Token Bucket: 10 запросов в 5 секунд
    rateLimiter.configureEndpoint(
        "/api/search",
        RateLimitConfig(
            maxRequests = 10,
            windowSize = java.time.Duration.ofSeconds(5),
            algorithm = RateLimitAlgorithm.TOKEN_BUCKET
        )
    )

//    // Настраиваем лимиты для разных endpoint'ов (1)
//    rateLimiter.configureEndpoint(
//        "/api/users",
//        RateLimitConfig(
//            maxRequests = 100,
//            windowSize = java.time.Duration.ofMinutes(1),
//            algorithm = RateLimitAlgorithm.TOKEN_BUCKET
//        )
//    )

    // Настраиваем лимиты для разных endpoint'ов (2)
    rateLimiter.configureEndpoint(
        "/api/payments",
        RateLimitConfig(
            maxRequests = 10,
            windowSize = java.time.Duration.ofSeconds(30),
            algorithm = RateLimitAlgorithm.SLIDING_WINDOW
        )
    )

    val userId = "user123"
    val endpoint = "/api/search"

    // Тестируем
//    repeat(15) { i ->
//        val result = rateLimiter.tryAcquire("user123", "/api/payments")
//        println("Запрос $i: ${if (result.allowed) "✅" else "❌"} - осталось ${result.remaining}")
//    }

    // Тест 1: Быстрые запросы
    println("🔹 ТЕСТ 1: 15 быстрых запросов (лимит: 10/5сек)")
    repeat(15) { i ->
        val result = rateLimiter.tryAcquire(userId, endpoint)
        println("Запрос ${i + 1}: ${if (result.allowed) "✅" else "❌"} " +
                "- осталось ${result.remaining} токенов")
        Thread.sleep(100) // Небольшая задержка между запросами
    }

    // Тест 2: Ждём и пробуем снова
    println("\n⏳ Ждём 3 секунды...")
    Thread.sleep(3000)

    println("🔹 ТЕСТ 2: Ещё 5 запросов после ожидания")
    repeat(5) { i ->
        val result = rateLimiter.tryAcquire(userId, endpoint)
        println("Запрос ${i + 1}: ${if (result.allowed) "✅" else "❌"} " +
                "- осталось ${result.remaining} токенов")
    }

    // Тест 3: Метрики
    println("\n📊 МЕТРИКИ ПОЛЬЗОВАТЕЛЯ:")
    val metrics = rateLimiter.getMetrics(userId)
    println("""
        👤 User: ${metrics?.userId}
        📨 Всего запросов: ${metrics?.totalRequests}
        🚫 Заблокировано: ${metrics?.blockedRequests}  
        📈 Успешных: ${metrics?.successRate?.times(100)?.toInt()}%
    """.trimIndent())

    // Тест 4: Несколько пользователей
    println("\n🔹 ТЕСТ 4: Разные пользователи")
    val user1Result = rateLimiter.tryAcquire("user1", endpoint)
    val user2Result = rateLimiter.tryAcquire("user2", endpoint)

    println("User1: ${if (user1Result.allowed) "✅" else "❌"} (${user1Result.remaining} осталось)")
    println("User2: ${if (user2Result.allowed) "✅" else "❌"} (${user2Result.remaining} осталось)")
}

// 🎯 ВЫВОД (примерный):
// === 🪣 ТЕСТИРОВАНИЕ TOKEN BUCKET ===
// 🔹 ТЕСТ 1: 15 быстрых запросов (лимит: 10/5сек)
// Запрос 1: ✅ - осталось 9 токенов
// Запрос 2: ✅ - осталось 8 токенов
// ...
// Запрос 10: ✅ - осталось 0 токенов
// Запрос 11: ❌ - осталось 0 токенов
// ...
// 🔹 ТЕСТ 2: Ещё 5 запросов после ожидания
// Запрос 1: ✅ - осталось 5 токенов (частично пополнилось)
// ...

//Тестируем метрики
fun testMetrics() {
    val rateLimiter = DistributedRateLimiter()

    // Настраиваем endpoint
    rateLimiter.configureEndpoint(
        "/api/test",
        RateLimitConfig(5, java.time.Duration.ofSeconds(1), RateLimitAlgorithm.TOKEN_BUCKET)
    )

    // Делаем несколько запросов
    repeat(3) {
        rateLimiter.tryAcquire("user123", "/api/test")
    }

    // Теперь это должно работать!
    val metrics = rateLimiter.getMetrics("user123")
    println("Метрики: $metrics") // ✅ Выведет реальные метрики

    // Тестируем несуществующего пользователя
    val unknownMetrics = rateLimiter.getMetrics("unknown")
    println("Неизвестный пользователь: $unknownMetrics") // ✅ Выведет null
}

