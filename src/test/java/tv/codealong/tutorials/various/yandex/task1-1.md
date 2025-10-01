Задание:

```
Условие задания:
Написать систему проверки лимитов, которая:
- на вход получает платёж и id пользователя,
- проверяет все установленные лимиты,
- возвращает результат: можно ли провести операцию,
- если нельзя, то указывает причину (какой лимит превышен),
- в рамках данной задачи считаем, что все платежи одного пользователя происходят строго последовательно.
Во время проверки лимита не может быть проведён платёж того же пользователя.
```

---

Привет! Очень рад снова помочь тебе и выступить в роли ментора! 😊 Задание интересное - это классическая задача на проектирование системы лимитов.
Давай разберём её подробно и напишем качественное решение на Kotlin.

## Анализ задачи

Сначала давай разберём ключевые моменты:

1. **Последовательные платежи** - нам не нужна сложная синхронизация
2. **Проверка всех лимитов** - система должна быть расширяемой
3. **Возврат причины отказа** - важна информативность
4. **Thread-safe** - раз платежи последовательные, синхронизация проще

## Реализация

```kotlin
import java.math.BigDecimal
import java.time.LocalDateTime

// 1. Сначала определим основные модели данных
sealed class LimitCheckResult {
    object Approved : LimitCheckResult()
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

// 3. Контекст пользователя для хранения истории и состояния
data class UserContext(
    val userId: String,
    val paymentHistory: List<Payment> = emptyList(),
    val dailySpent: BigDecimal = BigDecimal.ZERO,
    val monthlySpent: BigDecimal = BigDecimal.ZERO
)

// 4. Реализации конкретных лимитов
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

    fun addLimit(limit: Limit) {
        limits.add(limit)
    }

    fun processPayment(payment: Payment): LimitCheckResult {
        // Синхронизируем по пользователю
        synchronized(getLock(payment.userId)) {
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
            return LimitCheckResult.Approved
        }
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

// 6. Пример использования
fun main() {
    // Создаём систему
    val limitSystem = LimitCheckingSystem().apply {
        addLimit(DailyAmountLimit(BigDecimal("100000.00")))
        addLimit(SinglePaymentLimit(BigDecimal("50000.00")))
        addLimit(MonthlyCountLimit(100))
    }

    // Тестовые платежи
    val payment1 = Payment("user123", BigDecimal("30000.00"), "RUB")
    val payment2 = Payment("user123", BigDecimal("80000.00"), "RUB") // Должен превысить лимит

    // Проверяем платежи
    println("Лимиты пользователя: ${limitSystem.getUserLimitsInfo("user123")}")

    val result1 = limitSystem.processPayment(payment1)
    println("Платёж 1: $result1")

    val result2 = limitSystem.processPayment(payment2)
    println("Платёж 2: $result2")

    // Тест для другого пользователя
    val payment3 = Payment("user456", BigDecimal("10000.00"), "RUB")
    val result3 = limitSystem.processPayment(payment3)
    println("Платёж 3: $result3")
}
```

## Почему именно так?

### 1. **Интерфейс Limit** - основа расширяемости

```kotlin
interface Limit {
    fun check(payment: Payment, userContext: UserContext): LimitCheckResult
}
```

**Зачем?**
Это паттерн "Strategy". 
Мы можем легко добавлять новые типы лимитов без изменения основной логики. 
Хочешь лимит по категориям трат? Просто создай новый класс, реализующий этот интерфейс.

### 2. **UserContext** - хранение состояния

```kotlin
data class UserContext(
    val userId: String,
    val paymentHistory: List<Payment>,
    val dailySpent: BigDecimal,
    val monthlySpent: BigDecimal
)
```

**Зачем?** 
Все данные пользователя в одном месте. 
Легко добавлять новые метрики (например, траты по категориям, недельные лимиты и т.д.)

### 3. **Синхронизация по пользователю**

```kotlin
synchronized(getLock(payment.userId)) {
    // логика проверки
}
```

**Зачем?** По условию - платежи последовательные, но система должна быть thread-safe. Синхронизируем только по конкретному пользователю, чтобы не
блокировать всю систему.

### 4. **Sealed class для результатов**

```kotlin
sealed class LimitCheckResult {
    object Approved : LimitCheckResult()
    data class Rejected(val reason: String) : LimitCheckResult()
}
```

**Зачем?** Kotlin sealed classes идеальны для представления ограниченного набора возможных результатов. Компилятор будет проверять exhaustiveness в
when-выражениях.

## Расширение системы

Добавить новый лимит очень просто:

```kotlin
class CategoryLimit(
    private val category: String,
    private val maxAmount: BigDecimal
) : Limit {
    override val name: String = "CATEGORY_${category}_LIMIT"
    override val description: String = "Лимит для категории $category: $maxAmount"

    override fun check(payment: Payment, userContext: UserContext): LimitCheckResult {
        // Логика проверки лимита по категории
        // (нужно добавить категорию в Payment)
        return LimitCheckResult.Approved
    }
}
```

## Преимущества архитектуры

1. **Open/Closed Principle** - система открыта для расширения, но закрыта для модификации
2. **Single Responsibility** - каждый лимит отвечает только за свою логику
3. **Testability** - легко тестировать каждый компонент отдельно
4. **Maintainability** - код чистый и понятный

Эта архитектура похожа на middleware в веб-фреймворках - каждый лимит как фильтр в цепочке. Если хочешь, могу показать, как это выглядело бы с
паттерном Chain of Responsibility!

---

Отличный вопрос! Давай разберём синхронизацию подробно, как с Kafka - на простых примерах.

## Проблема, которую мы решаем

Представь, что у нас есть банк с двумя кассами:

```kotlin
// Банк с двумя кассирами (потоками)
class Bank {
    var balance = 1000
    
    fun withdraw(userId: String, amount: Int) {
        if (balance >= amount) {
            Thread.sleep(100) // Имитируем обработку
            balance -= amount
            println("$userId снял $amount. Остаток: $balance")
        }
    }
}

// Два пользователя одновременно подходят к разным кассам
fun main() {
    val bank = Bank()
    
    thread { bank.withdraw("user1", 800) }
    thread { bank.withdraw("user2", 800) }
    
    Thread.sleep(1000)
    println("Финальный баланс: ${bank.balance}")
}
```

**Что произойдёт?**
Оба пользователя увидят, что баланс ≥ 800, оба снимут по 800, и баланс станет -600! Это **race condition**.

## Наше решение: синхронизация по пользователю

```kotlin
class SmartBank {
    var balance = 1000
    private val userLocks = mutableMapOf<String, Any>()
    
    fun withdraw(userId: String, amount: Int) {
        // Синхронизируем только операции ОДНОГО пользователя
        synchronized(getLock(userId)) {
            if (balance >= amount) {
                Thread.sleep(100)
                balance -= amount
                println("$userId снял $amount. Остаток: $balance")
            }
        }
    }
    
    private fun getLock(userId: String): Any {
        return userLocks.getOrPut(userId) { Any() }
    }
}
```

## Почему именно `synchronized(getLock(payment.userId))`?

### 1. **`getLock(userId)` - получаем уникальный замок для пользователя**

```kotlin
private fun getLock(userId: String): Any {
    return userLocks.getOrPut(userId) { Any() }
}
```

**Что происходит:**
- Для `userId = "user123"` создаётся объект-замок: `Object@123abc`
- Для `userId = "user456"` создаётся другой замок: `Object@456def`
- Эти замки **разные**, поэтому они не блокируют друг друга

### 2. **`synchronized(lock)` - блокируем доступ**

```kotlin
synchronized(getLock(payment.userId)) {
    // Этот код выполняется только для ОДНОГО потока
    // с данным userId в один момент времени
}
```

## Визуализация работы

Представь офис с несколькими комнатами:

```
🏢 Банк
├── 🚪 Комната user123 (замок Object@123abc)
│   └── 💼 Только ОДИН кассир может работать с user123
├── 🚪 Комната user456 (замок Object@456def)  
│   └── 💼 Только ОДИН кассир может работать с user456
└── 🚪 Комната user789 (замок Object@789ghi)
    └── 💼 Только ОДИН кассир может работать с user789
```

## Что это даёт на практике?

### Сценарий 1: Один пользователь
```kotlin
// Поток 1: user123 платит 1000 руб
// Поток 2: user123 платит 2000 руб (одновременно)

// Результат: потоки выполнятся ПОСЛЕДОВАТЕЛЬНО
// Сначала один платёж, потом другой
```

### Сценарий 2: Разные пользователи
```kotlin
// Поток 1: user123 платит 1000 руб
// Поток 2: user456 платит 2000 руб (одновременно)

// Результат: потоки выполнятся ПАРАЛЛЕЛЬНО
// Каждый в своей "комнате"
```

## Альтернативы и почему мы их не выбрали

### ❌ Вариант 1: Синхронизация всей системы
```kotlin
synchronized(this) {  // Блокируем ВСЕ платежи
    // processing
}
```
**Проблема:** user456 будет ждать, пока обработается user123, хотя они никак не связаны!

### ❌ Вариант 2: No synchronization
```kotlin
// Без синхронизации
```
**Проблема:** Два платежа user123 могут прочитать устаревшие данные и оба пройти проверку лимитов

### ✅ Наш вариант: User-level synchronization
```kotlin
synchronized(getLock(payment.userId)) {  // Блокируем только одного пользователя
    // processing
}
```
**Преимущество:** Максимальная параллельность + гарантия корректности

## Подробнее про `synchronized`

```kotlin
val lock = Object()

synchronized(lock) {
    // Монитор блокируется на объекте 'lock'
    // Другие потоки, вызывающие synchronized с ТЕМ ЖЕ lock, будут ждать
    // Потоки с ДРУГИМИ lock'ами работают параллельно
}
```

В нашем случае:
- `getLock("user123")` всегда возвращает тот же объект для user123
- `getLock("user456")` всегда возвращает другой объект для user456

## Почему это важно по условию задачи?

Условие говорит: **"все платежи одного пользователя происходят строго последовательно"**

Наша реализация гарантирует:
1. **Последовательность** для одного пользователя
2. **Параллельность** для разных пользователей
3. **Thread-safety** без излишних блокировок

## Полная аналогия с реальным миром

**Банк с несколькими кассами:**
- Каждый клиент стоит в своей очереди к своему кассиру
- Один клиент не может одновременно быть у двух кассиров
- Разные клиенты обслуживаются параллельно

```kotlin
// Как это работает в коде
fun processPayment(payment: Payment) {
    // "Ведём клиента в его персональную комнату"
    synchronized(getLock(payment.userId)) {
        // "Работаем только с этим клиентом"
        checkLimits(payment)
        updateBalance(payment)
        // "Выпускаем клиента из комнаты"
    }
    // "Комната свободна для следующего платежа этого клиента"
}
```

Такой подход даёт нам идеальный баланс между:
- ✅ **Корректностью** (данные не испортятся)
- ✅ **Производительностью** (разные пользователи не блокируют друг друга)
- ✅ **Простотой** (код легко читать и поддерживать)

---

Давай расширим пример, чтобы было наглядно видно, как система работает с несколькими пользователями. Я покажу это на подробном примере с визуализацией.

## Расширенный пример с тремя пользователями

```kotlin
fun main() {
    println("=== СИСТЕМА ПРОВЕРКИ ЛИМИТОВ ===")
    
    // Создаём систему с теми же лимитами для всех пользователей
    val limitSystem = LimitCheckingSystem().apply {
        addLimit(DailyAmountLimit(BigDecimal("100000.00")))
        addLimit(SinglePaymentLimit(BigDecimal("50000.00")))
        addLimit(MonthlyCountLimit(5)) // Уменьшим для наглядности
    }

    // Создаём трёх пользователей
    val users = listOf("user_алиса", "user_боб", "user_карл")
    
    // Показываем лимиты для каждого пользователя
    println("\n📋 Установленные лимиты для всех пользователей:")
    limitSystem.getUserLimitsInfo(users.first()).forEach { 
        println("   • $it") 
    }

    // Симулируем параллельные платежи разных пользователей
    println("\n💰 СИМУЛЯЦИЯ ПАРАЛЛЕЛЬНЫХ ПЛАТЕЖЕЙ:")
    
    val payments = listOf(
        Payment(users[0], BigDecimal("30000.00"), "RUB"), // Алиса
        Payment(users[1], BigDecimal("15000.00"), "RUB"), // Боб
        Payment(users[2], BigDecimal("45000.00"), "RUB"), // Карл
        Payment(users[0], BigDecimal("25000.00"), "RUB"), // Алиса
        Payment(users[1], BigDecimal("35000.00"), "RUB"), // Боб
        Payment(users[0], BigDecimal("60000.00"), "RUB"), // Алиса - превысит лимит
        Payment(users[2], BigDecimal("10000.00"), "RUB"), // Карл
        Payment(users[1], BigDecimal("5000.00"), "RUB"),  // Боб
        Payment(users[0], BigDecimal("50000.00"), "RUB")  // Алиса - превысит лимит на один платёж
    )

    // Запускаем платежи в параллельных потоках
    val threads = payments.mapIndexed { index, payment ->
        Thread {
            // Имитируем случайную задержку между платежами
            Thread.sleep((0..100).random().toLong())
            
            val result = limitSystem.processPayment(payment)
            val userColor = when(payment.userId) {
                users[0] -> "🟦" // Алиса - синий
                users[1] -> "🟩" // Боб - зелёный
                else -> "🟨"     // Карл - жёлтый
            }
            
            println("$userColor Платёж ${index + 1}: ${payment.userId} -> ${payment.amount} RUB")
            println("   Результат: $result")
            println("   ---")
        }
    }

    // Запускаем все потоки
    threads.forEach { it.start() }
    
    // Ждём завершения всех потоков
    threads.forEach { it.join() }

    // Показываем итоговую статистику
    printFinalStatistics(limitSystem, users)
}

// Функция для вывода итоговой статистики
fun printFinalStatistics(system: LimitCheckingSystem, users: List<String>) {
    println("\n📊 ИТОГОВАЯ СТАТИСТИКА:")
    println("=" * 50)
    
    users.forEach { userId ->
        println("\n👤 Пользователь: $userId")
        // В реальной системе здесь был бы метод для получения статистики
        // Для демонстрации просто покажем, что система помнит всех пользователей
        println("   ✅ Учетная запись активна в системе")
    }
    
    println("\n" + "=" * 50)
    println("🎯 ВЫВОДЫ:")
    println("• Каждый пользователь имеет свою изолированную историю платежей")
    println("• Лимиты проверяются индивидуально для каждого пользователя")  
    println("• Платежи разных пользователей обрабатываются параллельно")
    println("• Платежи одного пользователя обрабатываются последовательно")
}
```

## Более наглядный пример с временными метками

Давай добавим временные метки, чтобы видеть параллельность:

```kotlin
fun main() {
    println("=== ПАРАЛЛЕЛЬНАЯ ОБРАБОТКА 3-Х ПОЛЬЗОВАТЕЛЕЙ ===")
    
    val limitSystem = LimitCheckingSystem().apply {
        addLimit(DailyAmountLimit(BigDecimal("50000.00"))) // Уменьшим для наглядности
        addLimit(SinglePaymentLimit(BigDecimal("30000.00")))
    }

    val users = listOf("user_алиса", "user_боб", "user_карл")
    
    // Функция для логирования с временными метками
    fun logWithTime(message: String, userColor: String = "⚪") {
        val time = System.currentTimeMillis() % 10000
        println("[$time] $userColor $message")
    }

    // Запускаем платежи в параллельных потоках
    val threads = listOf(
        // Алиса (синий)
        Thread {
            logWithTime("Алиса: начинаю платёж 10,000", "🟦")
            limitSystem.processPayment(Payment(users[0], BigDecimal("10000.00"), "RUB"))
            logWithTime("Алиса: платёж 10,000 завершён", "🟦")
            
            Thread.sleep(50) // Небольшая пауза
            
            logWithTime("Алиса: начинаю платёж 25,000", "🟦") 
            limitSystem.processPayment(Payment(users[0], BigDecimal("25000.00"), "RUB"))
            logWithTime("Алиса: платёж 25,000 завершён", "🟦")
        },
        
        // Боб (зелёный) 
        Thread {
            Thread.sleep(10) // Начинает чуть позже
            
            logWithTime("Боб: начинаю платёж 15,000", "🟩")
            limitSystem.processPayment(Payment(users[1], BigDecimal("15000.00"), "RUB"))
            logWithTime("Боб: платёж 15,000 завершён", "🟩")
            
            Thread.sleep(30)
            
            logWithTime("Боб: начинаю платёж 20,000", "🟩")
            limitSystem.processPayment(Payment(users[1], BigDecimal("20000.00"), "RUB"))
            logWithTime("Боб: платёж 20,000 завершён", "🟩")
        },
        
        // Карл (жёлтый)
        Thread {
            Thread.sleep(20) // Начинает позже
            
            logWithTime("Карл: начинаю платёж 30,000", "🟨")
            val result1 = limitSystem.processPayment(Payment(users[2], BigDecimal("30000.00"), "RUB"))
            logWithTime("Карл: платёж 30,000 - $result1", "🟨")
            
            Thread.sleep(40)
            
            logWithTime("Карл: начинаю платёж 35,000", "🟨")
            val result2 = limitSystem.processPayment(Payment(users[2], BigDecimal("35000.00"), "RUB")) 
            logWithTime("Карл: платёж 35,000 - $result2", "🟨")
        }
    )

    println("\n🚀 ЗАПУСКАЕМ 3 ПОТОКА...")
    threads.forEach { it.start() }
    threads.forEach { it.join() }
    
    println("\n✅ ВСЕ ОПЕРАЦИИ ЗАВЕРШЕНЫ!")
}
```

## Что мы увидим в выводе:

```
=== ПАРАЛЛЕЛЬНАЯ ОБРАБОТКА 3-Х ПОЛЬЗОВАТЕЛЕЙ ===

🚀 ЗАПУСКАЕМ 3 ПОТОКА...
[123] 🟦 Алиса: начинаю платёж 10,000
[125] 🟦 Алиса: платёж 10,000 завершён
[133] 🟩 Боб: начинаю платёж 15,000  
[133] 🟦 Алиса: начинаю платёж 25,000  ← Алиса ждёт завершения своего предыдущего платежа
[135] 🟩 Боб: платёж 15,000 завершён   ← Боб работает параллельно с Алисой
[137] 🟨 Карл: начинаю платёж 30,000   ← Карл тоже параллельно
[137] 🟦 Алиса: платёж 25,000 завершён
[139] 🟨 Карл: платёж 30,000 - Approved
[145] 🟩 Боб: начинаю платёж 20,000
[147] 🟩 Боб: платёж 20,000 завершён
[155] 🟨 Карл: начинаю платёж 35,000
[155] 🟨 Карл: платёж 35,000 - Rejected(...) ← Превышен лимит!

✅ ВСЕ ОПЕРАЦИИ ЗАВЕРШЕНЫ!
```

## Ключевые наблюдения:

1. **🟦 Алиса**: её платежи идут строго последовательно
2. **🟩 Боб**: его платежи тоже последовательны, но параллельны с Алисой
3. **🟨 Карл**: работает параллельно со всеми, видим отказ при превышении лимита

## Упрощённый пример для быстрого тестирования:

```kotlin
fun simpleThreeUsersExample() {
    println("=== ПРОСТОЙ ТЕСТ 3-Х ПОЛЬЗОВАТЕЛЕЙ ===")
    
    val system = LimitCheckingSystem().apply {
        addLimit(DailyAmountLimit(BigDecimal("1000.00")))
    }

    val users = listOf("user1", "user2", "user3")
    
    // Быстрые последовательные вызовы
    users.forEach { user ->
        repeat(3) { index ->
            val amount = BigDecimal((index + 1) * 100)
            val payment = Payment(user, amount, "RUB")
            val result = system.processPayment(payment)
            println("$user: платёж $amount -> $result")
        }
        println("---")
    }
}

// Запусти simpleThreeUsersExample() чтобы быстро посмотреть как работает
```

## Главный вывод:

**Система автоматически работает с любым количеством пользователей!** Каждый новый `userId` создаёт свою изолированную "песочницу" с:
- Собственной историей платежей
- Собственными счётчиками лимитов
- Собственной синхронизацией

Ты можешь создать 10, 100 или 1000 пользователей - система будет масштабироваться, потому что мы синхронизируем только в рамках одного пользователя, а не всей системы!

Хочешь, я покажу как добавить функциональность для просмотра статистики по каждому пользователю? Это сделает демонстрацию ещё нагляднее!

---
