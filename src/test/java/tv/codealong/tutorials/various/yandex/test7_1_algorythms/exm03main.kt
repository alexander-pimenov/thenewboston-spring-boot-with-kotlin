package tv.codealong.tutorials.various.yandex.test7_1_algorythms

fun findMinMaxDifferencePairs(a: IntArray): Pair<Pair<Int, Int>, Pair<Int, Int>> {
    val n = a.size

    // Сохраняем массив со значениями и индексами
    val indexedArray = a.withIndex().toList()

    // --- Поиск минимальной разности ---
    // Сортируем по значению, но сохраним индексы для проверки i < j
    val sortedByValue = indexedArray.sortedBy { it.value }

    var minDiff = Int.MAX_VALUE
    var minPair = Pair(-1, -1)

    for (k in 0 until n - 1) {
        val i = sortedByValue[k].index
        val j = sortedByValue[k + 1].index
        if (i < j) {
            val diff = a[i] - a[j]
            if (diff < minDiff) {
                minDiff = diff
                minPair = Pair(i, j)
            }
        } else if (j < i) {
            // Проверяем в случае j < i, так как разность зависит от порядка индексов
            val diff = a[j] - a[i]
            if (diff < minDiff) {
                minDiff = diff
                minPair = Pair(j, i)
            }
        }
    }

    // --- Поиск максимальной разности ---
    // Нужно найти i < j, дающие max(a_i - a_j)
    var maxDiff = Int.MIN_VALUE
    var maxPair = Pair(-1, -1)

    // Линейный проход с хранением минимального элемента справа
    var minValFromRight = a[n - 1]
    var minIndexFromRight = n - 1

    for (i in n - 2 downTo 0) {
        // Проверяем разницу с минимальным элементом справа
        val diff = a[i] - minValFromRight
        if (diff > maxDiff && i < minIndexFromRight) {
            maxDiff = diff
            maxPair = Pair(i, minIndexFromRight)
        }
        // Обновляем минимальный элемент справа
        if (a[i] < minValFromRight) {
            minValFromRight = a[i]
            minIndexFromRight = i
        }
    }

    return Pair(minPair, maxPair)
}

// Пример использования:
fun main() {
    val a = intArrayOf(2, 1, 3, 5, 2, 4)
    //2 4
    //1 2
//    val a = intArrayOf(3, 8, 2, 5, 1, 7)
    val (minPair, maxPair) = findMinMaxDifferencePairs(a)

    println("Минимальная разность: a[${minPair.first}] - a[${minPair.second}] = ${a[minPair.first]} - ${a[minPair.second]} = ${a[minPair.first] - a[minPair.second]}")
    println("Максимальная разность: a[${maxPair.first}] - a[${maxPair.second}] = ${a[maxPair.first]} - ${a[maxPair.second]} = ${a[maxPair.first] - a[maxPair.second]}")

    val a2 = -10
    val b2 = -11

    if (a2 < b2) {
        println("a2 < b2")
    } else {
        println("a2 >= b2")
    }
    val aaa = -10
    val b = -20

    if (aaa < b) {
        println("a меньше b") // Вывод: a меньше b
    }

    if (aaa > b) {
        println("a больше b") // Вывод: a больше b
    }

    if (aaa == 10) {
        println("a равно 10") // Вывод: a равно 10
    }

    if (aaa != b) {
        println("a не равно b") // Вывод: a не равно b
    }

}
