package tv.codealong.tutorials.various.yandex.task1_1

import java.math.BigDecimal

// 6. Пример использования
fun main() {
    // Создаём систему
    val limitSystem = LimitCheckingSystem().apply {
        addLimit(DailyAmountLimit(BigDecimal("100000.00")))
        addLimit(SinglePaymentLimit(BigDecimal("50000.00")))
        addLimit(MonthlyCountLimit(100))
    }

    // Тестовые платежи - создаем платежи
    val payment1 = Payment("user123", BigDecimal("30000.00"), "RUB")
    val payment2 = Payment("user123", BigDecimal("80000.00"), "RUB") // Должен превысить лимит

    // Проверяем платежи
    println("Лимиты пользователя: ${limitSystem.getUserLimitsInfo("user123")}")

    //обрабатываем платежи
    val result1 = limitSystem.processPayment(payment1)
    println("Платёж 1 для ${payment1.userId}: $result1")

    //обрабатываем платежи
    val result2 = limitSystem.processPayment(payment2)
    println("Платёж 2 для ${payment2.userId}: $result2")

    // Тест для другого пользователя
    val payment3 = Payment("user456", BigDecimal("10000.00"), "RUB")
    val result3 = limitSystem.processPayment(payment3)
    println("Платёж 3 для ${payment3.userId}: $result3")
}