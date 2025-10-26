package tv.codealong.tutorials.various.yandex.test1_1_introductory_contest

import java.io.File

/**
 * Ключевые моменты для собеседования:
 * 1. Обработка исключений
 * - Всегда обрабатывай FileNotFoundException, NumberFormatException
 * - Покажи, что думаешь о возможных ошибках
 *
 * 2. Закрытие ресурсов
 * - В Java используй try-with-resources
 * - В Kotlin используй use или useLines
 *
 * 3. Проверка входных данных
 * - Проверяй существование файла
 * - Проверяй количество чисел
 * - Обрабатывай некорректный формат
 *
 * 4. Эффективность
 * - Для больших файлов используй буферизованное чтение
 * - Для маленьких файлов подойдут простые методы
 *
 * Рекомендуемые решения:
 * Для Java: Вариант 3 с try-with-resources
 * Для Kotlin: Вариант 2 с обработкой исключений
 *
 * Оба варианта показывают хорошее понимание работы с файлами и обработки ошибок, что важно на собеседовании!
 */
fun main() {
    try {
        val inputFile = File("input.txt")
        val outputFile = File("output.txt")

        if (!inputFile.exists()) {
            println("Файл input.txt не найден")
            return
        }

        val input = inputFile.readText().trim()
        val numbers = input.split("\\s+".toRegex()) // разделитель - пробелы

        if (numbers.size >= 2) {
            val a = numbers[0].toInt()
            val b = numbers[1].toInt()
            outputFile.writeText((a + b).toString())
        } else {
            println("Ошибка: в файле должно быть два числа через пробел")
        }

    } catch (e: NumberFormatException) {
        println("Ошибка: в файле должны быть целые числа")
    } catch (e: Exception) {
        println("Ошибка: ${e.message}")
    }
}