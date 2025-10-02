package tv.codealong.tutorials.various.sealedclassvssealedinterface

interface LimitCheckResultCm {
    val isApproved: Boolean
}

object ApprovedCm : LimitCheckResultCm {
    override val isApproved: Boolean = true
}

data class RejectedCm(val reason: String) : LimitCheckResultCm {
    override val isApproved: Boolean = false
}
// Где-то в другом модуле...
data class RequiresManagerApproval(val managerId: String) : LimitCheckResultCm {
    override val isApproved: Boolean = false
}

// В нашем коде:
fun processResult(result: LimitCheckResultCm) {
    when (result) {
        is ApprovedCm -> {}// обработать
        is RejectedCm -> {}// обработать
        // ❌ Забыли про RequiresManagerApproval!
        // Компилятор не подскажет!

    }
}
//---------------------------------------------------------------

sealed interface LimitCheckResultSl {
    val isApproved: Boolean
}

data object ApprovedSl : LimitCheckResultSl {
    override val isApproved: Boolean = true
}

data class RejectedSl(val reason: String) : LimitCheckResultSl {
    override val isApproved: Boolean = false
}

data class RequiresManagerApprovalSl(val managerId: String) : LimitCheckResultSl {
    override val isApproved: Boolean = false
}

fun processResult(result: LimitCheckResultSl) {
    when (result) {
        is ApprovedSl -> println("Одобрено")
        is RejectedSl -> println("Отклонено: ${result.reason}")
        is RequiresManagerApprovalSl -> println("Нужно одобрение менеджера: ${result.managerId}")
        // ✅ Компилятор гарантирует, что ВСЕ случаи обработаны!
    }
}

//-----------------------------------------------------------

// Внутри модуля - sealed для безопасности
sealed interface DatabaseResultSl<out T>
data class SuccessSl<T>(val data: T) : DatabaseResultSl<T>
data class ErrorSl(val exception: Throwable) : DatabaseResultSl<Nothing>

// Публичное API - обычный интерфейс для расширяемости
interface Repository<T> {
    fun save(entity: T): DatabaseResultSl<Unit>
}