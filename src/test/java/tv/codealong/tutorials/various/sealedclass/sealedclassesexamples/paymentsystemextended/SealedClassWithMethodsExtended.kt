package tv.codealong.tutorials.various.sealedclass.sealedclassesexamples.paymentsystemextended

import java.math.BigDecimal

/**
 * sealed class - Это как иметь общий интерфейс + общую реализацию + типобезопасность! 🚀
 * Payment System:
 * - Общее состояние: name, feePercentage, supportedCurrencies
 * - Общие методы: calculateFee(), validateAmount(), getProcessingInfo()
 * - Специфичные методы: каждый платёжный метод имеет свою логику обработки
 */
//💳 Расширенный Sealed Class с методами (Payment System)
// ✅ Улучшенная система платежей с бизнес-логикой
sealed class PaymentMethod(
    val name: String,
    val feePercentage: BigDecimal,
    val supportedCurrencies: Set<String> = setOf("USD", "EUR", "GBP")
) {
    abstract val maxAmount: BigDecimal
    abstract val minAmount: BigDecimal
    abstract val processingTime: String

    // ОБЩИЕ МЕТОДЫ для всех платёжных методов
    fun calculateFee(amount: BigDecimal): BigDecimal {
        return amount * feePercentage / BigDecimal(100)
    }

    fun calculateTotal(amount: BigDecimal): BigDecimal {
        return amount + calculateFee(amount)
    }

    fun isCurrencySupported(currency: String): Boolean {
        return currency.uppercase() in supportedCurrencies
    }

    fun validateAmount(amount: BigDecimal): ValidationResult {
        return when {
            amount < minAmount -> ValidationResult.Error("Минимальная сумма: $minAmount")
            amount > maxAmount -> ValidationResult.Error("Максимальная сумма: $maxAmount")
            else -> ValidationResult.Success
        }
    }

    fun getProcessingInfo(): String {
        return """
            💳 $name
            💰 Комиссия: $feePercentage%
            ⏱️ Время обработки: $processingTime
            📊 Лимиты: $minAmount - $maxAmount
            🌍 Валюты: ${supportedCurrencies.joinToString()}
        """.trimIndent()
    }

    // Абстрактные методы для специфичной логики
    abstract fun processPayment(amount: BigDecimal, currency: String): PaymentResult
    abstract fun refund(transactionId: String): RefundResult

    // Sealed class для результатов валидации
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    // Sealed class для результатов платежа
    sealed class PaymentResult {
        data class Success(val transactionId: String, val fee: BigDecimal) : PaymentResult()
        data class Failure(val reason: String, val errorCode: Int) : PaymentResult()
        object Pending : PaymentResult()
    }

    // Sealed class для результатов возврата
    sealed class RefundResult {
        data class Success(val refundId: String, val amount: BigDecimal) : RefundResult()
        data class Failure(val reason: String) : RefundResult()
        data class Partial(val refundId: String, val refundedAmount: BigDecimal, val originalAmount: BigDecimal) : RefundResult()
    }
}

// 💳 КРЕДИТНАЯ КАРТА
object CreditCard : PaymentMethod(
    name = "Credit Card",
    feePercentage = BigDecimal("2.5"),
    supportedCurrencies = setOf("USD", "EUR", "GBP", "JPY", "CAD")
) {
    override val maxAmount: BigDecimal = BigDecimal("100000")
    override val minAmount: BigDecimal = BigDecimal("1")
    override val processingTime: String = "1-2 business days"

    private val transactions = mutableMapOf<String, BigDecimal>()

    override fun processPayment(amount: BigDecimal, currency: String): PaymentResult {
        if (!isCurrencySupported(currency)) {
            return PaymentResult.Failure("Валюта $currency не поддерживается", 1001)
        }

        val validation = validateAmount(amount)
        if (validation is ValidationResult.Error) {
            return PaymentResult.Failure(validation.message, 1002)
        }

        // Имитация обработки
        val transactionId = "CC_${System.currentTimeMillis()}"
        val fee = calculateFee(amount)
        transactions[transactionId] = amount

        return PaymentResult.Success(transactionId, fee)
    }

    override fun refund(transactionId: String): RefundResult {
        val originalAmount = transactions[transactionId]
        return if (originalAmount != null) {
            transactions.remove(transactionId)
            RefundResult.Success("REF_$transactionId", originalAmount)
        } else {
            RefundResult.Failure("Транзакция не найдена")
        }
    }

    // Кредитная карта-специфичные методы
    fun getTransactionHistory(): Map<String, BigDecimal> = transactions.toMap()

    fun getTotalProcessed(): BigDecimal = transactions.values.sumOf { it }
}

// 🏦 БАНКОВСКИЙ ПЕРЕВОД
object BankTransfer : PaymentMethod(
    name = "Bank Transfer",
    feePercentage = BigDecimal("0.5"),
    supportedCurrencies = setOf("USD", "EUR", "GBP", "RUB", "CNY")
) {
    override val maxAmount: BigDecimal = BigDecimal("1000000")
    override val minAmount: BigDecimal = BigDecimal("10")
    override val processingTime: String = "3-5 business days"

    override fun processPayment(amount: BigDecimal, currency: String): PaymentResult {
        if (!isCurrencySupported(currency)) {
            return PaymentResult.Failure("Валюта $currency не поддерживается", 2001)
        }

        val validation = validateAmount(amount)
        if (validation is ValidationResult.Error) {
            return PaymentResult.Failure(validation.message, 2002)
        }

        // Банковские переводы обрабатываются дольше
        return PaymentResult.Pending
    }

    override fun refund(transactionId: String): RefundResult {
        // Банковские возвраты сложнее
        return RefundResult.Failure("Возвраты для банковских переводов обрабатываются вручную")
    }

    // Банковский перевод-специфичные методы
    fun generateIBAN(account: String): String {
        return "IBAN${account.hashCode().toString().takeLast(10)}"
    }

    fun requiresSwift(currency: String): Boolean {
        return currency !in setOf("USD", "EUR")
    }
}

// ₿ КРИПТОВАЛЮТА
data class CryptoPayment(
    val wallet: String,
    val cryptoType: String = "BTC"
) : PaymentMethod(
    name = "Cryptocurrency",
    feePercentage = BigDecimal("1.0"),
    supportedCurrencies = setOf("BTC", "ETH", "USDT")
) {
    override val maxAmount: BigDecimal = BigDecimal("500000")
    override val minAmount: BigDecimal = BigDecimal("0.001")
    override val processingTime: String = "10-30 minutes"

    private val cryptoTransactions = mutableMapOf<String, CryptoTransaction>()

    override fun processPayment(amount: BigDecimal, currency: String): PaymentResult {
        if (currency != cryptoType) {
            return PaymentResult.Failure("Поддерживается только $cryptoType", 3001)
        }

        val validation = validateAmount(amount)
        if (validation is ValidationResult.Error) {
            return PaymentResult.Failure(validation.message, 3002)
        }

        val transactionId = "CRYPTO_${System.currentTimeMillis()}"
        val fee = calculateFee(amount)

        cryptoTransactions[transactionId] = CryptoTransaction(
            amount = amount,
            wallet = wallet,
            timestamp = System.currentTimeMillis(),
            cryptoType = cryptoType
        )

        return PaymentResult.Success(transactionId, fee)
    }

    override fun refund(transactionId: String): RefundResult {
        val transaction = cryptoTransactions[transactionId]
        return if (transaction != null) {
            // В крипте возвраты обычно partial из-за комиссий сети
            val refundableAmount = transaction.amount * BigDecimal("0.95") // 5% теряется
            cryptoTransactions.remove(transactionId)
            RefundResult.Partial("CRYPTO_REF_$transactionId", refundableAmount, transaction.amount)
        } else {
            RefundResult.Failure("Крипто-транзакция не найдена")
        }
    }

    // Крипто-специфичные методы
    fun getWalletBalance(): BigDecimal {
        return cryptoTransactions.values.sumOf { it.amount } * BigDecimal("35000") // примерный курс
    }

    fun getTransactionCount(): Int = cryptoTransactions.size

    data class CryptoTransaction(
        val amount: BigDecimal,
        val wallet: String,
        val timestamp: Long,
        val cryptoType: String
    )
}

// 🎯 ДЕМОНСТРАЦИЯ СИСТЕМЫ ПЛАТЕЖЕЙ
fun main() {
    println("=== 💳 СИСТЕМА ПЛАТЕЖЕЙ ===")

    // Тестируем разные платёжные методы
    val paymentMethods: List<PaymentMethod> = listOf(
        CreditCard,
        BankTransfer,
        CryptoPayment(wallet = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", cryptoType = "BTC")
    )

    // Демонстрация для каждой платёжной системы
    paymentMethods.forEach { method ->
        println("\n${method.getProcessingInfo()}")
        println("-".repeat(50))

        // Тестируем валидацию
        val testAmount = BigDecimal("500")
        val validation = method.validateAmount(testAmount)

        when (validation) {
            is PaymentMethod.ValidationResult.Success -> {
                println("✅ Валидация пройдена для суммы: $testAmount")

                // Рассчитываем комиссию
                val fee = method.calculateFee(testAmount)
                val total = method.calculateTotal(testAmount)
                println("💸 Комиссия: $fee")
                println("💰 Итого к оплате: $total")

                // Пробуем обработать платёж
                val result = method.processPayment(testAmount, "USD")
                when (result) {
                    is PaymentMethod.PaymentResult.Success -> {
                        println("✅ Платёж успешен! ID: ${result.transactionId}")
                        println("📊 Комиссия: ${result.fee}")
                    }

                    is PaymentMethod.PaymentResult.Failure -> {
                        println("❌ Ошибка платежа: ${result.reason} (код: ${result.errorCode})")
                    }

                    is PaymentMethod.PaymentResult.Pending -> {
                        println("⏳ Платёж в обработке...")
                    }
                }
            }

            is PaymentMethod.ValidationResult.Error -> {
                println("❌ Ошибка валидации: ${validation.message}")
            }
        }

        // Демонстрация специфичных методов
        when (method) {
            is CreditCard -> {
                println("\n💳 Специфичные методы Credit Card:")
                println("📈 Всего обработано: ${method.getTotalProcessed()}")
                println("📋 История транзакций: ${method.getTransactionHistory().size} транзакций")
            }

            is BankTransfer -> {
                println("\n🏦 Специфичные методы Bank Transfer:")
                println("🏷️  IBAN: ${method.generateIBAN("123456789")}")
                println("🌐 Требуется SWIFT для JPY: ${method.requiresSwift("JPY")}")
            }

            is CryptoPayment -> {
                println("\n₿ Специфичные методы Crypto:")
                println("👛 Кошелёк: ${method.wallet}")
                println("💎 Баланс: ~${method.getWalletBalance()} USD")
                println("🔗 Транзакций: ${method.getTransactionCount()}")
            }
        }
    }

    // Демонстрация возвратов
    println("\n=== 🔄 ТЕСТИРОВАНИЕ ВОЗВРАТОВ ===")
    testRefunds()
}

fun testRefunds() {
    // Создаём успешную транзакцию
    val result = CreditCard.processPayment(BigDecimal("100"), "USD")

    if (result is PaymentMethod.PaymentResult.Success) {
        println("✅ Создана транзакция: ${result.transactionId}")

        // Пробуем вернуть
        val refundResult = CreditCard.refund(result.transactionId)
        when (refundResult) {
            is PaymentMethod.RefundResult.Success -> {
                println("✅ Возврат успешен: ${refundResult.refundId}")
                println("💰 Возвращено: ${refundResult.amount}")
            }

            is PaymentMethod.RefundResult.Failure -> {
                println("❌ Ошибка возврата: ${refundResult.reason}")
            }

            is PaymentMethod.RefundResult.Partial -> {
                println("⚠️  Частичный возврат: ${refundResult.refundId}")
                println("💰 Возвращено: ${refundResult.refundedAmount} из ${refundResult.originalAmount}")
            }
        }
    }
}

// Вспомогательная функция для повторения строк
operator fun String.times(n: Int): String = repeat(n)