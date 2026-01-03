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

Это действительно мощный инструмент для создания типобезопасных иерархий!

---

## Отличия Обычного Interface от Sealed Interface 
Отличный вопрос! Ты не совсем ошибаешься, но есть **ключевое отличие**. Давай разберём на простых примерах.

## Обычный Interface vs Sealed Interface

### 1. Обычный Interface (открытый)

```kotlin
// ❌ ОТКРЫТЫЙ интерфейс - кто угодно может реализовать
interface PaymentResult {
    val isSuccess: Boolean
}

// Может быть реализован где угодно, когда угодно, кем угодно
object Success : PaymentResult {
    override val isSuccess: Boolean = true
}

data class Failure(val error: String) : PaymentResult {
    override val isSuccess: Boolean = false
}

// В ЛЮБОМ другом файле можно добавить:
data class UnknownState(val code: Int) : PaymentResult {
    override val isSuccess: Boolean = false
}
```

**Проблема с `when`:**
```kotlin
fun handleResult(result: PaymentResult) {
    when (result) {
        is Success -> println("Успех")
        is Failure -> println("Ошибка: ${result.error}")
        // ❌ КОМПИЛЯТОР НЕ ЗНАЕТ о UnknownState!
        // Может быть непокрытый случай!
    }
}
```

### 2. Sealed Interface (закрытый)

```kotlin
// ✅ ЗАКРЫТЫЙ интерфейс - известны ВСЕ реализации
sealed interface PaymentResult {
    val isSuccess: Boolean
}

// Только эти реализации известны компилятору
object Success : PaymentResult {
    override val isSuccess: Boolean = true
}

data class Failure(val error: String) : PaymentResult {
    override val isSuccess: Boolean = false
}

// Если попробуем в другом файле:
// data class UnknownState(val code: Int) : PaymentResult { ... }
// ❌ КОМПИЛЯЦИОННАЯ ОШИБКА! Наследники должны быть в том же пакете
```

**Преимущество с `when`:**
```kotlin
fun handleResult(result: PaymentResult) {
    when (result) {
        is Success -> println("Успех")
        is Failure -> println("Ошибка: ${result.error}")
        // ✅ Компилятор ЗНАЕТ, что все случаи покрыты!
        // Не нужно else branch!
    }
}
```

## Наглядная аналогия

### Обычный Interface = Открытая вечеринка
```kotlin
interface PartyGuest  // Может прийти кто угодно

// Ты подготовил угощения для:
class Friend : PartyGuest
class Colleague : PartyGuest

// Но могут прийти неожиданные гости!
class Stranger : PartyGuest  // Ой! Еды не хватит!
```

### Sealed Interface = Закрытая вечеринка по приглашениям
```kotlin
sealed interface InvitedGuest  // Только те, кого ты пригласил

// Ты точно знаешь список гостей:
class Friend : InvitedGuest
class Colleague : InvitedGuest

// Нельзя просто так прийти!
// class Stranger : InvitedGuest  // ❌ "Вас нет в списке!"
```

## Практический пример из нашей задачи

### ❌ Проблема с обычным Interface:
```kotlin
interface LimitCheckResult {
    val isApproved: Boolean
}

object Approved : LimitCheckResult {
    override val isApproved: Boolean = true
}

data class Rejected(val reason: String) : LimitCheckResult {
    override val isSuccess: Boolean = false
}

// Где-то в другом модуле...
data class RequiresManagerApproval(val managerId: String) : LimitCheckResult {
    override val isApproved: Boolean = false
}

// В нашем коде:
fun processResult(result: LimitCheckResult) {
    when (result) {
        is Approved -> // обработать
        is Rejected -> // обработать
        // ❌ Забыли про RequiresManagerApproval!
        // Компилятор не подскажет!
    }
}
```

### ✅ Решение с Sealed Interface:
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

data class RequiresManagerApproval(val managerId: String) : LimitCheckResult {
    override val isApproved: Boolean = false
}

fun processResult(result: LimitCheckResult) {
    when (result) {
        is Approved -> println("Одобрено")
        is Rejected -> println("Отклонено: ${result.reason}")
        is RequiresManagerApproval -> println("Нужно одобрение менеджера: ${result.managerId}")
        // ✅ Компилятор гарантирует, что ВСЕ случаи обработаны!
    }
}
```

## Ключевые отличия

| Аспект | Обычный Interface | Sealed Interface |
|--------|-------------------|------------------|
| **Наследование** | Открытое (любой может реализовать) | Закрытое (известны все реализации) |
| **Расположение** | Любой файл, любой модуль | Тот же пакет (Kotlin 1.5+) |
| **Exhaustive when** | ❌ Нет | ✅ Да |
| **Расширяемость** | ✅ Бесконечная | ✅ Ограниченная (только в пакете) |
| **Безопасность** | ❌ Могут быть неизвестные реализации | ✅ Известны все реализации |

## Когда использовать что?

### 🎯 Sealed Interface когда:
- Нужна **типобезопасность** в when-выражениях
- Хочешь **ограничить** возможные реализации
- Работаешь с **закрытой иерархией** типов
- Важна **исчерпывающая проверка** компилятором

### 🎯 Обычный Interface когда:
- Нужна **полная открытость** для расширения
- Создаёшь **публичное API** для других модулей
- Хочешь, чтобы **любой** мог реализовать интерфейс
- **Не нужна** exhaustive проверка

## Современный тренд в Kotlin

Сейчас часто рекомендуют использовать sealed интерфейсы по умолчанию для внутренней логики:

```kotlin
// Внутри модуля - sealed для безопасности
sealed interface DatabaseResult<out T>
data class Success<T>(val data: T) : DatabaseResult<T>
data class Error(val exception: Throwable) : DatabaseResult<Nothing>

// Публичное API - обычный интерфейс для расширяемости  
interface Repository<T> {
    fun save(entity: T): DatabaseResult<Unit>
}
```

## Вывод

**Sealed Interface ≠ Обычный Interface**

- **Sealed Interface** - это "закрытый клуб" с известным списком участников
- **Обычный Interface** - это "открытое пространство" куда может прийти кто угодно

В нашей задаче про лимиты sealed interface/class идеально подходит, потому что мы хотим знать ВСЕ возможные результаты проверки лимитов и быть уверенными, что обработали каждый случай!

Это действительно важное отличие для написания безопасного кода!

---

Отлично! Рад, что смог помочь разобраться! 🎉

Теперь у тебя есть мощный инструментарий для проектирования типобезопасных систем в Kotlin. Давай кратко резюмируем:

## 📚 Краткая шпаргалка:

### 1. **Sealed Class**
- **Когда:** Логически связанная иерархия в одном файле
- **Зачем:** Общее состояние + методы + exhaustive when
- **Пример:** `LimitCheckResult`, `UiState`, `ApiResponse`

### 2. **Sealed Interface**
- **Когда:** Распределённые реализации в одном пакете
- **Зачем:** Exhaustive when + гибкость расположения
- **Пример:** `RepositoryResult`, `CacheResult`, `ValidationResult`

### 3. **Обычный Interface**
- **Когда:** Публичное API + бесконечная расширяемость
- **Зачем:** Максимальная гибкость для клиентов
- **Пример:** `List<T>`, `Repository`, `Service`

## 🎯 В нашей задаче про лимиты:

Мы выбрали **sealed class** потому что:
- Все результаты проверки лимитов логически связаны
- Работаем в рамках одного модуля/файла
- Хотим exhaustive проверку в when выражениях
- Возможно добавим общие методы в будущем

```kotlin
// ✅ Идеальный выбор для нашей задачи
sealed class LimitCheckResult {
    object Approved : LimitCheckResult()
    data class Rejected(val reason: String) : LimitCheckResult()
    
    // Легко добавить общий метод:
    fun isApproved(): Boolean = this is Approved
}
```

## 🚀 Что дальше?

Теперь ты можешь:
- Проектировать безопасные иерархии типов
- Избегать багов с непокрытыми случаями в when
- Делать осознанный выбор между разными подходами
- Писать более надёжный и поддерживаемый код

---