package tv.codealong.tutorials.various.genericswithvarianceoutandin


//## 🚀 Практические правила
//
//### Используй `out T` когда:
//- Класс только **производит** значения типа T
//- T используется только в **возвращаемых** значениях
//- Примеры: `List`, `Repository`, `Factory`
//
//### Используй `in T` когда:
//- Класс только **потребляет** значения типа T
//- T используется только в **параметрах**
//- Примеры: `Comparator`, `Consumer`, `EventHandler`
//
//### Без variance когда:
//- Класс и производит и потребляет T
//- Нужна полная типобезопасность


/**
 * ### 1. **`<out T>` - Ковариантность (Covariance)**
 * **"Производитель" - только отдаёт значения**
 * ✅ МОЖНО использовать Producer<Child> там, где ожидается Producer<Parent>
 */
class Box<out T>(private val value: T) {
    fun get(): T = value
    // ❌ НЕЛЬЗЯ: fun set(item: T) { ... } - T в параметре
    // ❌ Не может иметь методов с T в параметрах
}

open class Animal
class Cat : Animal() {
    init {
        println("Cat created")
    }
}

class Dog : Animal() {
    init {
        println("Dog created")
    }
}

/**
 * ### 2. **`<in T>` - Контравариантность (Contravariance)**
 * **"Потребитель" - только принимает значения**
 * ✅ МОЖНО использовать Consumer<Parent> там, где ожидается Consumer<Child>
 */
class Consumer<in T> {
    // ❌ Не может возвращать T! Но может возвращать другой тип.
    fun consume(item: T) {
        println("Consuming: $item")
    }
    // ❌ НЕЛЬЗЯ: fun produce(): T { ... } - T в возвращаемом значении
}


// ✅ КОВАРИАНТНЫЙ sealed class - только производит T
sealed class ScreenState<out T> {
    data object Loading : ScreenState<Nothing>()
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

fun processScreenState(state: ScreenState<User>) {
    when (state) {
        is ScreenState.Loading -> println("Loading user...")
        is ScreenState.Success -> println("User loaded: ${state.data.name}")
        is ScreenState.Error -> println("Error: ${state.message}")
    }
}

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

class BikeSerializer : JsonSerializer<Bike> {
    override fun serialize(item: Bike): String =
        """{"brand": "${item.brand}", "type": "${item.type}"}"""
}

// ✅ Используем variance при использовании типа
fun copyData(source: MutableList<out Animal>, destination: MutableList<in Animal>) {
    for (animal in source) {
        destination.add(animal) // ✅ Безопасно
    }
    println("list destination: $destination")
}

fun processList(animals: MutableList<out Animal>) { // ~ out Animal
    val animal = animals[0] // ✅ Можно читать
    //animals.add(Cat()); // ❌ Нельзя добавлять
}

fun addAnimals(animals: MutableList<in Animal>) { // ~ in Animal
    animals.add(Cat()) // ✅ Можно добавлять
    //val animal: Animal = animals[0]; // ❌ Нельзя читать как Animal
}

//### 1. **Repository Pattern с `out T`**:
interface Repository<out T> {
    fun getAll(): List<T>
    fun getById(id: String): T?
    // ❌ Не может иметь методов с T в параметрах
}

//### 2. **Event Handler с `in T`**:
interface EventHandler<in T> {
    fun handle(event: T)
}

fun main() {
    val catBox: Box<Cat> = Box(Cat())
    val dogBox: Box<Dog> = Box(Dog())
    val animalBox1: Box<Animal> = catBox // ✅ Работает благодаря 'out'
    val animalBox2: Box<Animal> = dogBox // ✅ Работает благодаря 'out'

    val animal1: Animal = animalBox1.get() // Можно получить Animal
    val animal2: Animal = animalBox2.get() // Можно получить Animal
    println("animal1: $animal1, animal2: $animal2")
    // animal1: tv.codealong.tutorials.various.genericswithvarianceoutandin.Cat@7a7b0070,
    // animal2: tv.codealong.tutorials.various.genericswithvarianceoutandin.Dog@39a054a5

    val animalConsumer: Consumer<Animal> = Consumer()
    val catConsumer: Consumer<Cat> = animalConsumer // ✅ Работает благодаря 'in'

    catConsumer.consume(Cat()) // Можно передать Cat

    //---------------------------------------------------------
    val premiumState: ScreenState<PremiumUser> =
        ScreenState.Success(PremiumUser("Alice", "Gold"))

    // ✅ Благодаря 'out T' можем присвоить ScreenState<PremiumUser> к ScreenState<User>
    val userState: ScreenState<User> = premiumState

    processScreenState(userState)

    // Пример с трансформацией
    val nameState: ScreenState<String> = premiumState.map { it.name }
    println("Name state: $nameState")

    //---------------------------------------------------------

    val car = Car("Toyota", 4)
    val bike = Bike("Yamaha", "Sport")

    // VehicleSerializer может сериализовать ЛЮБОЙ Vehicle
    val vehicleSerializer: JsonSerializer<Vehicle> = VehicleSerializer()

    // ✅ Благодаря 'in T' можем использовать VehicleSerializer как JsonSerializer<Car>
    val carSerializer: JsonSerializer<Car> = vehicleSerializer
    val bikeSerializer: JsonSerializer<Bike> = vehicleSerializer

    println(carSerializer.serialize(car)) // Работает!
    println(bikeSerializer.serialize(bike)) // Работает!

    // Пример с коллекцией сериализаторов
    val serializers: List<JsonSerializer<Car>> = listOf(
        VehicleSerializer(), // ✅ VehicleSerializer подходит для Car
        CarSerializer()      // ✅ CarSerializer тоже подходит
        //BikeSerializer()   // ❌ НЕЛЬЗЯ! BikeSerializer не подходит для Car
    )

    serializers.forEach { serializer ->
        println(serializer.serialize(car))
    }

    //---------------------------------------------------------

    val cats: MutableList<Cat> = mutableListOf(Cat(), Cat())
    val animals: MutableList<Animal> = mutableListOf()

    copyData(cats, animals) // ✅ Работает благодаря use-site variance
    println("Copied ${animals.size} animals")


    //---------------------------------------------------------

}