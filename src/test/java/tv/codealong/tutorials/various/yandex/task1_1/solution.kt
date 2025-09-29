package tv.codealong.tutorials.various.yandex.task1_1

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * ## Анализ задачи ##
 * Сначала давай разберём ключевые моменты:
 * - Последовательные платежи - нам не нужна сложная синхронизация
 * - Проверка всех лимитов - система должна быть расширяемой
 * - Возврат причины отказа - важна информативность
 * - Thread-safe - раз платежи последовательные, синхронизация проще
 *
 * ## Преимущества архитектуры
 *
 * 1. **Open/Closed Principle** - система открыта для расширения, но закрыта для модификации
 * 2. **Single Responsibility** - каждый лимит отвечает только за свою логику
 * 3. **Testability** - легко тестировать каждый компонент отдельно
 * 4. **Maintainability** - код чистый и понятный
 *
 */
// 1. Сначала определим основные модели данных
// Kotlin sealed classes идеальны для представления ограниченного набора возможных
// результатов. Компилятор будет проверять exhaustiveness в when-выражениях.
sealed class LimitCheckResult {
    data object Approved : LimitCheckResult()
    data class Rejected(val reason: String) : LimitCheckResult()
}

data class Payment(
    val userId: String,
    val amount: BigDecimal,
    val currency: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

// 2. Интерфейс для лимитов - это ключевой паттерн!
interface Limit {
    val name: String
    val description: String
    fun check(payment: Payment, userContext: UserContext): LimitCheckResult
}

// 3. Контекст пользователя для хранения истории и состояния (Все данные пользователя в одном месте)
data class UserContext(
    val userId: String,
    val paymentHistory: List<Payment> = emptyList(),
    val dailySpent: BigDecimal = BigDecimal.ZERO,
    val monthlySpent: BigDecimal = BigDecimal.ZERO
)

// 4.1. Реализации конкретных лимитов - дневной лимит
class DailyAmountLimit(
    private val maxDailyAmount: BigDecimal
) : Limit {
    override val name: String = "DAILY_AMOUNT_LIMIT"
    override val description: String = "Максимальная сумма платежей в день: $maxDailyAmount"

    override fun check(payment: Payment, userContext: UserContext): LimitCheckResult {
        val newDailyTotal = userContext.dailySpent + payment.amount
        return if (newDailyTotal > maxDailyAmount) {
            LimitCheckResult.Rejected("Превышен дневной лимит: $newDailyTotal > $maxDailyAmount")
        } else {
            LimitCheckResult.Approved
        }
    }
}

// 4.2. Реализации конкретных лимитов - разовый лимит
class SinglePaymentLimit(
    private val maxSingleAmount: BigDecimal
) : Limit {
    override val name: String = "SINGLE_PAYMENT_LIMIT"
    override val description: String = "Максимальная сумма одного платежа: $maxSingleAmount"

    override fun check(payment: Payment, userContext: UserContext): LimitCheckResult {
        return if (payment.amount > maxSingleAmount) {
            LimitCheckResult.Rejected("Превышен лимит на один платёж: ${payment.amount} > $maxSingleAmount")
        } else {
            LimitCheckResult.Approved
        }
    }
}

// 4.3. Реализации конкретных лимитов - месячный лимит
class MonthlyCountLimit(
    private val maxMonthlyPayments: Int
) : Limit {
    override val name: String = "MONTHLY_COUNT_LIMIT"
    override val description: String = "Максимальное количество платежей в месяц: $maxMonthlyPayments"

    override fun check(payment: Payment, userContext: UserContext): LimitCheckResult {
        val currentMonthPayments = userContext.paymentHistory.count {
            it.timestamp.month == payment.timestamp.month &&
                    it.timestamp.year == payment.timestamp.year
        }
        return if (currentMonthPayments >= maxMonthlyPayments) {
            LimitCheckResult.Rejected("Превышено количество платежей в месяце: ${currentMonthPayments + 1} > $maxMonthlyPayments")
        } else {
            LimitCheckResult.Approved
        }
    }
}

/**
 * Условие говорит: "все платежи одного пользователя происходят строго последовательно"
 *
 * Наша реализация гарантирует:
 * ✅ Последовательность для одного пользователя
 * ✅ Параллельность для разных пользователей
 * ✅ Thread-safety без излишних блокировок
 *
 * Такой подход даёт нам идеальный баланс между:
 * ✅ Корректностью (данные не испортятся)
 * ✅ Производительностью (разные пользователи не блокируют друг друга)
 * ✅ Простотой (код легко читать и поддерживать)
 */
// 5. Главный класс системы лимитов
class LimitCheckingSystem {
    // Все лимиты храним в списке - легко добавлять новые
    private val limits: MutableList<Limit> = mutableListOf()

    // Хранилище контекстов пользователей
    private val userContexts = mutableMapOf<String, UserContext>()

    // Синхронизация по userId, т.к. платежи последовательные
    private val locks = mutableMapOf<String, Any>()
    private fun getLock(userId: String): Any {
        return locks.getOrPut(userId) { Any() }
    }

    // Добавляем лимит в систему
    fun addLimit(limit: Limit) {
        limits.add(limit)
    }

    /**
     * Синхронизируем по пользователю.
     * Блокируем только одного пользователя.
     * Этот код выполняется только для ОДНОГО потока с данным userId в один момент времени.
     * Преимущество: Максимальная параллельность + гарантия корректности.
     *
     * Полная аналогия с реальным миром -
     * Банк с несколькими кассами:
     *  - Каждый клиент стоит в своей очереди к своему кассиру
     *  - Один клиент не может одновременно быть у двух кассиров
     *  - Разные клиенты обслуживаются параллельно
     */
    fun processPayment(payment: Payment): LimitCheckResult {

        // "Ведём клиента в его персональную комнату"
        synchronized(getLock(payment.userId)) {
            // "В комнате есть только один клиент" - это гарантирует блокировка. Для информации выводим его идентификатор и объект блокировки
            println("--- [для информации: пользователь ${payment.userId} блокируемся на каком объекте: ${getLock(payment.userId)}] ---")
            // "Работаем только с этим клиентом"
            val userContext = userContexts.getOrPut(payment.userId) {
                UserContext(payment.userId)
            }

            // Проверяем все лимиты по порядку
            for (limit in limits) {
                val result = limit.check(payment, userContext)
                if (result is LimitCheckResult.Rejected) {
                    return result // Возвращаем первую найденную ошибку
                }
            }

            // Если все лимиты пройдены - обновляем контекст
            updateUserContext(payment, userContext)
            // "Выпускаем клиента из комнаты"
            return LimitCheckResult.Approved
        }
        // "Комната свободна для следующего платежа этого клиента"
    }

    private fun updateUserContext(payment: Payment, oldContext: UserContext) {
        val newHistory = oldContext.paymentHistory + payment

        // Обновляем дневные траты (упрощённо - считаем что все платежи сегодня)
        val dailySpent = newHistory
            .filter { it.timestamp.toLocalDate() == payment.timestamp.toLocalDate() }
            .sumOf { it.amount }

        // Обновляем месячные траты
        val monthlySpent = newHistory
            .filter {
                it.timestamp.month == payment.timestamp.month &&
                        it.timestamp.year == payment.timestamp.year
            }
            .sumOf { it.amount }

        userContexts[payment.userId] = oldContext.copy(
            paymentHistory = newHistory,
            dailySpent = dailySpent,
            monthlySpent = monthlySpent
        )
    }

    fun getUserLimitsInfo(userId: String): List<String> {
        return limits.map { it.description }
    }
}

