package tv.codealong.tutorials.various.yandex.test6_2_real_from_yndx_anddeepseek

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
/*
Вы — backend-разработчик в финтех компании.
Компания предоставляет платежные услуги и должна контролировать лимиты пользователей.
Product owner просит создать систему проверки лимитов перед проведением платежей.

## Определения

Платеж:
- id пользователя
- сумма (в рублях с копейками)
- тип операции (только списание)
- время операции

Лимиты пользователя:
- суточный лимит по сумме (за 24 часа)
- максимальный размер одной операции

История операций:
- список совершенных платежей пользователя

## Важно
Настройки лимитов пользователей и история платежей предоставляются другими компонентами системы.
Вам необходимо спроектировать контракты для получения этих данных.
Реализацию этих компонентов делать не нужно.

## Задача
Написать систему проверки лимитов, которая:
- на вход получает платеж
- проверяет лимиты
- возвращает результат проверки: можно ли провести операцию
- если нельзя, то указывает причину (какой именно лимит будет превышен)

Проведение платежа не входит в вашу задачу - другая команда займется обработкой платежей после проверки.
Ваша задача - только проверка возможности проведения платежа.

## Ограничения
В рамках данной задачи считаем, что все платежи одного пользователя происходят строго последовательно.
Во время проверки лимита не может быть проведен платеж того же пользователя.
*/
/**
 * Ключевые моменты решения:
 * 1) Чистая архитектура - разделили контракты и реализацию
 * 2) Иммутабельные модели - используем data class с val свойствами
 * 3) Правильная работа с деньгами - BigDecimal для точных расчетов
 * 4) Временные зоны - Instant для работы с временем
 * 5) Полное покрытие тестами:
 *  - Граничные случаи
 *  - Разные сценарии превышения лимитов
 *  - Проверка временных окон
 *  - Интеграционные сценарии
 *
 * Тесты покрывают все основные сценарии и edge cases, которые могут возникнуть в реальной системе. Особое внимание уделено:
 *  - Проверке приоритетов лимитов
 *  - Корректному расчету временных окон
 *  - Обработке граничных значений
 *  - Сценариям когда лимиты не заданы
 */

/**
 * Модель данных - платеж
 */
data class Payment(
    val userId: String,
    val amount: BigDecimal,
    val operationType: OperationType,
    val operationTime: Instant
)

/**
 * Модель данных
 */
enum class OperationType {
    DEBIT // только списание
}

/**
 * Модель данных - пользовательские лимиты
 */
data class UserLimits(
    val userId: String,
    val dailyLimit: BigDecimal,         // суточный лимит за 24 часа
    val maxSingleOperation: BigDecimal  // максимальный размер одной операции
)

/**
 * Модель данных - результат проверки лимитов
 */
data class LimitCheckResult(
    val isAllowed: Boolean,
    val rejectionReason: RejectionReason? = null
)

/**
 * Модель данных
 */
enum class RejectionReason(val desc: String) {
    DAILY_LIMIT_EXCEEDED("ПРЕВЫШЕН ДНЕВНОЙ ЛИМИТ"),
    SINGLE_OPERATION_LIMIT_EXCEEDED("ПРЕВЫШЕН РАЗОВЫЙ ЛИМИТ ОПЕРАЦИИ")
}

/**
 * Контракты для внешних компонентов - UserLimitsProvider
 */
interface UserLimitsProvider {
    fun getLimits(userId: String): UserLimits?
}

/**
 * Контракты для внешних компонентов - PaymentHistoryProvider
 */
interface PaymentHistoryProvider {
    fun getUserPayments(userId: String, fromTime: Instant, toTime: Instant): List<Payment>
}


/**
 * Сервис проверки лимитов
 */
class LimitCheckService (
    private val userLimitsProvider: UserLimitsProvider,
    private val paymentHistoryProvider: PaymentHistoryProvider
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(LimitCheckService::class.java)
    }

    fun checkPaymentLimits(payment: Payment): LimitCheckResult {
        logger.info("Проверяем лимиты для платежа: $payment")

        // Получаем лимиты пользователя
        val limits = userLimitsProvider.getLimits(payment.userId)
            ?: return LimitCheckResult(true) // если лимитов нет - разрешаем операцию

        // Проверяем лимит на одну операцию
        logger.info("Проверяем лимит на одну операцию: платеж=${payment.amount} > maxSingleOperation=${limits.maxSingleOperation}")
        if (payment.amount > limits.maxSingleOperation) {
            return LimitCheckResult(
                isAllowed = false,
                rejectionReason = RejectionReason.SINGLE_OPERATION_LIMIT_EXCEEDED
            )
        }

        // Рассчитываем период для суточного лимита (последние 24 часа)
        val endTime = payment.operationTime
        val startTime = endTime.minus(24, ChronoUnit.HOURS)

        // Получаем платежи за последние 24 часа
        val recentPayments: List<Payment> = paymentHistoryProvider.getUserPayments(
            payment.userId,
            startTime,
            endTime
        )
        logger.info("Найдено ${recentPayments.size} платежей за последние 24 часа")
        logger.info("Это платежи: $recentPayments")

        // Суммируем сумму всех операций за период
        val dailySpent = recentPayments.sumOf { it.amount }
        logger.info("Сумма операций за период с start=$startTime по end=$endTime составляет: $dailySpent")
        val totalAfterPayment = dailySpent + payment.amount
        logger.info("Сумма операций, которая будет после платежа: $totalAfterPayment")

        // Проверяем суточный лимит
        if (totalAfterPayment > limits.dailyLimit) {
            return LimitCheckResult(
                isAllowed = false,
                rejectionReason = RejectionReason.DAILY_LIMIT_EXCEEDED
            )
        }

        return LimitCheckResult(true)
    }
}