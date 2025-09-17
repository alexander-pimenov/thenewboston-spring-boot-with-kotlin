package tv.codealong.tutorials.various.other

/**
 * Пример класса с "static" инициализацией в Kotlin:
 */
class DatabaseConfigExampleWithStatic {
    companion object {
        // Аналог static field
        val CONNECTION_POOL = createPool()

        // Аналог static block
        init {
            println("DatabaseConfig class loaded")
            initializeConnectionPool()
        }

        private fun initializeConnectionPool() {
            TODO("Not yet implemented")
        }

        private fun createPool(): ConnectionPool {
            // Сложная инициализация
            return ConnectionPool()
        }
    }
}

class ConnectionPool {

}

//// Предзагрузка такого класса
//PreloadUtils.safePreload("com.yourproject.config.DatabaseConfigExampleWithStatic")


/**
 * Static блоки в Kotlin
 * В Kotlin нет точного аналога static {}, но есть:
 *
 * 1. Companion object + init блок
 */
class MyClass1 {
    companion object {
        init {
            // Аналог static {} - выполняется при первом обращении к классу
            println("This runs like static block")
        }
    }
}

/**
 * Static блоки в Kotlin
 * В Kotlin нет точного аналога static {}, но есть:
 *
 * 2. @JvmStatic + init
 */
class MyClass2 {
    companion object {
        @JvmStatic
        val MY_CONSTANT = computeValue()

        private fun computeValue(): String {
            println("Like static block")
            return "value"
        }
    }
}

/**
 * Static блоки в Kotlin
 * В Kotlin нет точного аналога static {}, но есть:
 *
 * 3. Top-level initialization (в каком-то файле MyUtils.kt)
 */
val initialized = run {
    println("This runs on class loading")
    "initial value"
}