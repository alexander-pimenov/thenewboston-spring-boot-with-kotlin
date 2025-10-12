### `getOrPut` 
- это одна из самых полезных и элегантных функций в Kotlin. Давай разберём её на простых примерах! 🎯

## 🎯 **ЧТО ТАКОЕ `getOrPut`?**

**`getOrPut` делает две вещи одновременно:**
1. **Пытается получить** значение по ключу из Map
2. **Если ключа нет** - вычисляет новое значение, добавляет в Map и возвращает его

### Простая аналогия:
```kotlin
// 📚 Представь библиотеку с картотекой:
// - Если книга есть в картотеке → достаём её
// - Если книги нет → заказываем новую и добавляем в картотеку
```

## 🔍 **КАК РАБОТАЕТ `getOrPut`:**

### Базовый синтаксис:
```kotlin
val value = map.getOrPut(key) { 
    defaultValue // Вычисляется только если ключа нет
}
```

## 🧪 **ПРОСТЫЕ ПРИМЕРЫ:**

### Пример 1: **Подсчёт частоты слов**
```kotlin
fun countWords(text: String): Map<String, Int> {
    val wordCount = mutableMapOf<String, Int>()
    
    text.split(" ").forEach { word ->
        // ❌ Без getOrPut (много кода):
        // if (wordCount.containsKey(word)) {
        //     wordCount[word] = wordCount[word]!! + 1
        // } else {
        //     wordCount[word] = 1
        // }
        
        // ✅ С getOrPut (элегантно):
        wordCount[word] = wordCount.getOrPut(word) { 0 } + 1
    }
    
    return wordCount
}

fun main() {
    val text = "hello world hello kotlin world"
    val counts = countWords(text)
    println(counts) // {hello=2, world=2, kotlin=1}
}
```

### Пример 2: **Кеширование вычислений**
```kotlin
class Calculator {
    private val cache = mutableMapOf<Int, Long>()
    
    fun factorial(n: Int): Long {
        return cache.getOrPut(n) {
            if (n <= 1) 1L else n * factorial(n - 1)
        }
    }
}

fun main() {
    val calc = Calculator()
    println(calc.factorial(5)) // 120 (вычисляет)
    println(calc.factorial(5)) // 120 (берёт из кеша)
    println(calc.factorial(6)) // 720 (вычисляет только 6 * 120)
}
```

### Пример 3: **Группировка объектов**
```kotlin
data class Person(val name: String, val city: String)

fun groupByCity(people: List<Person>): Map<String, List<Person>> {
    val groups = mutableMapOf<String, MutableList<Person>>()
    
    people.forEach { person ->
        // ✅ Создаём список для города если его нет
        val cityGroup = groups.getOrPut(person.city) { mutableListOf() }
        cityGroup.add(person)
    }
    
    return groups
}

fun main() {
    val people = listOf(
        Person("Alice", "Moscow"),
        Person("Bob", "SPb"), 
        Person("Carol", "Moscow"),
        Person("Dave", "SPb")
    )
    
    val groups = groupByCity(people)
    println(groups)
    // {Moscow=[Alice, Carol], SPb=[Bob, Dave]}
}
```

## 🔄 **КАК ЭТО РАБОТАЕТ В RATE LIMITER:**

Давай разберём конкретно наш пример:

```kotlin
class SimpleRateLimiter : RateLimiter {
    private val attempts = ConcurrentHashMap<String, MutableList<Instant>>()
    
    override fun tryAcquire(key: String): Boolean {
        val now = Instant.now()
        val windowStart = now.minusSeconds(60)
        
        synchronized(attempts) {
            // 🔍 Вот эта строка:
            val keyAttempts = attempts.getOrPut(key) { mutableListOf() }
            
            // Что происходит:
            // 1. Пытаемся получить список попыток для ключа (например, "login:alice@email.com")
            // 2. Если такого ключа нет → создаём пустой mutableListOf() и добавляем в Map
            // 3. Если ключ есть → просто возвращаем существующий список
            
            // Эквивалент без getOrPut:
            // var keyAttempts = attempts[key]
            // if (keyAttempts == null) {
            //     keyAttempts = mutableListOf()
            //     attempts[key] = keyAttempts
            // }
            
            // Дальше работаем с keyAttempts...
            keyAttempts.removeAll { it.isBefore(windowStart) }
            return keyAttempts.size < 5
        }
    }
}
```

## 🎯 **ПОШАГОВЫЙ РАЗБОР ВЫПОЛНЕНИЯ:**

### Сценарий: **Первый вызов для нового пользователя**
```kotlin
val limiter = SimpleRateLimiter()

// Первый вызов для "user123":
limiter.tryAcquire("user123")

// Внутри getOrPut:
// 1. attempts["user123"] = null (ключа нет)
// 2. Выполняется lambda { mutableListOf() } → создаётся пустой список
// 3. attempts["user123"] = mutableListOf() // добавляется в Map
// 4. Возвращается новый пустой список
```

### Сценарий: **Повторный вызов для того же пользователя**
```kotlin
// Второй вызов для "user123":
limiter.tryAcquire("user123")

// Внутри getOrPut:
// 1. attempts["user123"] = [Instant1] (ключ уже есть)
// 2. Lambda НЕ выполняется!
// 3. Возвращается существующий список [Instant1]
```

## 📊 **ВИЗУАЛИЗАЦИЯ ПРОЦЕССА:**

```
ПЕРВЫЙ ВЫЗОВ tryAcquire("user123"):

attempts Map: {}
↓ getOrPut("user123") { mutableListOf() }
attempts Map: {"user123" → []}
Возвращает: []

ПОВТОРНЫЙ ВЫЗОВ tryAcquire("user123"):

attempts Map: {"user123" → [timestamp1]}
↓ getOrPut("user123") { mutableListOf() } // lambda НЕ выполняется!
attempts Map: {"user123" → [timestamp1, timestamp2]}
Возвращает: [timestamp1, timestamp2]
```

## 🚀 **ПРЕИМУЩЕСТВА getOrPut:**

### ✅ **Лаконичность**
```kotlin
// ❌ Без getOrPut (5 строк):
var list = map[key]
if (list == null) {
    list = mutableListOf()
    map[key] = list
}

// ✅ С getOrPut (1 строка):
val list = map.getOrPut(key) { mutableListOf() }
```

### ✅ **Безопасность**
- Нет `!!` оператора
- Нет `get()` с возможностью NPE
- Чёткая семантика "получить или создать"

### ✅ **Производительность**
- Lambda вычисляется **только если ключа нет**
- При повторных вызовах - минимальные накладные расходы

## 🔧 **РАЗНЫЕ ВАРИАНТЫ ИСПОЛЬЗОВАНИЯ:**

### С примитивами:
```kotlin
val config = mutableMapOf<String, Int>()
val timeout = config.getOrPut("timeout") { 30 } // Int

val settings = mutableMapOf<String, Boolean>()  
val debug = settings.getOrPut("debug") { false } // Boolean
```

### С сложными объектами:
```kotlin
val services = mutableMapOf<String, Service>()
val database = services.getOrPut("database") { 
    DatabaseService(config) // Создаём только если нужно
}

val caches = mutableMapOf<String, Cache<*, *>>()
val userCache = caches.getOrPut("users") { 
    InMemoryCache<String, User>().apply { 
        maxSize = 1000 
    } 
}
```

## 🎯 **В КОНТЕКСТЕ НАШЕГО RATE LIMITER:**

```kotlin
class SimpleRateLimiter {
    private val attempts = ConcurrentHashMap<String, MutableList<Instant>>()
    
    fun tryAcquire(key: String): Boolean {
        synchronized(attempts) {
            val keyAttempts = attempts.getOrPut(key) { mutableListOf() }
            // ↑ Гарантирует что для каждого ключа всегда есть список
            // ↑ Не важно новый пользователь или существующий
            
            // Дальше можно безопасно работать с keyAttempts
            keyAttempts.add(Instant.now())
            keyAttempts.removeAll { ... }
            
            return keyAttempts.size < 5
        }
    }
}
```

Теперь понимаешь эту элегантную функцию? Она идеально подходит для паттернов "получить-или-создать", которые часто встречаются в кешировании, группировке и инициализации! 💪

---
