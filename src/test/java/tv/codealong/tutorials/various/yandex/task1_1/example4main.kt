package tv.codealong.tutorials.various.yandex.task1_1

import java.math.BigDecimal

// 6. Пример использования
// Упрощённый пример для быстрого тестирования (без потоков):
fun main() {
    simpleThreeUsersExample()
}

fun simpleThreeUsersExample() {
    println("=== ПРОСТОЙ ТЕСТ 3-Х ПОЛЬЗОВАТЕЛЕЙ ===")

    val system = LimitCheckingSystem().apply {
        addLimit(DailyAmountLimit(BigDecimal("1000.00")))
    }

    val users = listOf("user1", "user2", "user3")

    // Быстрые последовательные вызовы
    users.forEach { user ->
        repeat(3) { index ->
            val amount = BigDecimal((index + 1) * 100)
            // Создаём платеж
            val payment = Payment(user, amount, "RUB")
            // Обрабатываем платёж
            val result = system.processPayment(payment)
            println("$user: платёж $amount -> $result")
        }
        println("---")
    }
}