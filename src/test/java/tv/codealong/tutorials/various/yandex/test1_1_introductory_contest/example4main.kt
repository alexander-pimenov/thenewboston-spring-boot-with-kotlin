package tv.codealong.tutorials.various.yandex.test1_1_introductory_contest

import java.io.File

/**
 * Даны два числа
 * A и B
 * Вам нужно вычислить их сумму A+B
 * В этой задаче вам нужно читать из файла и выводить ответ в файл
 *
 * Формат ввода
 * Первая строка входного файла содержит числа
 * A и B (Integer) разделенные пробелом
 *
 * Формат вывода
 * В единственной строке выходного файла выведите сумму чисел
 * A+B
 *
 *
 *
 */
fun main() {
    try {
        val input = File("input.txt").readText().trim()
        val (a, b) = input.split(" ").map { it.toInt() }
        File("output.txt").writeText((a + b).toString())
    } catch (e: Exception) {
        println("Ошибка: ${e.message}")
    }
}