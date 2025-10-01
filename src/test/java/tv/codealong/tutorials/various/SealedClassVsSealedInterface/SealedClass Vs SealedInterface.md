Давай разберём `sealed классы и интерфейсы` в Kotlin с подробными примерами. 
Это одна из самых мощных фич языка!

## Sealed Class vs Sealed Interface

### 1. Sealed Class (то, что я использовал)

```kotlin
// ✅ Sealed CLASS - ограниченная иерархия классов
sealed class LimitCheckResult {
    object Approved : LimitCheckResult()
    data class Rejected(val reason: String) : LimitCheckResult()
    data class Pending(val reviewId: String) : LimitCheckResult()
}
```

**Особенности:**
- Все наследники должны быть в том же файле
- Sealed class может иметь состояние (поля)
- Может иметь конструкторы (`sealed class Result(val timestamp: LocalDateTime)`)
- Наследники должны наследоваться от этого класса

### 2. Sealed Interface (Kotlin 1.5+)

```kotlin
// ✅ Sealed INTERFACE - ограниченная иерархия интерфейсов
sealed interface PaymentResult {
    object Success : PaymentResult
    data class Failure(val error: String) : PaymentResult
}

// Можно реализовать в разных классах!
data class CreditCardPayment(val cardNumber: String) : PaymentResult
data class BankTransferPayment(val account: String) : PaymentResult
```

**Ключевое отличие:** Наследники sealed interface могут быть в разных файлах!

## Практические примеры в нашей задаче

### Вариант с Sealed Class (наш случай)

```kotlin
// ✅ ХОРОШО для простых иерархий в одном файле
sealed class LimitCheckResult {
    object Approved : LimitCheckResult()
    data class Rejected(val reason: String) : LimitCheckResult()
    
    // Можем добавить методы
    fun isApproved(): Boolean = this is Approved
    fun getReasonOrNull(): String? = (this as? Rejected)?.reason
}

// Использование с when (exhaustive проверка!)
fun handleResult(result: LimitCheckResult) {
    when (result) {
        is LimitCheckResult.Approved -> processPayment()
        is LimitCheckResult.Rejected -> showError(result.reason)
        // Компилятор знает, что все случаи покрыты!
    }
}
```

### Вариант с Sealed Interface

```kotlin
// ✅ ХОРОШО для распределённых реализаций
sealed interface ValidationResult

// В одном файле
object Valid : ValidationResult
data class Invalid(val violations: List<String>) : ValidationResult

// В ДРУГОМ файле можем добавить новые реализации!
data class RequiresAdditionalData(val fields: List<String>) : ValidationResult
```

## Когда что использовать?

### 🎯 Sealed Class используй когда:

**1. Есть общее состояние**
```kotlin
sealed class ApiResponse(val timestamp: LocalDateTime) {
    data class Success(val data: User) : ApiResponse(LocalDateTime.now())
    data class Error(val message: String) : ApiResponse(LocalDateTime.now())
}
```

**2. Нужны общие методы**
```kotlin
sealed class PaymentStatus {
    object Pending : PaymentStatus()
    data class Completed(val amount: BigDecimal) : PaymentStatus()
    data class Failed(val reason: String) : PaymentStatus()
    
    // Общие методы для всех статусов
    fun isFinal(): Boolean = this !is Pending
}
```

**3. Все реализации логически связаны и находятся в одном модуле**

### 🎯 Sealed Interface используй когда:

**1. Нужно реализовать несколько sealed иерархий**
```kotlin
sealed interface ValidationResult
sealed interface ProcessingResult

// Один класс может реализовать несколько sealed интерфейсов
data class UserData(
    val name: String,
    val email: String
) : ValidationResult, ProcessingResult
```

**2. Реализации в разных модулях/файлах**
```kotlin
// core модуль
sealed interface RepositoryResult<out T>

// data модуль  
data class UserEntity(val id: String) : RepositoryResult<UserEntity>

// domain модуль
data class DomainUser(val id: String) : RepositoryResult<DomainUser>
```

**3. Нужна обратная совместимость**
```kotlin
// Старая версия
sealed class Result

// Новая версия - можем добавить без изменения существующего кода
sealed interface Result
class NewResultType : Result
```

## Перепишем нашу задачу с Sealed Interface

```kotlin
// Базовый интерфейс для всех результатов
sealed interface LimitCheckResult {
    val isApproved: Boolean
}

// Реализации
object Approved : LimitCheckResult {
    override val isApproved: Boolean = true
}

data class Rejected(val reason: String) : LimitCheckResult {
    override val isApproved: Boolean = false
}

data class RequiresApproval(val managerId: String) : LimitCheckResult {
    override val isApproved: Boolean = false
}

// Теперь можем расширять в других файлах!
// В файле AntiFraud.kt
data class SuspiciousActivity(val score: Int) : LimitCheckResult {
    override val isApproved: Boolean = false
}
```

## Сравнение в таблице

| Критерий | Sealed Class | Sealed Interface |
|----------|--------------|------------------|
| **Наследование** | Только классы | Классы, object, data class |
| **Расположение** | Один файл | Разные файлы |
| **Множественное наследование** | ❌ Нет | ✅ Да |
| **Состояние** | ✅ Может иметь поля | ❌ Только свойства |
| **Методы** | ✅ Может иметь реализацию | ❌ Только declaration |
| **Когда использовать** | Логически единая иерархия | Распределённые реализации |

## Практический пример из реального проекта

```kotlin
// Sealed Interface для кеширования
sealed interface CacheResult<out T> {
    data class Hit<T>(val value: T) : CacheResult<T>
    object Miss : CacheResult<Nothing>
    data class Error(val exception: Throwable) : CacheResult<Nothing>
}

// Sealed Class для UI состояния
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    
    // Общие методы
    fun isLoading(): Boolean = this is Loading
    fun getDataOrNull(): T? = (this as? Success)?.data
}
```

## Современный подход (Kotlin 1.8+)

Сейчас часто рекомендуют использовать sealed interfaces по умолчанию:

```kotlin
// ✅ Современный стиль
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    object Loading : Result<Nothing>
}

// Более гибко, можно расширять
data class PartialSuccess<T>(val data: T, val warnings: List<String>) : Result<T>
```

## Вывод для нашей задачи

**Для `LimitCheckResult` я выбрал sealed class потому что:**
1. Это простая иерархия в одном файле
2. Нет необходимости расширять из других модулей
3. В будущем могу добавить общие методы/свойства

**Но sealed interface тоже был бы хорош:**
```kotlin
sealed interface LimitCheckResult {
    val isApproved: Boolean
}

object Approved : LimitCheckResult {
    override val isApproved: Boolean = true
}

data class Rejected(val reason: String) : LimitCheckResult {
    override val isApproved: Boolean = false
}
```

Оба подхода работают отлично! Выбор зависит от твоих конкретных потребностей в проекте.

Теперь понятна разница? Это действительно мощный инструмент для создания типобезопасных иерархий!

---

