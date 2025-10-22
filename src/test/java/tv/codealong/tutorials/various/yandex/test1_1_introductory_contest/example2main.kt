package tv.codealong.tutorials.various.yandex.test1_1_introductory_contest

import java.io.File

/**
 * Ключевые моменты для собеседования:
 * - Обработка исключений - всегда показывай, что думаешь о возможных ошибках
 * - Эффективность - BufferedReader обычно эффективнее Scanner для больших объемов данных
 * - Чистота кода - используй понятные имена переменных
 * - Закрытие ресурсов - не забывай закрывать потоки или используй try-with-resources
 *
 * Рекомендую для собеседования: Вариант 2 на Java с BufferedReader или простой вариант на Kotlin - они показывают понимание основ и эффективности.
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