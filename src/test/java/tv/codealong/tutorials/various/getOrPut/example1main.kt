package tv.codealong.tutorials.various.getOrPut

import java.math.BigInteger

/**
 * ## 🎯 **ЧТО ТАКОЕ `getOrPut`?**
 * ### 🎯 Чёткая семантика "получить или создать" 🎯
 *
 * **`getOrPut` делает две вещи одновременно:**
 * 1. **Пытается получить** значение по ключу из Map
 * 2. **Если ключа нет** - вычисляет новое значение, добавляет в Map и возвращает его
 *
 * ### Простая аналогия:
 * ```kotlin
 * // 📚 Представь библиотеку с картотекой:
 * // - Если книга есть в картотеке → достаём её
 * // - Если книги нет → заказываем новую и добавляем в картотеку
 * ```
 *
 * ## 🔍 **КАК РАБОТАЕТ `getOrPut`:**
 *
 * ### Базовый синтаксис:
 * ```kotlin
 * val value = map.getOrPut(key) {
 *     defaultValue // Вычисляется только если ключа нет
 * }
 * ```
 */
fun main() {
    val text = "hello world hello kotlin world and hello again"
    val counts = countWords(text)
    println(counts) // {hello=3, world=2, kotlin=1, and=1, again=1}

    val calc = Calculator()
    println(calc.factorial(5)) // 120 (вычисляет)
    println(calc.factorial(5)) // 120 (берёт из кеша)
    println(calc.factorial(6)) // 720 (вычисляет только 6 * 120)
    println(calc.factorial(7)) // 5040 (вычисляет только 7 * 720)

    val lonCalc = CalculatorBigInteger()
    println(lonCalc.factorial(20))

    val people = listOf(
        Person("Alice", "Moscow"),
        Person("Bob", "SPb"),
        Person("Carol", "Moscow"),
        Person("Dave", "SPb")
    )

    val groups = groupByCity(people)
    println(groups)
    // {Moscow=[Alice, Carol], SPb=[Bob, Dave]}

    val config = mutableMapOf<String, Int>()
    val timeout = config.getOrPut("timeout") { 30 } // Int
    println(timeout) // 30
    println(config) // {timeout=30}

    val settings = mutableMapOf<String, Boolean>()
    val debug = settings.getOrPut("debug") { false } // Boolean
    println(debug) // false
    println(settings) // {debug=false}
}

/**
 * Пример 1: **Подсчёт частоты слов**
 */
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

/**
 * Пример 2: **Кеширование вычислений**
 */
class Calculator {
    private val cache = mutableMapOf<Int, Long>()

    fun factorial(n: Int): Long {
        return cache.getOrPut(n) {
            if (n <= 1) 1L else n * factorial(n - 1)
        }
    }
}

class CalculatorBigInteger {
    private val cache = mutableMapOf<Int, BigInteger>()

    fun factorial(n: Int): BigInteger {
        return cache.getOrPut(n) {
            if (n <= 1) BigInteger.ONE else BigInteger.valueOf(n.toLong()) * factorial(n - 1)
        }
    }
}

/**
 * Пример 3: **Группировка объектов**
 */
data class Person(val name: String, val city: String)

fun groupByCity(people: List<Person>): Map<String, List<Person>> {
    val groups = mutableMapOf<String, MutableList<Person>>()

    people.forEach { person ->
        // ✅ Создаём список для города если его нет
        val cityGroup = groups.getOrPut(person.city) {
            mutableListOf()
        }
        cityGroup.add(person)
    }

    return groups
}