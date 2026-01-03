package tv.codealong.tutorials.various.sealed_class.sealed_classes_examples

//1. Sealed Class с общим состоянием
//
// ✅ Все результаты имеют временную метку
sealed class ApiResponse(val timestamp: Long = System.currentTimeMillis()) {
    data class Success(val data: String) : ApiResponse()
    data class Error(val message: String, val code: Int) : ApiResponse()
    data object Loading : ApiResponse()

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