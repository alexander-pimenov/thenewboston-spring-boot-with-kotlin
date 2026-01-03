package tv.codealong.tutorials.various.sealed_class.sealed_classes_examples

import java.math.BigDecimal

//2. Sealed Class с методами
//
// ✅ Sealed class с методами реализации
sealed class PaymentMethod(val fee: BigDecimal) {
    abstract val maxAmount: BigDecimal

    // Общий метод для всех платежей
    fun calculateTotal(amount: BigDecimal): BigDecimal {
        return amount + (amount * fee / BigDecimal(100))
    }

    // Разные реализации
    data object CreditCard : PaymentMethod(fee = BigDecimal("2.5")) {
        override val maxAmount: BigDecimal = BigDecimal("100000")
    }

    data object BankTransfer : PaymentMethod(fee = BigDecimal("0.5")) {
        override val maxAmount: BigDecimal = BigDecimal("1000000")
    }

    data class Crypto(val wallet: String) : PaymentMethod(fee = BigDecimal("1.0")) {
        override val maxAmount: BigDecimal = BigDecimal("500000")
    }

    // Метод, использующий sealed nature
    fun canProcess(amount: BigDecimal): Boolean {
        return amount <= maxAmount
    }
}

// Использование
fun processPayment(amount: BigDecimal, method: PaymentMethod) {
    println("Обработка платежа...")
    println("Метод: ${method::class.simpleName}, Комиссия: ${method.fee}%")
    // Проверка лимита
    if (!method.canProcess(amount)) {
        println("Превышен лимит для ${method::class.simpleName}")
        return
    }

    val total = method.calculateTotal(amount)
    println("К оплате: $total (включая комиссию ${method.fee}%)")
}