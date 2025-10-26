package tv.codealong.tutorials.various.yandex.test1_1_introductory_contest

/**
 * Отличная задача! Анаграммы - это классика. Давай разберем несколько решений на Kotlin.
 *
 * ## Анализ задачи
 *
 * Две строки являются анаграммами, если:
 * - Они имеют одинаковую длину
 * - Содержат одинаковые символы в одинаковом количестве (только порядок разный)
 *
 * ## Решение 1: Сортировка символов (простое, но не самое эффективное)
 *
 * ```kotlin
 * fun main() {
 *     val lines = generateSequence { readLine() }.toList()
 *     val s1 = lines[0]
 *     val s2 = lines[1]
 *
 *     if (s1.length != s2.length) {
 *         println(0)
 *         return
 *     }
 *
 *     val result = if (s1.toCharArray().sorted() == s2.toCharArray().sorted()) 1 else 0
 *     println(result)
 * }
 * ```
 *
 * ## Решение 2: Подсчет частот символов (оптимальное)
 *
 * ```kotlin
 * fun main() {
 *     val lines = generateSequence { readLine() }.toList()
 *     val s1 = lines[0]
 *     val s2 = lines[1]
 *
 *     if (s1.length != s2.length) {
 *         println(0)
 *         return
 *     }
 *
 *     val freq = IntArray(26)  // для букв 'a'..'z'
 *
 *     // Увеличиваем счетчики для первой строки
 *     for (c in s1) {
 *         freq[c - 'a']++
 *     }
 *
 *     // Уменьшаем счетчики для второй строки
 *     for (c in s2) {
 *         freq[c - 'a']--
 *     }
 *
 *     // Если все счетчики = 0, то строки - анаграммы
 *     val result = if (freq.all { it == 0 }) 1 else 0
 *     println(result)
 * }
 * ```
 *
 * ## Решение 3: С использованием HashMap (универсальное)
 *
 * ```kotlin
 * fun main() {
 *     val lines = generateSequence { readLine() }.toList()
 *     val s1 = lines[0]
 *     val s2 = lines[1]
 *
 *     if (s1.length != s2.length) {
 *         println(0)
 *         return
 *     }
 *
 *     val freq = mutableMapOf<Char, Int>()
 *
 *     // Подсчет для первой строки
 *     for (c in s1) {
 *         freq[c] = freq.getOrDefault(c, 0) + 1
 *     }
 *
 *     // Проверка для второй строки
 *     for (c in s2) {
 *         val count = freq.getOrDefault(c, 0)
 *         if (count == 0) {
 *             println(0)
 *             return
 *         }
 *         freq[c] = count - 1
 *     }
 *
 *     println(1)
 * }
 * ```
 *
 * ## Решение 4: Компактный вариант
 *
 * ```kotlin
 * fun main() {
 *     val (s1, s2) = generateSequence { readLine() }.take(2).toList()
 *
 *     val result = if (s1.length == s2.length &&
 *         s1.groupingBy { it }.eachCount() == s2.groupingBy { it }.eachCount()) 1 else 0
 *     println(result)
 * }
 * ```
 *
 * ## Решение 5: С использованием XOR (альтернативный подход)
 *
 * ```kotlin
 * fun main() {
 *     val lines = generateSequence { readLine() }.toList()
 *     val s1 = lines[0]
 *     val s2 = lines[1]
 *
 *     if (s1.length != s2.length) {
 *         println(0)
 *         return
 *     }
 *
 *     var xor = 0
 *     var sum1 = 0
 *     var sum2 = 0
 *
 *     for (i in s1.indices) {
 *         xor = xor xor s1[i].code xor s2[i].code
 *         sum1 += s1[i].code
 *         sum2 += s2[i].code
 *     }
 *
 *     val result = if (xor == 0 && sum1 == sum2) 1 else 0
 *     println(result)
 * }
 * ```
 *
 * ## Сравнение решений:
 *
 * | Метод | Время | Память | Примечания |
 * |-------|-------|--------|------------|
 * | **Сортировка** | O(n log n) | O(n) | Простое, но медленное |
 * | **Подсчет частот** | O(n) | O(1) | **Оптимальное** |
 * | **HashMap** | O(n) | O(k) | Универсальное для любого алфавита |
 * | **XOR** | O(n) | O(1) | Быстрое, но есть коллизии |
 *
 * ## Как работает решение с подсчетом частот:
 *
 * **Пример:** `"qiu"` и `"iuq"`
 *
 * 1. Создаем массив на 26 элементов (для a-z)
 * 2. Для `"qiu"`:
 *    - `'q'` → индекс 16: +1
 *    - `'i'` → индекс 8: +1
 *    - `'u'` → индекс 20: +1
 * 3. Для `"iuq"`:
 *    - `'i'` → индекс 8: -1
 *    - `'u'` → индекс 20: -1
 *    - `'q'` → индекс 16: -1
 * 4. Все элементы массива = 0 → анаграммы
 *
 * ## Рекомендую для собеседования:
 *
 * **Решение 2** с массивом частот - оптимальное по времени и памяти.
 *
 * ```kotlin
 * fun main() {
 *     val lines = generateSequence { readLine() }.toList()
 *     val s1 = lines[0]
 *     val s2 = lines[1]
 *
 *     if (s1.length != s2.length) {
 *         println(0)
 *         return
 *     }
 *
 *     val freq = IntArray(26)
 *
 *     for (c in s1) freq[c - 'a']++
 *     for (c in s2) freq[c - 'a']--
 *
 *     println(if (freq.all { it == 0 }) 1 else 0)
 * }
 * ```
 *
 * **Почему это лучше:**
 * - Время: O(n) - два прохода по строкам
 * - Память: O(1) - массив из 26 элементов
 * - Простое и эффективное
 *
 * 😊
 */

fun main() {
    //читаем первый ввод:
    val s1 = readLine()!!
    //читаем второй ввод:
    val s2 = readLine()!!

    if (s1.length != s2.length) {
        println(0)
        return
    }

    val freq = mutableMapOf<Char, Int>()

    // Подсчет для первой строки
    for (c in s1) {
        freq[c] = freq.getOrDefault(c, 0) + 1
    }

    // Проверка для второй строки
    for (c in s2) {
        val count = freq.getOrDefault(c, 0)
        if (count == 0) {
            println(0)
            return
        }
        freq[c] = count - 1
    }

    println(1)
}