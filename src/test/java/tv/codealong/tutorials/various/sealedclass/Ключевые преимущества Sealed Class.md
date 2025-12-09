## Ключевые преимущества Sealed Class:
- `Общее состояние` - все наследники наследуют поля родителя
- `Общие методы` - можно добавить реализацию в sealed class
- `Type-safe` - компилятор знает все возможные варианты
- `Extensible` - легко добавлять новую логику в when выражениях
- `Polymorphic` - можно использовать ООП принципы
- `Sealed Class` идеально подходит для моделирования ограниченных иерархий с общей логикой!

### Чем отличаются data-классы и sealed-классы?

- `data class` — класс для хранения данных с автогенерацией `equals()`, `hashCode()`, `copy()`.  
- `sealed class` — ограниченная иерархия классов, используется для `when`.

🚩`data class` – для хранения данных !!!

`data class` автоматически создаёт:  
- `equals()` и `hashCode()` → сравнение объектов по значениям.  
- `copy()` → удобное копирование с изменением параметров.  
- `toString()` → красивый вывод.  

```Kotlin
data class User(val id: Int, val name: String)

fun main() {
    val user1 = User(1, "Alice")
    val user2 = user1.copy(name = "Bob") // Создаём копию с новым именем

    println(user1) // User(id=1, name=Alice)
    println(user2) // User(id=1, name=Bob)
}
```

🚩`sealed class` – для ограниченных иерархий классов !!!

`sealed class` используется, когда есть фиксированное число подклассов.  
```Kotlin
sealed class NetworkState {
    object Loading : NetworkState()
    data class Success(val data: String) : NetworkState()
    data class Error(val message: String) : NetworkState()
}

fun handleState(state: NetworkState) {
    when (state) {
        is NetworkState.Loading -> println("Загрузка...")
        is NetworkState.Success -> println("Данные: ${state.data}")
        is NetworkState.Error -> println("Ошибка: ${state.message}")
    }
}
```

🚩Можно ли использовать `data class` внутри `sealed class`?

Да! Это лучший вариант для управления состояниями:  
```Kotlin
sealed class Result {
    object Loading : Result()
    data class Success(val data: String) : Result()
    data class Error(val message: String) : Result()
}
```

