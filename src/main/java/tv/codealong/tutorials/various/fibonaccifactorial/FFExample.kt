package tv.codealong.tutorials.various.fibonaccifactorial

import java.math.BigInteger

class FFExample {
}

/**
 * Преимущества итеративного подхода:
 * Временная сложность: O(n) - линейная
 *
 * Пространственная сложность: O(1) - константная
 *
 * Производительность: очень высокая даже для больших n
 *
 * Память: использует всего две переменные
 *
 * Для n = 50 результат будет: 12586269025 (вычисляется мгновенно)
 *
 * Рекурсия же для такого n работала бы нереалистично долго из-за экспоненциального роста количества вызовов.
 */
fun main() {
    val n = 10
    println("Число Фибоначчи для n=$n:")
    println("Итеративный метод: ${fibonacciIterative(n)}")
    println("Через массив: ${fibonacciArray(n)}")
    println("Через последовательность: ${fibonacciSequence(n)}")

    // Тест производительности
    val largeN = 50
    println("\nF($largeN) = ${fibonacciIterative(largeN)}")


    val numbers = listOf(0, 1, 5, 10, 20)

    println("Факториалы итеративным методом:")
    numbers.forEach { n ->
        println("$n! = ${factorialIterative(n)}")
    }

    println("\nБольшие числа (с BigInteger):")
    val bigNumbers = listOf(30, 50, 100)
    bigNumbers.forEach { n ->
        println("$n! = ${factorialBig(n)}")
    }

    // Сравнение производительности
    val nn = 20
    println("\nСравнение для n=$nn:")
    println("Итеративный: ${factorialIterative(nn)}")
    println("Функциональный: ${factorialFunctional(nn)}")
}

// Самый рекомендуемый вариант - итеративный
fun fibonacciIterative(n: Int): Long {
    require(n >= 0) { "n must be non-negative" }

    if (n <= 1) return n.toLong()

    var a = 0L
    var b = 1L

    repeat(n - 1) {
        val sum = a + b
        a = b
        b = sum
    }

    return b
}


fun fibonacciSequence(n: Int): Long {
    return generateSequence(Pair(0L, 1L)) {
        Pair(it.second, it.first + it.second)
    }
        .map { it.first }
        .take(n + 1)
        .last()
}

fun fibonacciArray(n: Int): Long {
    if (n <= 1) return n.toLong()

    val fib = LongArray(n + 1)
    fib[0] = 0
    fib[1] = 1

    for (i in 2..n) {
        fib[i] = fib[i - 1] + fib[i - 2]
    }

    return fib[n]
}

// Самый рекомендуемый вариант - простой итеративный
fun factorialIterative(n: Int): Long {
    require(n >= 0) { "n must be non-negative" }

    var result = 1L
    for (i in 2..n) {
        result *= i
    }
    return result
}

fun factorialFunctional(n: Int): Long {
    require(n >= 0) { "n must be non-negative" }

    return if (n == 0) {
        1L
    } else {
        (1L..n.toLong()).reduce { acc, i -> acc * i }
    }
}

fun factorialBig(n: Int): BigInteger {
    require(n >= 0) { "n must be non-negative" }

    var result = BigInteger.ONE
    for (i in 2..n) {
        result = result.multiply(BigInteger.valueOf(i.toLong()))
    }
    return result
}

fun factorialSequence(n: Int): Long {
    require(n >= 0) { "n must be non-negative" }

    return generateSequence(1L to 1) { (acc, i) ->
        if (i < n) acc * (i + 1) to i + 1 else null
    }.last().first
}

