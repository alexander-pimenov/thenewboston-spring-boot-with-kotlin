package tv.codealong.tutorials.various.yandex.test1_1_introductory_contest

/**
 * readLine() - это функция из стандартной библиотеки Kotlin, которая читает одну строку из
 * стандартного ввода (обычно с клавиатуры).
 * readLine() - это простой и удобный способ чтения пользовательского ввода в Kotlin,
 * который отлично сочетается с null-safe features языка. Всегда обрабатывайте возможные
 * null значения для написания надежного кода!
 */
fun main() {
    try {
        val (a, b) = readLine()!!.split(" ").map { it.toInt() }
        println(a + b)
    } catch (e: Exception) {
        println("Ошибка ввода: ${e.message}")
    }
}
//fun main() {
//    var number: Int? = null
//
//    while (number == null) {
//        println("Введите целое число:")
//        val input = readLine()
//        number = input?.toIntOrNull()
//
//        if (number == null) {
//            println("Некорректный ввод, попробуйте еще раз")
//        }
//    }
//
//    println("Спасибо! Вы ввели: $number")
//}

//fun main() {
//    println("Вводите строки (для выхода введите 'exit'):")
//
//    while (true) {
//        val line = readLine()
//        when {
//            line == "exit" -> break
//            line.isNullOrEmpty() -> continue
//            else -> println("Вы ввели: $line")
//        }
//    }
//}

//fun main() {
//    println("Введите два числа через пробел:")
//    val input = readLine()
//
//    input?.let {
//        val numbers = it.split(" ").mapNotNull { num -> num.toIntOrNull() }
//        if (numbers.size == 2) {
//            println("Сумма: ${numbers[0] + numbers[1]}")
//        } else {
//            println("Нужно ввести два числа!")
//        }
//    }
//}