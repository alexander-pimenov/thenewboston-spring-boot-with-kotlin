package tv.codealong.tutorials.various.yandex.task1_1

import java.math.BigDecimal

// 6. Пример использования
fun main() {
    println("=== СИСТЕМА ПРОВЕРКИ ЛИМИТОВ ===")

    // Создаём систему с теми же лимитами для всех пользователей
    val limitSystem = LimitCheckingSystem().apply {
        addLimit(DailyAmountLimit(BigDecimal("100000.00")))
        addLimit(SinglePaymentLimit(BigDecimal("50000.00")))
        addLimit(MonthlyCountLimit(5)) // Уменьшим для наглядности
    }

    // Создаём трёх пользователей
    val users = listOf("user_алиса", "user_боб", "user_карл")

    // Показываем лимиты для каждого пользователя
    println("\n📋 Установленные лимиты для всех пользователей:")
    limitSystem.getUserLimitsInfo(users.first()).forEach {
        println("   • $it")
    }

    // Симулируем параллельные платежи разных пользователей
    println("\n💰 СИМУЛЯЦИЯ ПАРАЛЛЕЛЬНЫХ ПЛАТЕЖЕЙ:")

    // Создаём платежи
    val payments = listOf(
        Payment(users[0], BigDecimal("30000.00"), "RUB"), // Алиса
        Payment(users[1], BigDecimal("15000.00"), "RUB"), // Боб
        Payment(users[2], BigDecimal("45000.00"), "RUB"), // Карл
        Payment(users[0], BigDecimal("25000.00"), "RUB"), // Алиса
        Payment(users[1], BigDecimal("35000.00"), "RUB"), // Боб
        Payment(users[0], BigDecimal("60000.00"), "RUB"), // Алиса - превысит лимит
        Payment(users[2], BigDecimal("10000.00"), "RUB"), // Карл
        Payment(users[1], BigDecimal("5000.00"), "RUB"),  // Боб
        Payment(users[0], BigDecimal("50000.00"), "RUB")  // Алиса - превысит лимит на один платёж
    )

    // Запускаем платежи в параллельных потоках
    val threads: List<Thread> = payments.mapIndexed { index, payment ->
        Thread {
            // Имитируем случайную задержку между платежами
            Thread.sleep((700..1000).random().toLong())

            val userColor = when (payment.userId) {
                users[0] -> "🟦" // Алиса - синий
                users[1] -> "🟩" // Боб - зелёный
                else -> "🟨"     // Карл - жёлтый
            }

            //добавим временную метку
            //logWithTime("${payment.userId} начинает платеж ${index + 1}.", userColor)
            // Обрабатываем платёж
            val result = limitSystem.processPayment(payment)
            //logWithTime("${payment.userId} закончил платеж ${index + 1}.", userColor)



            println("$userColor Платёж ${index + 1}: ${payment.userId} -> ${payment.amount} RUB")
            println("   Результат: $result")
            println("   ---")
        }
    }
    println("\n запланированы ${threads.size} платежей \n")

    // Запускаем все потоки
    threads.forEach { it.start() }

    // Ждём завершения всех потоков
    threads.forEach { it.join() }

    // Показываем итоговую статистику
    printFinalStatistics(limitSystem, users)
}

// Функция для вывода итоговой статистики
fun printFinalStatistics(system: LimitCheckingSystem, users: List<String>) {
    println("\n📊 ИТОГОВАЯ СТАТИСТИКА:")
    println("==================================================")

    users.forEach { userId ->
        println("\n👤 Пользователь: $userId")
        // В реальной системе здесь был бы метод для получения статистики
        // Для демонстрации просто покажем, что система помнит всех пользователей
        println("   ✅ Учетная запись активна в системе")
    }

    println("\n" + "==================================================")
    println("🎯 ВЫВОДЫ:")
    println("• Каждый пользователь имеет свою изолированную историю платежей")
    println("• Лимиты проверяются индивидуально для каждого пользователя")
    println("• Платежи разных пользователей обрабатываются параллельно")
    println("• Платежи одного пользователя обрабатываются последовательно")
}

// Функция для логирования с временными метками
fun logWithTime(message: String, userColor: String = "⚪") {
    val time = System.currentTimeMillis() % 10000
    println("[$time] $userColor $message")
}