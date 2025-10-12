package tv.codealong.tutorials.various.inlineReified

/**
 * 📌 Inline функции
 * Что это? Функции, которые компилятор "встраивает" прямо в место вызова, вместо создания реального вызова функции.
 * Зачем нужно?
 * - Убирают накладные расходы на вызов функции (особенно важно для лямбд)
 * - Позволяют использовать reified generics (об этом ниже)
 * - Улучшают производительность в определенных сценариях
 *
 * ## ⚠️ Важные ограничения для inline+reified-функций:
 *
 * 1. **Только с inline функциями** - `reified` работает только в inline функциях
 * 2. **Нельзя использовать как параметр** - нельзя передать `reified T` как параметр
 * 3. **Увеличивает размер байткода** - код дублируется в каждом месте вызова
 *
 * Проблема: В Java/Kotlin информация о generic-типах стирается во время выполнения (type erasure).
 * Решение: reified + inline = магия!
 *
 * ## 🏆 Когда использовать
 *
 * **✅ Хорошо:**
 * - Утилитные функции для создания объектов
 * - Проверки типов во время выполнения
 * - Рефлексивные операции с generic-типами
 * - Фреймворки и библиотеки
 *
 * **❌ Избегать:**
 * - Большие функции (увеличит размер кода)
 * - Рекурсивные вызовы
 * - Функции, редко вызываемые
 */

inline fun <reified T : Any> createInstance(vararg args: Any): T {
    val constructor = T::class.java.getDeclaredConstructor(
        *args.map { it::class.java }.toTypedArray()
    )
    return constructor.newInstance(*args)
}

/**
 * Что происходит:
 * - inline - функция встраивается в место вызова
 * - reified T - тип T сохраняется во время выполнения
 * - T : Any - гарантия, что T не nullable
 * - T::class.java - получаем Class объекта через рефлексию
 * - newInstance() - создаем экземпляр через конструктор по умолчанию
 */
inline fun <reified T : Any> createInstance(): T {
    return T::class.java.getDeclaredConstructor().newInstance()
}

inline fun <reified T> checkType(obj: Any): Boolean {
    return obj is T // работает!
}


data class Person2(
    var name: String? = null,
    var age: Int? = null,
)

fun main() {
    // Создание разных объектов
    val stringInstance: String = createInstance()
    val listInstance: ArrayList<Int> = createInstance()
    println(
        // Создание объекта с параметрами
        createInstance<Person2>("John", 25)
    )
    println(stringInstance)
    println(listInstance)
}

// Обычная функция с лямбдой
fun doSomething(block: () -> Unit) {
    block()
}

// Inline версия
// При компиляции вызов doSomethingInline превратится в:
// block() - код вставляется прямо на место вызова
inline fun doSomethingInline(block: () -> Unit) {
    block()
}

