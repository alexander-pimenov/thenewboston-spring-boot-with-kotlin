Давай разберём generics с variance (`out`, `in`) подробно с примерами. Это одна из самых мощных фич Kotlin!

## 📚 Основы Generics Variance

### 1. **`<out T>` - Ковариантность (Covariance)**

**"Производитель" - только отдаёт значения**

```kotlin
// ✅ МОЖНО использовать Producer<Child> там, где ожидается Producer<Parent>
class Box<out T>(private val value: T) {
    fun get(): T = value
    // ❌ НЕЛЬЗЯ: fun set(item: T) { ... } - T в параметре
}

open class Animal
class Cat : Animal()

fun main() {
    val catBox: Box<Cat> = Box(Cat())
    val animalBox: Box<Animal> = catBox // ✅ Работает благодаря 'out'
    
    val animal: Animal = animalBox.get() // Можно получить Animal
}
```

### 2. **`<in T>` - Контравариантность (Contravariance)**

**"Потребитель" - только принимает значения**

```kotlin
// ✅ МОЖНО использовать Consumer<Parent> там, где ожидается Consumer<Child>
class Consumer<in T> {
    fun consume(item: T) {
        println("Consuming: $item")
    }
    // ❌ НЕЛЬЗЯ: fun produce(): T { ... } - T в возвращаемом значении
}

fun main() {
    val animalConsumer: Consumer<Animal> = Consumer()
    val catConsumer: Consumer<Cat> = animalConsumer // ✅ Работает благодаря 'in'
    
    catConsumer.consume(Cat()) // Можно передать Cat
}
```

## 🎯 Подробный пример с `ScreenState<out T>`

```kotlin
// ✅ КОВАРИАНТНЫЙ sealed class - только производит T
sealed class ScreenState<out T> {
    object Loading : ScreenState<Nothing>()
    data class Success<out T>(val data: T) : ScreenState<T>()
    data class Error(val message: String) : ScreenState<Nothing>()
    
    // Можем безопасно читать T
    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    // Можем трансформировать T
    fun <R> map(transformer: (T) -> R): ScreenState<R> = when (this) {
        is Loading -> Loading
        is Success -> Success(transformer(data))
        is Error -> Error(message)
    }
}

// Иерархия данных
open class User(val name: String)
class PremiumUser(name: String, val subscription: String) : User(name)

// Использование ковариантности
fun main() {
    val premiumState: ScreenState<PremiumUser> = 
        ScreenState.Success(PremiumUser("Alice", "Gold"))
    
    // ✅ Благодаря 'out T' можем присвоить ScreenState<PremiumUser> к ScreenState<User>
    val userState: ScreenState<User> = premiumState
    
    processScreenState(userState)
    
    // Пример с трансформацией
    val nameState: ScreenState<String> = premiumState.map { it.name }
    println("Name state: $nameState")
}

fun processScreenState(state: ScreenState<User>) {
    when (state) {
        is ScreenState.Loading -> println("Loading user...")
        is ScreenState.Success -> println("User loaded: ${state.data.name}")
        is ScreenState.Error -> println("Error: ${state.message}")
    }
}
```

## 🔄 Пример с `in T` (Consumer)

```kotlin
// ✅ КОНТРАВАРИАНТНЫЙ интерфейс - только потребляет T
interface JsonSerializer<in T> {
    fun serialize(item: T): String
    // ❌ Не может возвращать T!
}

// Иерархия классов
open class Vehicle(val brand: String)
class Car(brand: String, val doors: Int) : Vehicle(brand)
class Bike(brand: String, val type: String) : Vehicle(brand)

// Реализации сериализаторов
class VehicleSerializer : JsonSerializer<Vehicle> {
    override fun serialize(item: Vehicle): String = 
        """{"brand": "${item.brand}"}"""
}

class CarSerializer : JsonSerializer<Car> {
    override fun serialize(item: Car): String = 
        """{"brand": "${item.brand}", "doors": ${item.doors}}"""
}

fun main() {
    val car = Car("Toyota", 4)
    val bike = Bike("Yamaha", "Sport")
    
    // VehicleSerializer может сериализовать ЛЮБОЙ Vehicle
    val vehicleSerializer: JsonSerializer<Vehicle> = VehicleSerializer()
    
    // ✅ Благодаря 'in T' можем использовать VehicleSerializer как JsonSerializer<Car>
    val carSerializer: JsonSerializer<Car> = vehicleSerializer
    
    println(carSerializer.serialize(car)) // Работает!
    
    // Пример с коллекцией сериализаторов
    val serializers: List<JsonSerializer<Car>> = listOf(
        VehicleSerializer(), // ✅ VehicleSerializer подходит для Car
        CarSerializer()      // ✅ CarSerializer тоже подходит
    )
    
    serializers.forEach { serializer ->
        println(serializer.serialize(car))
    }
}
```

## 📊 Сравнение `out` vs `in`

| Аспект | `out T` (Ковариантность) | `in T` (Контравариантность) |
|--------|--------------------------|-----------------------------|
| **Роль** | Producer (производитель) | Consumer (потребитель) |
| **Использование** | Только в возвращаемых значениях | Только в параметрах |
| **Назначение** | `Box<Child>` → `Box<Parent>` | `Consumer<Parent>` → `Consumer<Child>` |
| **PECS** | Producer → Extends | Consumer → Super |
| **Примеры** | `List<out T>`, `ScreenState<out T>` | `Comparable<in T>`, `JsonSerializer<in T>` |

## 🛠️ Использование в функциях

### Declaration-site variance (объявление на месте):

```kotlin
// ✅ Объявляем variance при объявлении класса
interface ReadOnlyList<out T> {
    fun get(index: Int): T
    // fun add(item: T) // ❌ Нельзя - T в параметре
}

interface WriteOnlyList<in T> {
    fun add(item: T)
    // fun get(index: Int): T // ❌ Нельзя - T в возвращаемом значении
}
```

### Use-site variance (использование на месте):

```kotlin
// ✅ Используем variance при использовании типа
fun copyData(source: MutableList<out Animal>, destination: MutableList<in Animal>) {
    for (animal in source) {
        destination.add(animal) // ✅ Безопасно
    }
}

fun main() {
    val cats: MutableList<Cat> = mutableListOf(Cat(), Cat())
    val animals: MutableList<Animal> = mutableListOf()
    
    copyData(cats, animals) // ✅ Работает благодаря use-site variance
    println("Copied ${animals.size} animals")
}
```

## ☕ Как это делается в Java?

### Java использует Wildcards:

```java
// Kotlin:
class Box<out T> { fun get(): T }

// Java эквивалент:
class Box<T> { 
    T get() { ... }
    // ❌ Не может иметь методов с T в параметрах
}

// Use-site в Java:
void processList(List<? extends Animal> animals) { // ~ out Animal
    Animal animal = animals.get(0); // ✅ Можно читать
    // animals.add(new Cat()); // ❌ Нельзя добавлять
}

void addAnimals(List<? super Animal> animals) { // ~ in Animal
    animals.add(new Cat()); // ✅ Можно добавлять
    // Animal animal = animals.get(0); // ❌ Нельзя читать как Animal
}
```

### Сравнение Kotlin vs Java:

| Kotlin | Java | Описание |
|--------|------|----------|
| `List<out T>` | `List<? extends T>` | Ковариантность |
| `MutableList<in T>` | `MutableList<? super T>` | Контравариантность |
| `List<T>` | `List<T>` | Инвариантность |
| Declaration-site | Use-site only | Место объявления variance |

## 🎯 Real-world примеры

### 1. **Repository Pattern с `out T`**:

```kotlin
interface Repository<out T> {
    fun getAll(): List<T>
    fun getById(id: String): T?
    // ❌ Не может иметь методов с T в параметрах
}

class UserRepository : Repository<User> {
    override fun getAll(): List<User> = listOf(User("Alice"), User("Bob"))
    override fun getById(id: String): User? = User("Alice")
}

fun processUserRepository(repo: Repository<User>) {
    val users = repo.getAll() // Безопасное чтение
}

// ✅ Можем передать любой репозиторий, который производит User или его подтипы
val premiumRepo: Repository<PremiumUser> = PremiumUserRepository()
processUserRepository(premiumRepo) // Работает благодаря 'out'
```

### 2. **Event Handler с `in T`**:

```kotlin
interface EventHandler<in T> {
    fun handle(event: T)
}

class UIEvent
class ButtonClickEvent : UIEvent()
class FormSubmitEvent : UIEvent()

class GenericEventHandler : EventHandler<UIEvent> {
    override fun handle(event: UIEvent) {
        println("Handling UI event: $event")
    }
}

// ✅ Можем использовать общий обработчик для конкретных событий
val buttonHandler: EventHandler<ButtonClickEvent> = GenericEventHandler()
buttonHandler.handle(ButtonClickEvent()) // Безопасно
```

## 🚀 Практические правила

### Используй `out T` когда:
- Класс только **производит** значения типа T
- T используется только в **возвращаемых** значениях
- Примеры: `List`, `Repository`, `Factory`

### Используй `in T` когда:
- Класс только **потребляет** значения типа T
- T используется только в **параметрах**
- Примеры: `Comparator`, `Consumer`, `EventHandler`

### Без variance когда:
- Класс и производит и потребляет T
- Нужна полная типобезопасность

## 📝 Key Takeaways:

1. **`out T`** = безопасное чтение, присваивание подтипов
2. **`in T`** = безопасная запись, присваивание супертипов
3. **Kotlin** имеет declaration-site variance (более удобно)
4. **Java** имеет только use-site variance (менее удобно)

Эта мощная фича делает систему типов Kotlin гораздо более выразительной и безопасной! 🎉

---
