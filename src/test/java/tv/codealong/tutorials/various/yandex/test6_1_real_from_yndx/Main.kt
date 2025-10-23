package tv.codealong.tutorials.various.yandex.test6_1_real_from_yndx


fun main() {

    val calculator = Calculator()
    println("2 + 3 = ${calculator.add(2, 3)}")
    val userService = UserService()
    val user = userService.createUser("Bob", "bob@example.ru")
    println("Создан пользователь: $user")
}

// пример класса
class Calculator {
    fun add(a: Int, b: Int): Int = a + b
    fun multiply(a: Int, b: Int): Int = a * b
}

// пример класса
data class User(
    val id: Int,
    val name: String,
    val email: String
)

// пример класса
class UserService {
    private var nextId = 1

    fun createUser(name: String, email: String): User {
        return User(nextId++, name, email)
    }
}