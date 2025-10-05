package tv.codealong.tutorials.various.yandex.task1_1

import java.math.BigDecimal

// 6. Пример использования
fun main() {
    println("=== ПАРАЛЛЕЛЬНАЯ ОБРАБОТКА 3-Х ПОЛЬЗОВАТЕЛЕЙ ===")

    val limitSystem = LimitCheckingSystem().apply {
        addLimit(DailyAmountLimit(BigDecimal("50000.00"))) // Уменьшим для наглядности
        addLimit(SinglePaymentLimit(BigDecimal("30000.00")))
    }

    val users = listOf("user_алиса", "user_боб", "user_карл")

    // Функция для логирования с временными метками
    fun logWithTime(message: String, userColor: String = "⚪") {
        val time = System.currentTimeMillis() % 10000
        println("[$time] $userColor $message")
    }

    // Запускаем платежи в параллельных потоках
    val threads = listOf(
        // Алиса (синий)
        Thread {
            logWithTime("Алиса: начинаю платёж 10,000", "🟦")
            limitSystem.processPayment(Payment(users[0], BigDecimal("10000.00"), "RUB"))
            logWithTime("Алиса: платёж 10,000 завершён", "🟦")

            Thread.sleep(50) // Небольшая пауза

            logWithTime("Алиса: начинаю платёж 25,000", "🟦")
            limitSystem.processPayment(Payment(users[0], BigDecimal("25000.00"), "RUB"))
            logWithTime("Алиса: платёж 25,000 завершён", "🟦")
        },

        // Боб (зелёный)
        Thread {
            Thread.sleep(10) // Начинает чуть позже

            logWithTime("Боб: начинаю платёж 15,000", "🟩")
            limitSystem.processPayment(Payment(users[1], BigDecimal("15000.00"), "RUB"))
            logWithTime("Боб: платёж 15,000 завершён", "🟩")

            Thread.sleep(30)

            logWithTime("Боб: начинаю платёж 20,000", "🟩")
            limitSystem.processPayment(Payment(users[1], BigDecimal("20000.00"), "RUB"))
            logWithTime("Боб: платёж 20,000 завершён", "🟩")
        },

        // Карл (жёлтый)
        Thread {
            Thread.sleep(20) // Начинает позже

            logWithTime("Карл: начинаю платёж 30,000", "🟨")
            val result1 = limitSystem.processPayment(Payment(users[2], BigDecimal("30000.00"), "RUB"))
            logWithTime("Карл: платёж 30,000 - $result1", "🟨")

            Thread.sleep(40)

            logWithTime("Карл: начинаю платёж 35,000", "🟨")
            val result2 = limitSystem.processPayment(Payment(users[2], BigDecimal("35000.00"), "RUB"))
            logWithTime("Карл: платёж 35,000 - $result2", "🟨")
        }
    )

    println("\n🚀 ЗАПУСКАЕМ 3 ПОТОКА...")
    threads.forEach { it.start() }
    threads.forEach { it.join() }

    println("\n✅ ВСЕ ОПЕРАЦИИ ЗАВЕРШЕНЫ!")
}