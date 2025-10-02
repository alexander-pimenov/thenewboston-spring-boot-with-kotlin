package tv.codealong.tutorials.various.sealedclassvssealedinterface

/**
 * Сравнение в таблице
 * Критерий       	            Sealed Class  	            Sealed Interface
 * Наследование	                Только классы	            Классы, object, data class
 * Расположение	                Один файл	                Разные файлы
 * Множественное наследование	❌ Нет	                    ✅ Да
 * Состояние	                ✅ Может иметь поля	        ❌ Только свойства
 * Методы	        ✅ Может иметь реализацию	            ❌ Только declaration
 * Когда использовать	    Логически единая иерархия	    Распределённые реализации
 *
 */
//Сейчас часто рекомендуют использовать sealed interfaces по умолчанию
// ✅ Современный стиль
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}

// Более гибко, можно расширять интерфейс
data class PartialSuccess<T>(val data: T, val warnings: List<String>) : Result<T>

// Sealed Interface для кеширования
sealed interface CacheResult<out T> {
    data class Hit<T>(val value: T) : CacheResult<T>
    data object Miss : CacheResult<Nothing>
    data class Error(val exception: Throwable) : CacheResult<Nothing>
}

// Sealed Class для UI состояния
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()

    // Общие методы
    fun isLoading(): Boolean = this is Loading
    fun getDataOrNull(): T? = (this as? Success)?.data
}