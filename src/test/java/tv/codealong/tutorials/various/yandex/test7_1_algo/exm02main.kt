package tv.codealong.tutorials.various.yandex.test7_1_algo

/**
 * Перепад цен
 *
 * Дан массив a, состоящий из n чисел.
 * Необходимо найти две пары индексов i_1 < j_1 и i_2 < j_2 таких, что
 * разность чисел (a_i_1 - a_j_1) = минимально возможное и разность числе (a_i_2 - a_j_2) = максимально возможное.
 *
 * При сравнении отрицательных чисел: больше то число, модуль которого меньше, а меньше то число, модуль которого больше. Это означает, что чем дальше число от нуля в отрицательную сторону, тем оно меньше (например, \(-8<-5\))
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
 *
 * Поиск минимальной разности
 * Массив a должен быть отсортирован по возрастанию. Минимальная разность будет найдена между двумя соседними элементами
 * в отсортированном массиве. Индексы i_1 и j_1 в исходном массиве должны быть найдены для этих двух элементов.
 *
 * Поиск максимальной разности
 * Максимальная разность будет найдена между минимальным и максимальным элементами в массиве. Минимальный элемент в
 * массиве a должен быть найден. Максимальный элемент в массиве a должен быть найден.
 * Индексы i_2 и j_2 в исходном массиве должны быть найдены для этих двух элементов.
 *
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
    val maxPair = findMaxDifferencePairOptimized(arr)
//    val maxPair = findMaxDifferencePair(arr)

    // Выводим результат (индексы 1-based)
    println("${minPair.first + 1} ${minPair.second + 1}")
    println("${maxPair.first + 1} ${maxPair.second + 1}")
}

/**
 * Индексы i_1 и \(j_{1}\) в исходном массиве должны быть найдены для этих двух элементов.
 * Простая версия - полный перебор.
 * Но это решение O(n²) - слишком медленно для n=10⁵!
 *
 * Находит пару индексов (i, j) с минимальной разностью a[i] - a[j] при i < j
 * При равенстве разностей выбирает пару с минимальными индексами
 *
 * Пример для массива [2, 1, 3, 5, 2, 4] -> 6 чисел через пробелы для передачи в консоль (2 1 3 5 2 4):
 * i=0: сравниваем 2 с 1,3,5,2,4 → разности: 1,-1,-3,0,-2
 *   minDiff = -3 (2-5), minI=0, minJ=3
 *
 * i=1: сравниваем 1 с 3,5,2,4 → разности: -2,-4,-1,-3
 *   minDiff = -4 (1-5), minI=1, minJ=3
 *
 * i=2: сравниваем 3 с 5,2,4 → разности: -2,1,-1
 *   minDiff остается -4
 *
 * i=3: сравниваем 5 с 2,4 → разности: 3,1
 *   minDiff остается -4
 *
 * i=4: сравниваем 2 с 4 → разность: -2
 *   minDiff остается -4
 */
fun findMinDifferencePair(arr: IntArray): Pair<Int, Int> {
    var minI = 0
    var minJ = 1
    var minDiff = arr[0] - arr[1]

    // Проходим по всем возможным парам i < j
    // Перебираем ВСЕ возможные пары где i < j
    for (i in 0 until arr.size - 1) {
        println("i=$i, value=${arr[i]}")
        for (j in i + 1 until arr.size) {
            println("j=$j, value=${arr[j]}")
            val diff = arr[i] - arr[j]
            println("diff=${arr[i]}-${arr[j]}=$diff")

            // Сравниваем с текущим минимумом
            // Если нашли меньшую разность ИЛИ такую же разность но с меньшими индексами
            if (diff < minDiff) {
                // Нашли меньшую разность - обновляем
                minDiff = diff
                minI = i
                minJ = j
            } else if (diff == minDiff) {
                // Разность такая же, проверяем индексы
                if (i < minI) {
                    // i меньше - обновляем
                    minI = i
                    minJ = j
                } else if (i == minI && j < minJ) {
                    // i такой же, но j меньше - обновляем
                    minJ = j
                }
            }
//            if (diff < minDiff ||
//                (diff == minDiff && i < minI) ||
//                (diff == minDiff && i == minI && j < minJ)) {
//                minDiff = diff
//                minI = i
//                minJ = j
//            }
        }
    }
    return Pair(minI, minJ)
}

/**
 * Простая версия - полный перебор.
 * Но это решение O(n²) - слишком медленно для n=10⁵!
 *
 * Находит пару индексов (i, j) с минимальной разностью a[i] - a[j] при i < j
 * При равенстве разностей выбирает пару с минимальными индексами
 *
 * Пример для массива [2, 1, 3, 5, 2, 4] -> 6 чисел через пробелы для передачи в консоль (2 1 3 5 2 4):
 * i=0: сравниваем 2 с 1,3,5,2,4 → разности: 1,-1,-3,0,-2
 *   minDiff = -3 (2-5), minI=0, minJ=3
 *
 * i=1: сравниваем 1 с 3,5,2,4 → разности: -2,-4,-1,-3
 *   minDiff = -4 (1-5), minI=1, minJ=3
 *
 * i=2: сравниваем 3 с 5,2,4 → разности: -2,1,-1
 *   minDiff остается -4
 *
 * i=3: сравниваем 5 с 2,4 → разности: 3,1
 *   minDiff остается -4
 *
 * i=4: сравниваем 2 с 4 → разность: -2
 *   minDiff остается -4
 */
fun findMinDifferencePair2(arr: IntArray): Pair<Int, Int> {
    var minI = 0
    var minJ = 1
    //сортируем массив
    arr.sort()
    //минимальная разность
    val i1 = arr[0] - arr[arr.size - 1]
    println("min diff=${arr[0]}-${arr[arr.size - 1]}=$i1")

    var minDiff = arr[0] - arr[1]

    // Проходим по всем возможным парам i < j
    // Перебираем ВСЕ возможные пары где i < j
    for (i in 0 until arr.size - 1) {
        println("i=$i, value=${arr[i]}")
        for (j in i + 1 until arr.size) {
            println("j=$j, value=${arr[j]}")
            val diff = arr[i] - arr[j]
            println("diff=${arr[i]}-${arr[j]}=$diff")

            // Сравниваем с текущим минимумом
            // Если нашли меньшую разность ИЛИ такую же разность но с меньшими индексами
            if (diff < minDiff) {
                // Нашли меньшую разность - обновляем
                minDiff = diff
                minI = i
                minJ = j
            } else if (diff == minDiff) {
                // Разность такая же, проверяем индексы
                if (i < minI) {
                    // i меньше - обновляем
                    minI = i
                    minJ = j
                } else if (i == minI && j < minJ) {
                    // i такой же, но j меньше - обновляем
                    minJ = j
                }
            }
//            if (diff < minDiff ||
//                (diff == minDiff && i < minI) ||
//                (diff == minDiff && i == minI && j < minJ)) {
//                minDiff = diff
//                minI = i
//                minJ = j
//            }
        }
    }
    return Pair(minI, minJ)
}

/**
 * Простая версия - полный перебор.
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
        println("i=$i, value=${arr[i]}")
        for (j in i + 1 until arr.size) {
            println("j=$j, value=${arr[j]}")
            val diff = arr[i] - arr[j]
            println("diff=${arr[i]}-${arr[j]}=$diff")

            // Если нашли большую разность ИЛИ такую же разность но с меньшими индексами
            if (diff > maxDiff ||
                (diff == maxDiff && i < maxI) ||
                (diff == maxDiff && i == maxI && j < maxJ)
            ) {
                maxDiff = diff
                maxI = i
                maxJ = j
            }
        }
    }

    return Pair(maxI, maxJ)
}

/**
 * Оптимизированный поиск минимальной разности.
 * Минимальная разность будет между соседними элементами в отсортированном массиве
 * Пример для массива [2, 1, 3, 5, 2, 4] -> 6 чисел через пробелы для передачи в консоль (2 1 3 5 2 4):
 * Шаг 1: Создаем массив с сохранением исходных индексов
 * Шаг 2: Сортируем с индексами по значению: [(1,1), (2,0), (2,4), (3,2), (4,5), (5,3)]
 * Шаг 3: Берем модуль разности для сравнения
 * Шаг 4: Нашли меньшую абсолютную разность
 * Шаг 5: Определяем правильный порядок индексов (i < j)
 * Сравниваем соседей:
 * (1,1) и (2,0): diff = 1-2 = -1, abs=1
 * minDiff = -1, minI=1, minJ=0 ❌ (не i<j)
 * (2,0) и (2,4): diff = 2-2 = 0, abs=0 ← НАИЛУЧШАЯ!
 * minDiff = 0, minI=0, minJ=4
 * (2,4) и (3,2): diff = 2-3 = -1, abs=1
 * (3,2) и (4,5): diff = 3-4 = -1, abs=1
 * (4,5) и (5,3): diff = 4-5 = -1, abs=1
 * Шаг 6: Сравниваем с текущей лучшей парой
 */
fun findMinDifferencePairOptimized(arr: IntArray): Pair<Int, Int> {
    // Шаг 1: Создаем массив пар с сохранением исходных индексов (значение, исходный индекс)
    val indexedArr = Array(arr.size) { i -> Pair(arr[i], i) }
    // Получаем: [(2,0), (1,1), (3,2), (5,3), (2,4), (4,5)]
    // Сохраняем массив со значениями и индексами
    val indexedArray = arr.withIndex().toList()

    // Шаг 2: Сортируем по значению
    indexedArr.sortBy { it.first }
    // Получаем: [(1,1), (2,0), (2,4), (3,2), (4,5), (5,3)]

//    var minI = -1
//    var minJ = -1
//    var minDiff = Int.MAX_VALUE
    var minI = 0
    var minJ = 1
    var minDiff = arr[0] - arr[1]

    // Ищем минимальную разность между соседними элементами в отсортированном массиве
    for (i in 0 until indexedArr.size - 1) {
        val current = indexedArr[i]     // (значение, индекс)
        println("current: $current")
        val next = indexedArr[i + 1]    // (значение, индекс)
        println("next: $next")
        val diff = current.first - next.first   // Разность значений
        println("diff: $diff")

        // Берем разность для сравнения
        val absDiff = current.first - next.first

        if (diff < minDiff) {
            // Нашли меньшую абсолютную разность
            minDiff = diff  // Сохраняем реальную разность (может быть отрицательной)

            // Определяем правильный порядок индексов (i < j)
            if (current.second < next.second) {
                minI = current.second
                minJ = next.second
            } else {
                minI = next.second
                minJ = current.second
            }
        } else if (absDiff == minDiff) {
            // Такая же абсолютная разность, проверяем условия выбора
            val candidateI: Int
            val candidateJ: Int

            // Определяем порядок индексов для кандидата
            if (current.second < next.second) {
                candidateI = current.second
                candidateJ = next.second
            } else {
                candidateI = next.second
                candidateJ = current.second
            }

            // Сравниваем с текущей лучшей парой
            if (candidateI < minI || (candidateI == minI && candidateJ < minJ)) {
                minI = candidateI
                minJ = candidateJ
                minDiff = diff
            }
        }
        //if (absDiff < Math.abs(minDiff) ||
        //            (absDiff == Math.abs(minDiff) && diff < minDiff)
        //        ) {
        //            // Определяем правильный порядок индексов (i < j)
        //            val idx1 = indexedArr[i].second
        //            val idx2 = indexedArr[i + 1].second
        //
        //            if (idx1 < idx2) {
        //                minI = idx1
        //                minJ = idx2
        //            } else {
        //                minI = idx2
        //                minJ = idx1
        //            }
        //            minDiff = arr[minI] - arr[minJ]
        //        }
    }

    return Pair(minI, minJ)
}

/**
 * Оптимизированный поиск максимальной разности
 * Максимальная разность = максимальный элемент - минимальный элемент (при i < j)
 *
 * Пошагово для [2, 1, 3, 5, 2, 4] -> 6 чисел через пробелы для передачи в консоль (2 1 3 5 2 4):
 * i=1: arr[1]=1
 *   minIdx=0, проверяем (0,1): 2-1=1 > maxDiff=-1
 *   maxDiff=1, maxI=0, maxJ=1
 *   arr[1]=1 < arr[0]=2 → minIdx=1
 *
 * i=2: arr[2]=3
 *   minIdx=1, проверяем (1,2): 1-3=-2 < maxDiff=1
 *   arr[2]=3 > arr[1]=1 → minIdx остается 1
 *
 * i=3: arr[3]=5
 *   minIdx=1, проверяем (1,3): 1-5=-4 < maxDiff=1
 *   arr[3]=5 > arr[1]=1 → minIdx остается 1
 *
 * i=4: arr[4]=2
 *   minIdx=1, проверяем (1,4): 1-2=-1 < maxDiff=1
 *   arr[4]=2 > arr[1]=1 → minIdx остается 1
 *
 * i=5: arr[5]=4
 *   minIdx=1, проверяем (1,5): 1-4=-3 < maxDiff=1
 */
fun findMaxDifferencePairOptimized(arr: IntArray): Pair<Int, Int> {
    var minIdx = 0  // Индекс минимального элемента
    var maxIdx = 0
    var maxI = 0
    var maxJ = 1
    var maxDiff = arr[0] - arr[1]

    for (i in 1 until arr.size) {
        // Проверяем кандидата: (minIdx, i)
        if (minIdx < i) {
            val diff = arr[minIdx] - arr[i]
            if (diff > maxDiff) {
                maxDiff = diff
                maxI = minIdx
                maxJ = i
            } else if (diff == maxDiff) {
                // Такая же разность, проверяем индексы
                if (minIdx < maxI || (minIdx == maxI && i < maxJ)) {
                    maxI = minIdx
                    maxJ = i
                }
            }
        }

        // Обновляем минимум если нужно
        if (arr[i] < arr[minIdx]) {
            minIdx = i
        }
    }
    //for (i in 1 until arr.size) {
    //        // Проверяем, может ли текущий элемент быть лучшим минимумом
    //        if (arr[i] < arr[minIdx]) {
    //            minIdx = i
    //        }
    //
    //        // Проверяем пару (minIdx, i) - это кандидат на максимальную разность
    //        if (minIdx < i) {
    //            val diff = arr[minIdx] - arr[i]
    //            if (diff > maxDiff ||
    //                (diff == maxDiff && minIdx < maxI) ||
    //                (diff == maxDiff && minIdx == maxI && i < maxJ)
    //            ) {
    //                maxDiff = diff
    //                maxI = minIdx
    //                maxJ = i
    //            }
    //        }
    //
    //        // Также проверяем пару (maxIdx, i)
    //        if (maxIdx < i) {
    //            val diff = arr[maxIdx] - arr[i]
    //            if (diff > maxDiff ||
    //                (diff == maxDiff && maxIdx < maxI) ||
    //                (diff == maxDiff && maxIdx == maxI && i < maxJ)
    //            ) {
    //                maxDiff = diff
    //                maxI = maxIdx
    //                maxJ = i
    //            }
    //        }
    //
    //        // Обновляем максимум
    //        if (arr[i] > arr[maxIdx]) {
    //            maxIdx = i
    //        }
    //    }

    return Pair(maxI, maxJ)
}