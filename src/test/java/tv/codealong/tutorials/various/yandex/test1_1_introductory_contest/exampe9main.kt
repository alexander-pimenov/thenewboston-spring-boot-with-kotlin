package tv.codealong.tutorials.various.yandex.test1_1_introductory_contest

/**
 * Отличная задача! Это классическая задача на генерацию всех правильных скобочных последовательностей. Используем рекурсивный подход с backtracking.
 *
 * ## Решение на Kotlin
 *
 * ### Решение 1: Рекурсивная генерация с проверкой баланса
 * ```kotlin
 * fun main() {
 *     val n = readLine()!!.toInt()
 *     if (n == 0) return
 *
 *     val result = mutableListOf<String>()
 *     generateParentheses(n, result)
 *     result.forEach(::println)
 * }
 *
 * fun generateParentheses(n: Int, result: MutableList<String>, current: String = "", open: Int = 0, close: Int = 0) {
 *     if (current.length == 2 * n) {
 *         result.add(current)
 *         return
 *     }
 *
 *     if (open < n) {
 *         generateParentheses(n, result, "$current(", open + 1, close)
 *     }
 *
 *     if (close < open) {
 *         generateParentheses(n, result, "$current)", open, close + 1)
 *     }
 * }
 * ```
 *
 * ### Решение 2: Более эффективное с StringBuilder
 * ```kotlin
 * fun main() {
 *     val n = readLine()!!.toInt()
 *     if (n == 0) return
 *
 *     val result = mutableListOf<String>()
 *     val current = StringBuilder()
 *     generateParentheses(n, result, current, 0, 0)
 *     result.forEach(::println)
 * }
 *
 * fun generateParentheses(n: Int, result: MutableList<String>, current: StringBuilder, open: Int, close: Int) {
 *     if (current.length == 2 * n) {
 *         result.add(current.toString())
 *         return
 *     }
 *
 *     if (open < n) {
 *         current.append('(')
 *         generateParentheses(n, result, current, open + 1, close)
 *         current.deleteAt(current.length - 1)
 *     }
 *
 *     if (close < open) {
 *         current.append(')')
 *         generateParentheses(n, result, current, open, close + 1)
 *         current.deleteAt(current.length - 1)
 *     }
 * }
 * ```
 *
 * ### Решение 3: С использованием CharArray для минимального использования памяти
 * ```kotlin
 * fun main() {
 *     val n = readLine()!!.toInt()
 *     if (n == 0) return
 *
 *     val result = mutableListOf<String>()
 *     val chars = CharArray(2 * n)
 *     generateParentheses(n, result, chars, 0, 0, 0)
 *     result.forEach(::println)
 * }
 *
 * fun generateParentheses(n: Int, result: MutableList<String>, chars: CharArray, pos: Int, open: Int, close: Int) {
 *     if (pos == 2 * n) {
 *         result.add(String(chars))
 *         return
 *     }
 *
 *     if (open < n) {
 *         chars[pos] = '('
 *         generateParentheses(n, result, chars, pos + 1, open + 1, close)
 *     }
 *
 *     if (close < open) {
 *         chars[pos] = ')'
 *         generateParentheses(n, result, chars, pos + 1, open, close + 1)
 *     }
 * }
 * ```
 *
 * ## Как это работает:
 *
 * ### Алгоритм:
 * 1. **Открывающие скобки**: можем добавить, если `open < n`
 * 2. **Закрывающие скобки**: можем добавить, если `close < open` (больше открытых чем закрытых)
 *
 * ### Пример для n = 2:
 * ```
 * Генерация:
 * 1. "(" → "((" → "(()" → "(())" ✓
 * 2. "(" → "()" → "()(" → "()()" ✓
 * ```
 *
 * ### Визуализация дерева рекурсии для n = 2:
 * ```
 *         ""
 *        /
 *       "("
 *      /    \
 *    "(("   "()"
 *    /       \
 *  "(()"     "()("
 *  /          \
 * "(())"✓     "()()"✓
 * ```
 *
 * ## Почему это лексикографический порядок?
 *
 * Так как мы сначала всегда пробуем добавить `'('`, а потом `')'`, и `'('` < `')'` в ASCII, то последовательности автоматически генерируются в лексикографическом порядке.
 *
 * ## Сложность:
 *
 * - **Время**: O(C_n) - пропорционально количеству правильных последовательностей (числа Каталана)
 * - **Память**: O(n) - глубина рекурсии + массив для хранения текущей последовательности
 *
 * ## Числа Каталана для n ≤ 11:
 * ```
 * n | Количество последовательностей
 * 0 | 1
 * 1 | 1
 * 2 | 2
 * 3 | 5
 * 4 | 14
 * 5 | 42
 * 6 | 132
 * 7 | 429
 * 8 | 1430
 * 9 | 4862
 * 10| 16796
 * 11| 58786
 * ```
 *
 * ## Проверка для n = 3:
 * ```kotlin
 * fun main() {
 *     val n = 3
 *     val result = mutableListOf<String>()
 *     generateParentheses(n, result)
 *     println(result.joinToString("\n"))
 * }
 *
 * // Вывод:
 * // ((()))
 * // (()())
 * // (())()
 * // ()(())
 * // ()()()
 * ```
 *
 * ## Улучшенная версия с итеративным выводом (если памяти мало):
 * ```kotlin
 * fun main() {
 *     val n = readLine()!!.toInt()
 *     if (n > 0) {
 *         generateAndPrintParentheses(n)
 *     }
 * }
 *
 * fun generateAndPrintParentheses(n: Int) {
 *     val chars = CharArray(2 * n)
 *     generateAndPrint(n, chars, 0, 0, 0)
 * }
 *
 * fun generateAndPrint(n: Int, chars: CharArray, pos: Int, open: Int, close: Int) {
 *     if (pos == 2 * n) {
 *         println(String(chars))
 *         return
 *     }
 *
 *     if (open < n) {
 *         chars[pos] = '('
 *         generateAndPrint(n, chars, pos + 1, open + 1, close)
 *     }
 *
 *     if (close < open) {
 *         chars[pos] = ')'
 *         generateAndPrint(n, chars, pos + 1, open, close + 1)
 *     }
 * }
 * ```
 *
 * ## Рекомендую для собеседования:
 *
 * **Решение 1** - самый читаемый и понятный вариант.
 *
 * **Ключевые моменты для объяснения:**
 * 1. Используем рекурсию с backtracking
 * 2. Следим за балансом открывающих/закрывающих скобок
 * 3. Автоматически получаем лексикографический порядок
 * 4. Сложность оптимальна - O(число Каталана) по времени, O(n) по памяти
 *
 * 😊
 */

fun main() {
    val n = readLine()!!.toInt()
    if (n == 0) return

    val result = mutableListOf<String>()
    generateParenthesesWithRecursion(n, result)
    result.forEach(::println)

    if (n > 0) {
        iterativeOutputVersion(n)
    }
}

/**
 *  ### Визуализация дерева рекурсии для n = 2:
 *  ```
 *           ""
 *          /
 *         "("
 *        /    \
 *      "(("   "()"
 *      /       \
 *    "(()"     "()("
 *    /          \
 *   "(())"✓     "()()"✓
 *  ```
 */
fun generateParenthesesWithRecursion(n: Int, result: MutableList<String>, current: String = "", open: Int = 0, close: Int = 0) {
    if (current.length == 2 * n) {
        result.add(current)
        return
    }

    if (open < n) {
        generateParenthesesWithRecursion(n, result, "$current(", open + 1, close)
    }

    if (close < open) {
        generateParenthesesWithRecursion(n, result, "$current)", open, close + 1)
    }
}

fun iterativeOutputVersion(n: Int) {
    val chars = CharArray(2 * n)
    generateAndPrint(n, chars, 0, 0, 0)
}

fun generateAndPrint(n: Int, chars: CharArray, pos: Int, open: Int, close: Int) {
    if (pos == 2 * n) {
        println(String(chars))
        return
    }

    if (open < n) {
        chars[pos] = '('
        generateAndPrint(n, chars, pos + 1, open + 1, close)
    }

    if (close < open) {
        chars[pos] = ')'
        generateAndPrint(n, chars, pos + 1, open, close + 1)
    }
}