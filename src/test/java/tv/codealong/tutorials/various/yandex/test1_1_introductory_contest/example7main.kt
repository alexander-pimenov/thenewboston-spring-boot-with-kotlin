package tv.codealong.tutorials.various.yandex.test1_1_introductory_contest

/**
 * Требуется найти в бинарном векторе самую длинную последовательность единиц и вывести её длину.
 * Желательно получить решение, работающее за линейное время и при этом проходящее по входному массиву только один раз.
 * Формат ввода:
 * Первая строка входного файла содержит одно число n (n≤10000). Каждая из следующих n строк содержит ровно одно число — очередной элемент массива.
 * Формат вывода:
 * Выходной файл должен содержать единственное число — длину самой длинной последовательности единиц во входном массиве.
 * ```
 * Пример ввода:
 * 5
 * 1
 * 0
 * 1
 * 0
 * 1
 * Пример вывода: 1
 * ```
 * Отличная задача! Нужно найти максимальную последовательность подряд идущих единиц. Давай разберем несколько решений.
 *
 * ## Анализ задачи
 *
 * Нам нужно:
 * - Обрабатывать числа по одному (один проход)
 * - Считать текущую последовательность единиц
 * - Запоминать максимальную найденную последовательность
 * - Время: O(n), память: O(1)
 *
 * ## Решение на Java
 *
 * ### Вариант 1: Простой и эффективный
 * ```java
 * import java.io.*;
 *
 * public class Main {
 *     public static void main(String[] args) throws IOException {
 *         BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
 *         PrintWriter writer = new PrintWriter("output.txt");
 *
 *         int n = Integer.parseInt(reader.readLine());
 *         int currentSequence = 0;
 *         int maxSequence = 0;
 *
 *         for (int i = 0; i < n; i++) {
 *             int num = Integer.parseInt(reader.readLine());
 *             if (num == 1) {
 *                 currentSequence++;
 *                 maxSequence = Math.max(maxSequence, currentSequence);
 *             } else {
 *                 currentSequence = 0;
 *             }
 *         }
 *
 *         writer.println(maxSequence);
 *
 *         reader.close();
 *         writer.close();
 *     }
 * }
 * ```
 *
 * ### Вариант 2: С try-with-resources
 * ```java
 * import java.io.*;
 *
 * public class Main {
 *     public static void main(String[] args) {
 *         try (BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
 *              PrintWriter writer = new PrintWriter("output.txt")) {
 *
 *             int n = Integer.parseInt(reader.readLine());
 *             int currentSequence = 0;
 *             int maxSequence = 0;
 *
 *             for (int i = 0; i < n; i++) {
 *                 int num = Integer.parseInt(reader.readLine());
 *                 if (num == 1) {
 *                     currentSequence++;
 *                     if (currentSequence > maxSequence) {
 *                         maxSequence = currentSequence;
 *                     }
 *                 } else {
 *                     currentSequence = 0;
 *                 }
 *             }
 *
 *             writer.println(maxSequence);
 *
 *         } catch (IOException e) {
 *             System.out.println("Ошибка: " + e.getMessage());
 *         }
 *     }
 * }
 * ```
 *
 * ## Решение на Kotlin
 *
 * ### Вариант 1: Простой способ
 * ```kotlin
 * import java.io.File
 *
 * fun main() {
 *     val lines = File("input.txt").readLines()
 *     val n = lines[0].toInt()
 *
 *     var currentSequence = 0
 *     var maxSequence = 0
 *
 *     for (i in 1..n) {
 *         val num = lines[i].toInt()
 *         if (num == 1) {
 *             currentSequence++
 *             if (currentSequence > maxSequence) {
 *                 maxSequence = currentSequence
 *             }
 *         } else {
 *             currentSequence = 0
 *         }
 *     }
 *
 *     File("output.txt").writeText(maxSequence.toString())
 * }
 * ```
 *
 * ### Вариант 2: Функциональный стиль с fold
 * ```kotlin
 * import java.io.File
 *
 * fun main() {
 *     val lines = File("input.txt").readLines()
 *     val n = lines[0].toInt()
 *
 *     val result = lines.subList(1, n + 1).fold(Pair(0, 0)) { (current, max), line ->
 *         val num = line.toInt()
 *         if (num == 1) {
 *             val newCurrent = current + 1
 *             Pair(newCurrent, maxOf(max, newCurrent))
 *         } else {
 *             Pair(0, max)
 *         }
 *     }
 *
 *     File("output.txt").writeText(result.second.toString())
 * }
 * ```
 *
 * ### Вариант 3: С использованием индексов
 * ```kotlin
 * import java.io.File
 *
 * fun main() {
 *     val lines = File("input.txt").readLines()
 *     val n = lines[0].toInt()
 *
 *     var current = 0
 *     var max = 0
 *
 *     for (i in 1 until lines.size) {
 *         when (lines[i].toInt()) {
 *             1 -> {
 *                 current++
 *                 if (current > max) max = current
 *             }
 *             else -> current = 0
 *         }
 *     }
 *
 *     File("output.txt").writeText(max.toString())
 * }
 * ```
 *
 * ## Пошаговый размотр примера:
 *
 * **Входные данные:**
 * ```
 * 5
 * 1
 * 0
 * 1
 * 0
 * 1
 * ```
 *
 * **Обработка:**
 * ```
 * Шаг | Число | currentSequence | maxSequence
 * ----|-------|----------------|------------
 * 0   |       | 0              | 0
 * 1   | 1     | 1              | 1
 * 2   | 0     | 0              | 1
 * 3   | 1     | 1              | 1
 * 4   | 0     | 0              | 1
 * 5   | 1     | 1              | 1
 * ```
 *
 * **Результат: 1**
 *
 * ## Еще пример:
 *
 * **Входные данные:**
 * ```
 * 7
 * 1
 * 1
 * 0
 * 1
 * 1
 * 1
 * 0
 * ```
 *
 * **Обработка:**
 * ```
 * Шаг | Число | currentSequence | maxSequence
 * ----|-------|----------------|------------
 * 0   |       | 0              | 0
 * 1   | 1     | 1              | 1
 * 2   | 1     | 2              | 2
 * 3   | 0     | 0              | 2
 * 4   | 1     | 1              | 2
 * 5   | 1     | 2              | 2
 * 6   | 1     | 3              | 3
 * 7   | 0     | 0              | 3
 * ```
 *
 * **Результат: 3**
 *
 * ## Ключевые моменты алгоритма:
 *
 * 1. **Текущая последовательность** - счетчик подряд идущих единиц
 * 2. **Максимальная последовательность** - сохраняем лучший результат
 * 3. **При встрече 0** - сбрасываем текущий счетчик
 * 4. **При встрече 1** - увеличиваем счетчик и обновляем максимум
 *
 * ## Сложность:
 * - **Время**: O(n) - один проход по данным
 * - **Память**: O(1) - константная память
 *
 * ## Рекомендации для собеседования:
 *
 * 1. **Подчеркни однопроходность** - алгоритм читает данные только один раз
 * 2. **Объясни сложность** - O(n) по времени, O(1) по памяти
 * 3. **Проверь граничные случаи**:
 *    - Все нули: `0 0 0 0` → результат 0
 *    - Все единицы: `1 1 1 1` → результат n
 *    - Пустой массив: результат 0
 *
 * **Лучшее решение**: Первый вариант на Java или Kotlin - простой, эффективный и легко читаемый.
 *
 */

import java.io.File

fun main() {
//    countMaxOnes3()
//    countMaxOnes2()

    checkSequenceOfNumber1()
}

fun countMaxOnes() {
    val lines = File("src/test/resources/input111.txt").readLines()

    var current = 0
    var max = 0

    for (i in 0 until lines.size) {
        when (lines[i].toInt()) {

            1 -> {
                println(i)
                current++
                if (current > max) max = current
            }

            else -> {
                println(i)
                current = 0
            }
        }
    }

    File("src/test/resources/output111.txt").writeText(max.toString())
}

fun countMaxOnes2() {
    val lines = File("src/test/resources/input111.txt").readLines()
    println(lines.size)

    var currentSequence = 0
    var maxSequence = 0

    for (i in 0 until lines.size) {
        val num = lines[i].toInt()
        if (num == 1) {
            currentSequence++
            if (currentSequence > maxSequence) {
                maxSequence = currentSequence
            }
        } else {
            currentSequence = 0
        }
    }

    File("src/test/resources/output111.txt").writeText(maxSequence.toString())
}

fun countMaxOnes3() {
    val lines = readInput() //[1, 3, 5, 10, 20, 40, 80, 160, 320, 640]
    println(lines)
    if (lines.isEmpty()) {
        println(0)
        return
    }

    var current = 0
    var max = 0

    for (i in 0 until lines.size) {
        val num = lines[i].toInt()
        current = if (num == 1) current + 1 else 0
        max = maxOf(max, current)
    }

    println(max)
}

fun readInput(): List<String> {
    return if (File("input.txt").exists()) {
        File("input.txt").readLines()
    } else {
        val numbers = generateSequence(1) { if (it < 4) it + 2 else it * 2 } // `it` is the previous element
        numbers.take(10).map { it.toString() }.toList()
//        generateSequence(1) { if (it < 8) it + 2 else 0 }.take(10).toList()

    }
}

/**
 * Вариант решения. Проверим последовательность чисел вводимую из консоли.
 * Первым идет число показывающее сколько будет чисел в массиве.
 * Потом идет сам массив.
 */
fun checkSequenceOfNumber1() {
    val n = readLine()!!.toInt()
    if (n == 0) return
    println("проверим n = $n чисел")

    var currentSequence = 0
    var maxSequence = 0

    for (i in 0 until n) {
        val num = readLine()!!.toInt()
        println("i = $i, num = $num")
        if (num == 1) {
            currentSequence++
            if (currentSequence > maxSequence) {
                maxSequence = currentSequence
            }
        } else {
            currentSequence = 0
        }
    }
    println(maxSequence)

}