package tv.codealong.tutorials.various.yandex.test7_1_algo

/**
 * Перепад цен
 *
 * Дан массив a, состоящий из n чисел. Необходимо найти две пары индексов i_1 < j_1 и i_2 < j_2 таких, что
 * разность чисел (a_i_1 - a_j_1) = минимально возможное и разность числе (a_i_2 - a_j_2) = максимально возможное.
 *
 * Формат ввода
 * Первая строка содержит единственное число n - количество чисел в массиве.
 * Вторая строка содержит n разделенных пробелом чисел a_i, где a_i - число на i-ой позиции в массиве a.
 *
 * Формат вывода:
 * Первая строка вывода должна содержать два разделенных пробелом числа i_1 и j_1. Если подходящих пар несколько, то
 * нужно выбрать пару с минимальным значением i_1. Если пар с минимальным значением i_1 несколько, то нужно выбрать пару
 * с минимальным значением j_1.
 * Вторая строка вывода должна содержать два разделенных пробелом числа i_2 и j_2. Если подходящих пар несколько, то
 * нужно выбрать пару с минимальным значением i_2. Если пар с минимальным значением i_2 несколько, то нужно выбрать пару
 * с минимальным значением j_2.
 *
 * Пример 1
 * Ввод:
 * 6
 * 2 1 3 5 2 4
 * Вывод:
 * 2 4
 * 4 5
 *
 * Пример 2
 * Ввод:
 * 5
 * 3 2 4 5 6
 * Вывод:
 * 2 5
 * 1 2
 *
 * Примечание.
 * Ограничения:
 * 3≤n≤10 в 5-й
 * 1≤a_i≤10 в 5-й для всех 1≤i≤n
 *
 * Это интересная задача и требует понимания, как эффективно находить экстремальные разности.
 * Отлично! Давай разберем эту задачу. Она интересная и требует понимания, как эффективно находить экстремальные разности.
 *
 * ## Анализ задачи
 *
 * Нам нужно найти:
 * 1. **Минимальную разность** `a[i] - a[j]` где `i < j`
 * 2. **Максимальную разность** `a[i] - a[j]` где `i < j`
 *
 * Но есть важный нюанс: при равенстве разностей выбираем пары с минимальными индексами.
 *
 * ## Понимание экстремальных разностей
 *
 * **Для минимальной разности:**
 * - Минимальная разность будет близка к 0 (или отрицательная)
 * - Нужно искать близкие по значению элементы
 *
 * **Для максимальной разности:**
 * - Максимальная разность = максимальный элемент - минимальный элемент
 * - Но с ограничением `i < j`
 *
 * ## Эффективное решение
 *
 *
 * ## Разбор примеров:
 *
 * **Пример 1:**
 * ```
 * Ввод: 2 1 3 5 2 4
 *
 * Минимальная разность: 2-2 = 0 (индексы 2 и 4 в 0-based = 3 и 5 в 1-based)
 * Максимальная разность: 5-2 = 3 (индексы 4 и 5 в 0-based = 5 и 6 в 1-based)
 * ```
 *
 * **Пример 2:**
 * ```
 * Ввод: 3 2 4 5 6
 *
 * Минимальная разность: 3-2 = 1 (индексы 1 и 5 в 0-based = 2 и 6 в 1-based)
 * Максимальная разность: 3-2 = 1 (индексы 0 и 1 в 0-based = 1 и 2 в 1-based)
 * ```
 *
 * Ключевая идея: для минимальной разности сортируем и ищем соседние элементы, для максимальной - отслеживаем минимум и максимум по ходу обхода.
 */
import java.util.*
//TODO - более быстрая версия не работает. разобраться нужно.
fun main() {
    val scanner = Scanner(System.`in`)
    val n = scanner.nextInt()
    val arr = IntArray(n) { scanner.nextInt() }

    // Находим пару для минимальной разности
//    val minPair = findMinDifferencePairOptimized(arr)
    val minPair = findMinDifferencePair(arr)
    // Находим пару для максимальной разности
//    val maxPair = findMaxDifferencePairOptimized(arr)
    val maxPair = findMaxDifferencePair(arr)

    // Выводим результат (индексы 1-based)
    println("${minPair.first + 1} ${minPair.second + 1}")
    println("${maxPair.first + 1} ${maxPair.second + 1}")
}

/**
 * Но это решение O(n²) - слишком медленно для n=10⁵!
 *
 * Находит пару индексов (i, j) с минимальной разностью a[i] - a[j] при i < j
 * При равенстве разностей выбирает пару с минимальными индексами
 */
fun findMinDifferencePair(arr: IntArray): Pair<Int, Int> {
    var minI = 0
    var minJ = 1
    var minDiff = arr[0] - arr[1]

    // Проходим по всем возможным парам i < j
    for (i in 0 until arr.size - 1) {
        for (j in i + 1 until arr.size) {
            val diff = arr[i] - arr[j]

            // Если нашли меньшую разность ИЛИ такую же разность но с меньшими индексами
            if (diff < minDiff ||
                (diff == minDiff && i < minI) ||
                (diff == minDiff && i == minI && j < minJ)) {
                minDiff = diff
                minI = i
                minJ = j
            }
        }
    }

    return Pair(minI, minJ)
}

/**
 * Но это решение O(n²) - слишком медленно для n=10⁵!
 *
 * Находит пару индексов (i, j) с максимальной разностью a[i] - a[j] при i < j
 * При равенстве разностей выбирает пару с минимальными индексами
 */
fun findMaxDifferencePair(arr: IntArray): Pair<Int, Int> {
    var maxI = 0
    var maxJ = 1
    var maxDiff = arr[0] - arr[1]

    // Проходим по всем возможным парам i < j
    for (i in 0 until arr.size - 1) {
        for (j in i + 1 until arr.size) {
            val diff = arr[i] - arr[j]

            // Если нашли большую разность ИЛИ такую же разность но с меньшими индексами
            if (diff > maxDiff ||
                (diff == maxDiff && i < maxI) ||
                (diff == maxDiff && i == maxI && j < maxJ)) {
                maxDiff = diff
                maxI = i
                maxJ = j
            }
        }
    }

    return Pair(maxI, maxJ)
}

/**
 * Оптимизированный поиск минимальной разности
 * Минимальная разность будет между соседними элементами в отсортированном массиве
 */
fun findMinDifferencePairOptimized(arr: IntArray): Pair<Int, Int> {
    // Создаем массив пар (значение, исходный индекс)
    val indexedArr = Array(arr.size) { i -> Pair(arr[i], i) }

    // Сортируем по значению
    indexedArr.sortBy { it.first }

    var minI = -1
    var minJ = -1
    var minDiff = Int.MAX_VALUE

    // Ищем минимальную разность между соседними элементами в отсортированном массиве
    for (i in 0 until indexedArr.size - 1) {
        val diff = indexedArr[i].first - indexedArr[i + 1].first
        val absDiff = Math.abs(diff)

        if (absDiff < Math.abs(minDiff) ||
            (absDiff == Math.abs(minDiff) && diff < minDiff)) {
            // Определяем правильный порядок индексов (i < j)
            val idx1 = indexedArr[i].second
            val idx2 = indexedArr[i + 1].second

            if (idx1 < idx2) {
                minI = idx1
                minJ = idx2
            } else {
                minI = idx2
                minJ = idx1
            }
            minDiff = arr[minI] - arr[minJ]
        }
    }

    return Pair(minI, minJ)
}

/**
 * Оптимизированный поиск максимальной разности
 * Максимальная разность = максимальный элемент - минимальный элемент (при i < j)
 */
fun findMaxDifferencePairOptimized(arr: IntArray): Pair<Int, Int> {
    var minIdx = 0
    var maxIdx = 0
    var maxI = 0
    var maxJ = 1
    var maxDiff = arr[0] - arr[1]

    for (i in 1 until arr.size) {
        // Проверяем, может ли текущий элемент быть лучшим минимумом
        if (arr[i] < arr[minIdx]) {
            minIdx = i
        }

        // Проверяем пару (minIdx, i) - это кандидат на максимальную разность
        if (minIdx < i) {
            val diff = arr[minIdx] - arr[i]
            if (diff > maxDiff ||
                (diff == maxDiff && minIdx < maxI) ||
                (diff == maxDiff && minIdx == maxI && i < maxJ)) {
                maxDiff = diff
                maxI = minIdx
                maxJ = i
            }
        }

        // Также проверяем пару (maxIdx, i)
        if (maxIdx < i) {
            val diff = arr[maxIdx] - arr[i]
            if (diff > maxDiff ||
                (diff == maxDiff && maxIdx < maxI) ||
                (diff == maxDiff && maxIdx == maxI && i < maxJ)) {
                maxDiff = diff
                maxI = maxIdx
                maxJ = i
            }
        }

        // Обновляем максимум
        if (arr[i] > arr[maxIdx]) {
            maxIdx = i
        }
    }

    return Pair(maxI, maxJ)
}