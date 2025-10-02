Давай разберём `sealed class` на разнообразных примерах. Это действительно мощный инструмент!

## 1. Sealed Class с общим состоянием

**"Общее состояние"** - это поля/свойства, которые есть у ВСЕХ наследников:

```kotlin
// ✅ Все результаты имеют временную метку
sealed class ApiResponse(val timestamp: Long = System.currentTimeMillis()) {
    data class Success(val data: String) : ApiResponse()
    data class Error(val message: String, val code: Int) : ApiResponse()
    object Loading : ApiResponse()
    
    // Все наследники автоматически получают timestamp!
}

// Использование
fun handleResponse(response: ApiResponse) {
    println("Ответ получен в: ${response.timestamp}") // Доступно у всех!
    
    when (response) {
        is ApiResponse.Success -> println("Данные: ${response.data}")
        is ApiResponse.Error -> println("Ошибка: ${response.message}")
        is ApiResponse.Loading -> println("Загрузка...")
    }
}
```

## 2. Sealed Class с методами

```kotlin
// ✅ Sealed class с методами реализации
sealed class PaymentMethod(val fee: BigDecimal) {
    abstract val maxAmount: BigDecimal
    
    // Общий метод для всех платежей
    fun calculateTotal(amount: BigDecimal): BigDecimal {
        return amount + (amount * fee / BigDecimal(100))
    }
    
    // Разные реализации
    object CreditCard : PaymentMethod(fee = BigDecimal("2.5")) {
        override val maxAmount: BigDecimal = BigDecimal("100000")
    }
    
    object BankTransfer : PaymentMethod(fee = BigDecimal("0.5")) {
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
    if (!method.canProcess(amount)) {
        println("Превышен лимит для ${method::class.simpleName}")
        return
    }
    
    val total = method.calculateTotal(amount)
    println("К оплате: $total (включая комиссию ${method.fee}%)")
}
```

## 3. UI State Management (очень популярный кейс)

```kotlin
// ✅ Идеально для состояния UI
sealed class ScreenState<out T> {
    object Loading : ScreenState<Nothing>()
    data class Success<T>(val data: T) : ScreenState<T>()
    data class Error(val message: String, val retryAction: () -> Unit) : ScreenState<Nothing>()
    object Empty : ScreenState<Nothing>()
    
    // Полезные методы
    fun isLoading(): Boolean = this is Loading
    fun getDataOrNull(): T? = (this as? Success)?.data
    fun getErrorOrNull(): String? = (this as? Error)?.message
    
    // Transform метод
    fun <R> map(transform: (T) -> R): ScreenState<R> = when (this) {
        is Loading -> Loading
        is Success -> Success(transform(data))
        is Error -> Error(message, retryAction)
        is Empty -> Empty
    }
}

// Использование в Android/Compose
fun UserProfileScreen(state: ScreenState<User>) {
    when (state) {
        is ScreenState.Loading -> ShowSpinner()
        is ScreenState.Success -> ShowUserProfile(user = state.data)
        is ScreenState.Error -> ShowError(
            message = state.message, 
            onRetry = state.retryAction
        )
        is ScreenState.Empty -> ShowEmptyState()
    }
}
```

## 4. Game Development Example

```kotlin
// ✅ Игровая логика
sealed class GameEntity(
    val health: Int,
    val position: Position
) {
    abstract val speed: Int
    
    // Общий метод для всех сущностей
    fun move(direction: Direction) {
        // Логика движения...
        println("$this движется $direction со скоростью $speed")
    }
    
    data class Player(
        val name: String,
        val inventory: List<Item>,
        override val speed: Int = 5
    ) : GameEntity(health = 100, position = Position(0, 0))
    
    data class Enemy(
        val type: EnemyType,
        val damage: Int,
        override val speed: Int = 3
    ) : GameEntity(health = 50, position = Position(10, 10))
    
    data class NPC(
        val dialog: String,
        val quest: Quest?,
        override val speed: Int = 2
    ) : GameEntity(health = 25, position = Position(5, 5))
    
    // Метод для обработки столкновений
    fun handleCollision(other: GameEntity) {
        when (this) {
            is Player -> when (other) {
                is Enemy -> takeDamage(other.damage)
                is NPC -> startDialog(other.dialog)
            }
            is Enemy -> when (other) {
                is Player -> attack(other)
                else -> {/* ничего */}
            }
            is NPC -> {/* NPC не реагирует */}
        }
    }
}
```

## 5. File System Example

```kotlin
// ✅ Файловая система
sealed class FileSystemNode(
    val name: String,
    val created: LocalDateTime,
    open val size: Long
) {
    abstract val permissions: String
    
    // Общие методы
    fun getPath(): String = name // упрощённо
    fun isOlderThan(days: Int): Boolean {
        return created.isBefore(LocalDateTime.now().minusDays(days.toLong()))
    }
    
    data class File(
        val content: ByteArray,
        val extension: String,
        override val permissions: String = "rw-r--r--"
    ) : FileSystemNode(
        name = "file.$extension",
        created = LocalDateTime.now(),
        size = content.size.toLong()
    ) {
        // File-specific methods
        fun readContent(): String = String(content)
    }
    
    data class Directory(
        val children: List<FileSystemNode> = emptyList(),
        override val permissions: String = "rwxr-xr-x"
    ) : FileSystemNode(
        name = "directory",
        created = LocalDateTime.now(),
        size = children.sumOf { it.size }
    ) {
        // Directory-specific methods
        fun findFile(filename: String): File? {
            return children.filterIsInstance<File>().find { it.name == filename }
        }
    }
    
    data class SymLink(
        val target: FileSystemNode,
        override val permissions: String = "rwxrwxrwx"
    ) : FileSystemNode(
        name = "link_to_${target.name}",
        created = LocalDateTime.now(),
        size = 0
    )
}

// Использование
fun analyzeFileSystem(node: FileSystemNode) {
    println("Анализ: ${node.name}")
    println("Размер: ${node.size} bytes")
    println("Права: ${node.permissions}")
    println("Создан: ${node.created}")
    
    when (node) {
        is FileSystemNode.File -> {
            println("Тип: Файл (.${node.extension})")
            println("Контент: ${node.readContent().take(50)}...")
        }
        is FileSystemNode.Directory -> {
            println("Тип: Директория")
            println("Количество файлов: ${node.children.size}")
        }
        is FileSystemNode.SymLink -> {
            println("Тип: Ссылка")
            println("Цель: ${node.target.name}")
        }
    }
}
```

## 6. Expression Calculator

```kotlin
// ✅ Математические выражения
sealed class MathExpression {
    abstract fun evaluate(): Double
    abstract fun simplify(): MathExpression
    
    data class Number(val value: Double) : MathExpression() {
        override fun evaluate(): Double = value
        override fun simplify(): MathExpression = this
    }
    
    data class Add(val left: MathExpression, val right: MathExpression) : MathExpression() {
        override fun evaluate(): Double = left.evaluate() + right.evaluate()
        override fun simplify(): MathExpression = when {
            left is Number && left.value == 0.0 -> right.simplify()
            right is Number && right.value == 0.0 -> left.simplify()
            else -> Add(left.simplify(), right.simplify())
        }
    }
    
    data class Multiply(val left: MathExpression, val right: MathExpression) : MathExpression() {
        override fun evaluate(): Double = left.evaluate() * right.evaluate()
        override fun simplify(): MathExpression = when {
            left is Number && left.value == 1.0 -> right.simplify()
            right is Number && right.value == 1.0 -> left.simplify()
            left is Number && left.value == 0.0 -> Number(0.0)
            right is Number && right.value == 0.0 -> Number(0.0)
            else -> Multiply(left.simplify(), right.simplify())
        }
    }
    
    // Общий метод для всех выражений
    fun prettyPrint(): String = when (this) {
        is Number -> value.toString()
        is Add -> "(${left.prettyPrint()} + ${right.prettyPrint()})"
        is Multiply -> "(${left.prettyPrint()} * ${right.prettyPrint()})"
    }
}

// Использование
fun main() {
    val expression = MathExpression.Add(
        MathExpression.Number(5.0),
        MathExpression.Multiply(
            MathExpression.Number(2.0),
            MathExpression.Number(3.0)
        )
    )
    
    println("Выражение: ${expression.prettyPrint()}") // (5.0 + (2.0 * 3.0))
    println("Результат: ${expression.evaluate()}")    // 11.0
    println("Упрощённое: ${expression.simplify().prettyPrint()}") // (5.0 + 6.0)
}
```

## Ключевые преимущества Sealed Class:

1. **Общее состояние** - все наследники наследуют поля родителя
2. **Общие методы** - можно добавить реализацию в sealed class
3. **Type-safe** - компилятор знает все возможные варианты
4. **Extensible** - легко добавлять новую логику в when выражениях
5. **Polymorphic** - можно использовать ООП принципы

Он идеально подходит для моделирования ограниченных иерархий с общей логикой!

---

