package tv.codealong.tutorials.various.yandex.test7_1_algorythms

import java.util.Scanner
import kotlin.math.abs

/*
Ссылка - https://education.yandex.ru/handbook/algorithms/article/odnosvyaznyj-spisok
### Задача
Дан массив $$ a $$ из $$ n $$ чисел. Нужно найти две пары индексов $$ i_1 < j_1 $$ и $$ i_2 < j_2 $$, таких что:
- разность чисел $$ a_{i_1} - a_{j_1} $$ минимально возможна (минимальная разница),
- разность чисел $$ a_{i_2} - a_{j_2} $$ максимально возможна (максимальная разница).

### Описание решения
- Для поиска пары с минимальной разницей удобно отсортировать массив и взять пару соседних элементов с наименьшей разницей.
- Для поиска пары с максимальной разницей — взять минимальный и максимальный элементы массива.
- Индексы пар находят исходя из значений.

### Объяснение
- Мы сортируем массив, чтобы найти минимальную разницу между соседними элементами (так как минимальная разница всегда
  находится между соседями в отсортированном массиве).
- Максимальная разница — между самым маленьким и самым большим элементом.
- Возвращаются индексы пар в виде кортежей $$ (i, j) $$, где $$ i < j $$.

Такой подход работает за O(n \log n) из-за сортировки, что эффективно для типичных задач с массивом.

Этот подход известен и широко используется для подобных задач на минимальные и максимальные разности пар.

//[8](https://leetcode.com/problems/minimize-the-maximum-difference-of-pairs/)
//[9](https://www.youtube.com/watch?v=lf1Pxg7IrzQ)
//[10](https://www.geeksforgeeks.org/dsa/number-of-pairs-of-array-where-the-max-and-min-of-pair-is-same-as-their-indices/)
*/
fun findMinMaxDiffPairs(a: IntArray): Pair<Pair<Int, Int>, Pair<Int, Int>> {
    val n = a.size
    // Сохраним пару (значение, индекс)
    val indexed = a.withIndex().toList().sortedBy { it.value }

    // println(indexed) //[IndexedValue(index=4, value=3), IndexedValue(index=5, value=3), IndexedValue(index=2, value=5), IndexedValue(index=3, value=5), IndexedValue(index=0, value=7), IndexedValue(index=1, value=7)]

    // Минимальная разница между соседями после сортировки
    var minDiff = Int.MAX_VALUE
    var minPair = Pair(-1, -1)
    for (i in 0 until n - 1) {
        val diff = indexed[i + 1].value - indexed[i].value
        if (diff < minDiff) {
            minDiff = diff
            minPair = Pair(indexed[i].index, indexed[i + 1].index)
        }
    }

    // Максимальная разница - между минимальным и максимальным элементами
    val maxPair = Pair(indexed[0].index, indexed[n - 1].index)

    // Обеспечим, что в парах i < j
    val minPairSorted = if (minPair.first < minPair.second) minPair else Pair(minPair.second, minPair.first)
    val maxPairSorted = if (maxPair.first < maxPair.second) maxPair else Pair(maxPair.second, maxPair.first)

    return Pair(minPairSorted, maxPairSorted)
}

fun main() {
    val testCases = listOf(
        intArrayOf(5, 2, 9, 1, 5, 6),
        intArrayOf(1, 3, 8, 12, 17),
        intArrayOf(10, 10, 10, 10),
        intArrayOf(1, 100),
        intArrayOf(7, 7, 5, 5, 3, 3)
    )

    for ((index, arr) in testCases.withIndex()) {
        val (minPair, maxPair) = findMinMaxDiffPairs(arr)
        println("Test case #$index: array = ${arr.joinToString(", ")}")
        println("  Minimal difference pair indices: $minPair with values (${arr[minPair.first]}, ${arr[minPair.second]}) and amount by module: ${abs(arr[minPair.first] - arr[minPair.second])}")
        println("  Maximal difference pair indices: $maxPair with values (${arr[maxPair.first]}, ${arr[maxPair.second]}) and amount by module: ${abs(arr[maxPair.first] - arr[maxPair.second])}")
        println()
    }

    val arr = intArrayOf(7, 7, 5, 5, 3, 3)
    val (minPair, maxPair) = findMinMaxDiffPairs(arr)

    // Выводим результат (индексы 1-based)
    println("$minPair")
    println("$maxPair")

    //TODO - не верные ответы. разобраться
    //Пример 2
    //Ввод
    //
    //5
    //3 2 4 5 6
    //Вывод
    //
    //2 5
    //1 2

    //6
    //7 7 5 5 3 3
    val scanner = Scanner(System.`in`)
    val n = scanner.nextInt()
    val arr1 = IntArray(n) { scanner.nextInt() }
    val (minPair2, maxPair2) = findMinMaxDiffPairs(arr1)

    // Выводим результат (индексы 1-based)
    println("${minPair2.first} ${minPair2.second}")
    println("${maxPair2.first} ${maxPair2.second}")
}