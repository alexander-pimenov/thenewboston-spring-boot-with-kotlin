package tv.codealong.tutorials.various.yandex.task4_1_AuthenticationService

// 🚀 ЗАПУСКАЕМ ТЕСТЫ

fun main() {
    testAuthenticationSystem()
    testAdvancedScenarios()
}

// 🎯 10. ТЕСТИРОВАНИЕ ВСЕЙ СИСТЕМЫ

fun testAuthenticationSystem() {
    println("=== 🔐 ТЕСТИРОВАНИЕ СИСТЕМЫ АУТЕНТИФИКАЦИИ ===")

    // Создаём сервис аутентификации
    val authService = AuthServiceFactory.createInMemoryService {
        jwtSecret = "very-secure-secret-key-for-testing-purposes-only"
        accessTokenExpiration = java.time.Duration.ofMinutes(5) // Короткое для теста
        maxLoginAttempts = 3 // Мало попыток для демонстрации rate limiting
    }

    // 🔹 ТЕСТ 1: Регистрация пользователя
    println("\n🔹 ТЕСТ 1: РЕГИСТРАЦИЯ")
    val registerResult = authService.register(
        email = "alice@example.com",
        password = "SecurePass123!"
    )

    when (registerResult) {
        is AuthResult.Success -> {
            println("✅ Регистрация успешна!")
            println("   Пользователь: ${registerResult.data.email}")
            println("   ID: ${registerResult.data.id}")
        }
        is AuthResult.Error -> {
            println("❌ Ошибка регистрации: ${registerResult.message}")
        }
    }

    // 🔹 ТЕСТ 2: Успешный логин
    println("\n🔹 ТЕСТ 2: УСПЕШНЫЙ ЛОГИН")
    val loginResult = authService.login(
        email = "alice@example.com",
        password = "SecurePass123!",
        deviceInfo = DeviceInfo(
            userAgent = "Mozilla/5.0...",
            ipAddress = "192.168.1.100",
            deviceType = "Desktop"
        )
    )

    when (loginResult) {
        is AuthResult.Success -> {
            println("✅ Логин успешен!")
            println("   Access Token: ${loginResult.data.accessToken.take(20)}...")
            println("   User: ${loginResult.data.user.email}")
            println("   Expires: ${loginResult.data.expiresAt}")
        }
        is AuthResult.Error -> {
            println("❌ Ошибка логина: ${loginResult.message}")
        }
    }

    // 🔹 ТЕСТ 3: Валидация токена
    println("\n🔹 ТЕСТ 3: ВАЛИДАЦИЯ ТОКЕНА")
    if (loginResult is AuthResult.Success) {
        val validateResult = authService.validateToken(loginResult.data.accessToken)

        when (validateResult) {
            is AuthResult.Success -> {
                println("✅ Токен валиден!")
                println("   Пользователь: ${validateResult.data.email}")
            }
            is AuthResult.Error -> {
                println("❌ Токен невалиден: ${validateResult.message}")
            }
        }
    }

    // 🔹 ТЕСТ 4: Неправильный пароль
    println("\n🔹 ТЕСТ 4: НЕПРАВИЛЬНЫЙ ПАРОЛЬ")
    val wrongPasswordResult = authService.login(
        email = "alice@example.com",
        password = "WrongPassword123!"
    )

    when (wrongPasswordResult) {
        is AuthResult.Success -> println("✅ Логин успешен (не должно быть!)")
        is AuthResult.Error -> println("❌ Ожидаемая ошибка: ${wrongPasswordResult.message}")
    }

    // 🔹 ТЕСТ 5: Rate Limiting
    println("\n🔹 ТЕСТ 5: RATE LIMITING")
    repeat(10) { attempt ->
        val rateLimitResult = authService.login(
            email = "test@example.com", // Несуществующий пользователь
            password = "anypassword"
        )

        when (rateLimitResult) {
            is AuthResult.Success -> println("  Попытка ${attempt + 1}: ✅ Успех")
            is AuthResult.Error -> println("  Попытка ${attempt + 1}: ❌ ${rateLimitResult.message}")
        }
    }

    // 🔹 ТЕСТ 6: Logout
    println("\n🔹 ТЕСТ 6: LOGOUT")
    if (loginResult is AuthResult.Success) {
        val logoutResult = authService.logout(loginResult.data.accessToken)

        when (logoutResult) {
            is AuthResult.Success -> println("✅ Logout успешен!")
            is AuthResult.Error -> println("❌ Ошибка logout: ${logoutResult.message}")
        }

        // Пробуем использовать токен после logout
        println("🔹 Проверка токена после logout:")
        val validateAfterLogout = authService.validateToken(loginResult.data.accessToken)
        when (validateAfterLogout) {
            is AuthResult.Success -> println("❌ Токен всё ещё работает (не должно быть!)")
            is AuthResult.Error -> println("✅ Токен правильно отозван: ${validateAfterLogout.message}")
        }
    }

    // 🔹 ТЕСТ 7: Метрики
    println("\n🔹 ТЕСТ 7: МЕТРИКИ")
    val metrics = authService.getMetrics()
    println("""
        📊 Метрики аутентификации:
        • Всего попыток входа: ${metrics.loginAttempts}
        • Успешных входов: ${metrics.successfulLogins} 
        • Неудачных входов: ${metrics.failedLogins}
        • Успешность: ${"%.1f".format(metrics.successRate * 100)}%
    """.trimIndent())

    // 🔹 ТЕСТ 8: Попытка регистрации с существующим email
    println("\n🔹 ТЕСТ 8: ДУБЛИРОВАНИЕ EMAIL")
    val duplicateResult = authService.register(
        email = "alice@example.com", // Тот же email
        password = "AnotherPass123!"
    )

    when (duplicateResult) {
        is AuthResult.Success -> println("❌ Регистрация прошла (не должно быть!)")
        is AuthResult.Error -> println("✅ Ожидаемая ошибка: ${duplicateResult.message}")
    }

    println("\n=== 🎯 ТЕСТИРОВАНИЕ ЗАВЕРШЕНО ===")
}



// 🎯 11. ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ ДЛЯ РАЗНЫХ СЦЕНАРИЕВ

fun testAdvancedScenarios() {
    println("\n=== 🧪 ПРОДВИНУТЫЕ СЦЕНАРИИ ===")

    val authService = AuthServiceFactory.createInMemoryService()

    // 🔹 Сценарий 1: Несколько пользователей
    println("🔹 СЦЕНАРИЙ 1: НЕСКОЛЬКО ПОЛЬЗОВАТЕЛЕЙ")

    val users = listOf(
        "bob@example.com" to "BobPassword123!",
        "carol@example.com" to "CarolSecure456!",
        "dave@example.com" to "DavePass789!"
    )

    users.forEach { (email, password) ->
        val result = authService.register(email, password)
        when (result) {
            is AuthResult.Success -> println("   ✅ $email зарегистрирован")
            is AuthResult.Error -> println("   ❌ $email: ${result.message}")
        }
    }

    // 🔹 Сценарий 2: Разные роли пользователей
    println("\n🔹 СЦЕНАРИЙ 2: АДМИНИСТРАТОР")

    // В реальной системе добавили бы метод для создания пользователя с ролями
    println("   ⚠️ Регистрация администратора (требует отдельного метода)")

    // 🔹 Сценарий 3: Валидация email и пароля
    println("\n🔹 СЦЕНАРИЙ 3: ВАЛИДАЦИЯ ДАННЫХ")

    val invalidCases = listOf(
        "invalid-email" to "Pass123!", // Неправильный email
        "test@example.com" to "123",   // Слишком короткий пароль
        "test@example.com" to "password", // Пароль без цифр
        "test@example.com" to "12345678"  // Пароль без букв
    )

    invalidCases.forEach { (email, password) ->
        val result = authService.register(email, password)
        when (result) {
            is AuthResult.Success -> println("   ❌ $email принят (не должно быть!)")
            is AuthResult.Error -> println("   ✅ $email отклонён: ${result.message}")
        }
    }
}




