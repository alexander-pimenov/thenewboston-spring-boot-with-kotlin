package tv.codealong.tutorials.various.yandex.test1_1_introductory_contest

/**
 * Отличная задача! Нужно удалить дубликаты из отсортированного массива, используя константную память. Это классическая задача "Remove Duplicates from Sorted Array".
 *
 * ## Анализ задачи
 *
 * - Массив уже отсортирован → дубликаты идут подряд
 * - Нужно использовать O(1) памяти → не можем создавать новые массивы
 * - Решение за один проход
 *
 * ## Решение на Kotlin со стандартным вводом/выводом
 *
 * ### Решение 1: Однопроходный алгоритм с запоминанием предыдущего элемента
 * ```kotlin
 * fun main() {
 *     val n = readLine()!!.toInt()
 *     if (n == 0) return
 *
 *     var prev = readLine()!!.toInt()
 *     println(prev)
 *
 *     for (i in 1 until n) {
 *         val current = readLine()!!.toInt()
 *         if (current != prev) {
 *             println(current)
 *             prev = current
 *         }
 *     }
 * }
 * ```
 *
 * ### Решение 2: С обработкой пустого массива
 * ```kotlin
 * fun main() {
 *     val n = readLine()!!.toInt()
 *     if (n <= 0) return
 *
 *     var previous: Int? = null
 *
 *     repeat(n) {
 *         val current = readLine()!!.toInt()
 *         if (current != previous) {
 *             println(current)
 *             previous = current
 *         }
 *     }
 * }
 * ```
 *
 * ### Решение 3: Функциональный стиль с sequence
 * ```kotlin
 * fun main() {
 *     val n = readLine()!!.toInt()
 *     var prev: Int? = null
 *
 *     generateSequence { readLine() }
 *         .take(n)
 *         .map { it.toInt() }
 *         .filter { it != prev?.also { prev = it } }
 *         .forEach(::println)
 * }
 * ```
 *
 * ### Решение 4: С использованием windowed логики (в уме)
 * ```kotlin
 * fun main() {
 *     val n = readLine()!!.toInt()
 *     if (n == 0) return
 *
 *     // Первый элемент всегда выводим
 *     var first = readLine()!!.toInt()
 *     println(first)
 *
 *     // Остальные элементы сравниваем с предыдущим
 *     repeat(n - 1) {
 *         val current = readLine()!!.toInt()
 *         if (current != first) {
 *             println(current)
 *             first = current
 *         }
 *     }
 * }
 * ```
 *
 * ## Как это работает:
 *
 * **Входные данные:**
 * ```
 * 5
 * 2
 * 4
 * 8
 * 8
 * 8
 * ```
 *
 * **Выполнение:**
 * ```kotlin
 * n = 5
 * prev = 2 → выводим 2
 * current = 4 ≠ prev → выводим 4, prev = 4
 * current = 8 ≠ prev → выводим 8, prev = 8
 * current = 8 = prev → пропускаем
 * current = 8 = prev → пропускаем
 * ```
 *
 * **Результат:**
 * ```
 * 2
 * 4
 * 8
 * ```
 *
 * ## Преимущества этого подхода:
 *
 * - **Память**: O(1) - храним только предыдущий элемент
 * - **Время**: O(n) - один проход по данным
 * - **Не храним весь массив** - обрабатываем числа по одному
 *
 * ## Для работы с файлами (если нужно):
 *
 * ```kotlin
 * import java.io.File
 *
 * fun main() {
 *     val lines = File("input.txt").readLines()
 *     val n = lines[0].toInt()
 *     if (n == 0) return
 *
 *     var prev = lines[1].toInt()
 *     val output = mutableListOf(prev.toString())
 *
 *     for (i in 2..n) {
 *         val current = lines[i].toInt()
 *         if (current != prev) {
 *             output.add(current.toString())
 *             prev = current
 *         }
 *     }
 *
 *     File("output.txt").writeText(output.joinToString("\n"))
 * }
 * ```
 *
 * ## Проверка граничных случаев:
 *
 * ### Случай 1: Пустой массив
 * ```
 * Ввод: 0
 * Вывод: (ничего)
 * ```
 *
 * ### Случай 2: Все элементы одинаковые
 * ```
 * Ввод:
 * 3
 * 5
 * 5
 * 5
 * Вывод: 5
 * ```
 *
 * ### Случай 3: Без дубликатов
 * ```
 * Ввод:
 * 3
 * 1
 * 2
 * 3
 * Вывод:
 * 1
 * 2
 * 3
 * ```
 *
 * ## Почему это оптимальное решение:
 *
 * 1. **Константная память** - O(1)
 * 2. **Линейное время** - O(n)
 * 3. **Один проход** - не нужно перечитывать данные
 * 4. **Простота** - легко понять и поддерживать
 *
 * ## Рекомендую для собеседования:
 *
 * **Решение 1** - самый понятный и демонстрирует алгоритмическое мышление.
 *
 * Ключевая идея: в отсортированном массиве дубликаты идут подряд, поэтому достаточно сравнивать каждый элемент с предыдущим!
 *
 */

import java.io.File

fun main() {
    val lines = File("src/test/resources/input246.txt").readLines()
    val n = lines[0].toInt() // в файле на первой строке указано количество элементов в массиве
    if (n == 0) return

    var prev = lines[1].toInt()
    val output = mutableListOf(prev.toString())

    for (i in 1..n) {
        println(lines[i])
        val current = lines[i].toInt()
        if (current != prev) {
            output.add(current.toString())
            prev = current
        }
    }

    File("src/test/resources/output246.txt").writeText(output.joinToString("\n"))

    //nonRepeatingNumbers()
}

/**
 * Вариант решения 2: вводим числа из консоли
 */
fun nonRepeatingNumbers()  {
    val n = readLine()!!.toInt()
    if (n == 0) return

    var prev = readLine()!!.toInt()
    println(prev)

    for (i in 1 until n) {
        val current = readLine()!!.toInt()
        if (current != prev) {
            println(current)
            prev = current
        }
    }
}