package tv.codealong.tutorials.various.yandex.test6_1_real_from_yndx

import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.plus

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

data class Payment(
    val userId: String,
    val amount: BigDecimal,
    val typeOperation: TypeOperation,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class TypeOperation(
    val desc: String
) {
    DEBIT("списание")
}

class DayLimit(
    val maxAmount: BigDecimal,
    val maxAmountPerOperation: BigDecimal
)

interface LimitService {
    fun checkPaymentLimit(payment: Payment): CheckStatusLimit
}

interface LimitRepo {
    fun getDayLimit(payment: Payment): DayLimit
}

interface HistoryOperation {
    fun getHistoryOfUserPayments(userId: String, typeOperation: TypeOperation, startTime: LocalDateTime, endTime: LocalDateTime): List<Payment>
}

class LimitServiceImpl(
    private val repo: LimitRepo,
    private val historyOperation: HistoryOperation,
) : LimitService {


    override fun checkPaymentLimit(payment: Payment): CheckStatusLimit {
        val dayLimit = repo.getDayLimit(payment)
        val checkOne: CheckStatusLimit = checkDayLimitAmount(payment, dayLimit)
        val checkTwo: CheckStatusLimit = checkAmountPerOperation(payment, dayLimit)

        return if (checkOne == CheckStatusLimit.Approved && checkTwo == CheckStatusLimit.Approved) {
            CheckStatusLimit.Approved
        } else if (checkOne == CheckStatusLimit.Approved && checkTwo == CheckStatusLimit.Approved) {
            checkTwo
        } else if (checkOne == CheckStatusLimit.Approved && checkTwo == CheckStatusLimit.Approved) {
            checkOne
        } else {
            CheckStatusLimit.Reject("не прошли оба")
        }
    }

    private fun checkDayLimitAmount(payment: Payment, limit: DayLimit): CheckStatusLimit {

        val listPayment =
            historyOperation.getHistoryOfUserPayments(payment.userId, payment.typeOperation, LocalDateTime.now().minusDays(1), LocalDateTime.now())
        val map = listPayment.sumOf { it.amount }
        val all = payment.amount + map
        return if (all >= limit.maxAmount) {
            CheckStatusLimit.Reject("limit.maxAmount + ${payment.amount}")
        } else {
            CheckStatusLimit.Approved
        }
    }

    private fun checkAmountPerOperation(payment: Payment, limit: DayLimit): CheckStatusLimit {
        return if (payment.amount >= limit.maxAmountPerOperation) {
            CheckStatusLimit.Reject("limit.maxAmountPerOperation")
        } else {
            CheckStatusLimit.Approved
        }
    }
}

sealed class CheckStatusLimit {

    object Approved : CheckStatusLimit()
    class Reject(val description: String) : CheckStatusLimit()

}

