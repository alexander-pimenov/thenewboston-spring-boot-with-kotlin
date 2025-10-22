package tv.codealong.tutorials.various.yandex.test1_1_introductory_contest

/**
 * Название "A+B"
 * Ввод	- стандартный ввод или input.txt
 * Вывод	- стандартный вывод или output.txt
 * Даны два числа : A и B
 * Вам нужно вычислить их сумму: A+B
 * В этой задаче для работы с входными и выходными данными вы можете использовать и файлы и потоки на ваше усмотрение.
 * Формат ввода:
 * Первая строка входа содержит числа
 * A и B (они целочисленные типа Integer) разделенные пробелом
 * Формат вывода:
 * В единственной строке выхода выведите сумму чисел : A+B
 */
fun main() {
    readLine()?.let { input ->
        val numbers = input.split(" ")
        if (numbers.size == 2) {
            try {
                val a = numbers[0].toInt()
                val b = numbers[1].toInt()
                println(a + b)
            } catch (e: NumberFormatException) {
                println("Ошибка: введите целые числа")
            }
        } else {
            println("Ошибка: введите два числа через пробел")
        }
    }
}