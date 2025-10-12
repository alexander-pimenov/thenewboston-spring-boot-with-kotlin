package tv.codealong.tutorials.various.yandex.task4_1

// 🚀 ЗАПУСКАЕМ ТЕСТЫ

fun main() {
    // Демонстрация использования в Web приложении
    demonstrateWebIntegration()
}

fun demonstrateWebIntegration() {
    println("\n=== 🌐 ИНТЕГРАЦИЯ С WEB API ===")

    val authService = AuthServiceFactory.createInMemoryService()
    val authController = AuthController(authService)

    // Симуляция HTTP запросов
    println("🔹 Симуляция HTTP запросов:")

    // 1. Регистрация
    println("1. POST /api/register")
    val registerResponse = authController.register(RegisterRequest("webuser@example.com", "WebPass123!"))
    println("   Ответ: ${if (registerResponse.success) "✅ Успех" else "❌ ${registerResponse.error}"}")

    // 2. Логин
    println("2. POST /api/login")
    val loginResponse = authController.login(LoginRequest("webuser@example.com", "WebPass123!", null))
    println("   Ответ: ${if (loginResponse.success) "✅ Успех" else "❌ ${loginResponse.error}"}")

    // 3. Получение профиля (с токеном)
    if (loginResponse.success && loginResponse.data != null) {
        println("3. GET /api/profile (с токеном)")
        val profileResponse = authController.getProfile("Bearer ${loginResponse.data.accessToken}")
        println("   Ответ: ${if (profileResponse.success) "✅ Профиль получен" else "❌ ${profileResponse.error}"}")
    }

    println("\n🎯 СИСТЕМА АУТЕНТИФИКАЦИИ ГОТОВА К ИСПОЛЬЗОВАНИЮ!")
}