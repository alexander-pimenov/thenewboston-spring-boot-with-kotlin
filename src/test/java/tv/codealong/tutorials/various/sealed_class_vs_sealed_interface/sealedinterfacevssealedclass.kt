package tv.codealong.tutorials.various.sealed_class_vs_sealed_interface

import java.math.BigDecimal
import java.time.LocalDateTime

// ✅ ЗАКРЫТЫЙ интерфейс - известны ВСЕ реализации (это удобно для кода с when)
// Базовый интерфейс для всех результатов
sealed interface LimitCheckResult {
    val isApproved: Boolean
}

// Реализации
// Только эти реализации известны компилятору
data object Approved : LimitCheckResult {
    override val isApproved: Boolean = true
}

// Реализации
// Только эти реализации известны компилятору
data class Rejected(val reason: String) : LimitCheckResult {
    override val isApproved: Boolean = false
}

// Реализации
// Только эти реализации известны компилятору
data class RequiresApproval(val managerId: String) : LimitCheckResult {
    override val isApproved: Boolean = false
}

// Теперь можем расширять и в других файлах, в отличие от sealed class!
// В файле AntiFraud.kt
// Только эти реализации известны компилятору
data class SuspiciousActivity(val score: Int) : LimitCheckResult {
    override val isApproved: Boolean = false
}

// ✅ ЗАКРЫТЫЙ интерфейс - известны ВСЕ реализации
sealed interface PaymentResult {
    val isSuccess: Boolean
}

// Только эти реализации известны компилятору
data object Success : PaymentResult {
    override val isSuccess: Boolean = true
}

data class Failure(val error: String) : PaymentResult {
    override val isSuccess: Boolean = false
}

// Если попробуем в другом пакете:
// data class UnknownState(val code: Int) : PaymentResult { ... }
// ❌ КОМПИЛЯЦИОННАЯ ОШИБКА! Наследники должны быть в том же пакете

//Sealed Interface используй когда:
//1. Нужно реализовать несколько sealed иерархий
sealed interface ValidationResult
sealed interface ProcessingResult

// Один класс может реализовать несколько sealed интерфейсов
data class UserData(
    val name: String,
    val email: String
) : ValidationResult, ProcessingResult

//2. Реализации в разных модулях/файлах
// core модуль
sealed interface RepositoryResult<out T>

// data модуль
data class UserEntity(val id: String) : RepositoryResult<UserEntity>

// domain модуль
data class DomainUser(val id: String) : RepositoryResult<DomainUser>

//Преимущество с when:
fun handleResult(result: PaymentResult) {
    when (result) {
        is Success -> println("Успех")
        is Failure -> println("Ошибка: ${result.error}")
        // ✅ Компилятор ЗНАЕТ, что все случаи покрыты!
        // Не нужно else branch!
    }
}

//--------------------------------------------------------------------------
// Sealed Class используй когда:
//1. Есть общее состояние
sealed class ApiResponse(val timestamp: LocalDateTime) {
    data class Success(val data: User) : ApiResponse(LocalDateTime.now())
    data class Error(val message: String) : ApiResponse(LocalDateTime.now())
}

data class User(val firstName: String = "Unknown", val lastName: String = "Unknown")

//2. Нужны общие методы и 3. Все реализации логически связаны и находятся в одном модуле
sealed class PaymentStatus {
    data object Pending : PaymentStatus()
    data class Completed(val amount: BigDecimal) : PaymentStatus()
    data class Failed(val reason: String) : PaymentStatus()

    // Общие методы для всех статусов
    fun isFinal(): Boolean = this !is Pending
}



