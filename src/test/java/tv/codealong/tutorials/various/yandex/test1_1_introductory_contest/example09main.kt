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
 *
 * Понимаю, рекурсия действительно одна из самых сложных тем для понимания! Давай разберем эту задачу **очень медленно и по шагам**, без спешки.
 *
 * ## Сначала поймем, что такое "правильные скобочные последовательности"
 *
 * Для `n = 2` (две пары скобок) правильные последовательности:
 * - `(())` - ✓ правильно
 * - `()()` - ✓ правильно
 * - `())(` - ✗ неправильно
 * - `)(()` - ✗ неправильно
 *
 * **Правило**: в любой момент количество закрывающих скобок не должно превышать количество открывающих.
 *
 * ## Давай представим, что мы строим скобки вручную
 *
 * Представь, что у тебя есть 2 открывающие `(` и 2 закрывающие `)` скобки. Ты строишь последовательность:
 *
 * ```
 * Шаг 1: Можно поставить только "(" (потому что закрывающих не может быть больше открывающих)
 * Текущее: "("
 *
 * Шаг 2: Можно поставить "(" или ")"
 * Вариант 2.1: "(("
 * Вариант 2.2: "()"
 *
 * Шаг 3: Продолжаем каждый вариант...
 * ```
 *
 * ## Давай напишем ПРОСТУЮ версию без рекурсии для n=2
 *
 * ```kotlin
 * fun main() {
 *     println("Для n=2:")
 *
 *     // Вариант 1: (())
 *     println("(())")
 *
 *     // Вариант 2: ()()
 *     println("()()")
 * }
 * ```
 *
 * Видишь? Всего 2 варианта для n=2.
 *
 * ## Теперь давай поймем рекурсию на ПРОСТОМ примере
 *
 * Рекурсия - это когда функция вызывает саму себя. Простой пример:
 *
 * ```kotlin
 * // Функция, которая считает от 1 до n
 * fun count(n: Int, current: Int = 1) {
 *     if (current > n) return  // остановка
 *     println(current)         // работа
 *     count(n, current + 1)   // рекурсивный вызов
 * }
 *
 * fun main() {
 *     count(3)  // выведет: 1 2 3
 * }
 * ```
 *
 * ## Теперь давай МЕДЛЕННО разберем скобочную задачу
 *
 * **Идея**: на каждом шаге мы можем:
 * 1. Добавить `(` - если еще не использовали все открывающие
 * 2. Добавить `)` - если закрывающих меньше чем открывающих
 *
 * ```kotlin
 * fun generate(current: String, open: Int, close: Int, n: Int) {
 *     // Если построили последовательность длины 2*n - выводим
 *     if (current.length == 2 * n) {
 *         println(current)
 *         return
 *     }
 *
 *     // Вариант 1: добавить открывающую скобку
 *     if (open < n) {
 *         generate("$current(", open + 1, close, n)
 *     }
 *
 *     // Вариант 2: добавить закрывающую скобку
 *     if (close < open) {
 *         generate("$current)", open, close + 1, n)
 *     }
 * }
 *
 * fun main() {
 *     val n = 2
 *     generate("", 0, 0, n)
 * }
 * ```
 *
 * ## Давай ПРОСЛЕДИМ выполнение для n=2
 *
 * ```
 * generate("", 0, 0, 2)
 * │
 * ├─ open=0 < 2? ДА → generate("(", 1, 0, 2)
 * │  │
 * │  ├─ open=1 < 2? ДА → generate("((", 2, 0, 2)
 * │  │  │
 * │  │  ├─ open=2 < 2? НЕТ
 * │  │  │
 * │  │  └─ close=0 < 2? ДА → generate("(()", 2, 1, 2)
 * │  │     │
 * │  │     ├─ open=2 < 2? НЕТ
 * │  │     │
 * │  │     └─ close=1 < 2? ДА → generate("(())", 2, 2, 2) ✓ ВЫВОД!
 * │  │        │
 * │  │        └─ длина = 4 → выводим "(())"
 * │  │
 * │  └─ close=0 < 1? ДА → generate("()", 1, 1, 2)
 * │     │
 * │     ├─ open=1 < 2? ДА → generate("()(", 2, 1, 2)
 * │     │  │
 * │     │  ├─ open=2 < 2? НЕТ
 * │     │  │
 * │     │  └─ close=1 < 2? ДА → generate("()()", 2, 2, 2) ✓ ВЫВОД!
 * │     │     │
 * │     │     └─ длина = 4 → выводим "()()"
 * │     │
 * │     └─ close=1 < 1? НЕТ
 * │
 * └─ close=0 < 0? НЕТ
 * ```
 *
 * ## Упростим еще больше - сделаем ВИЗУАЛИЗАЦИЮ
 *
 * ```kotlin
 * fun generate(current: String, open: Int, close: Int, n: Int, depth: Int = 0) {
 *     val indent = "  ".repeat(depth)
 *     println("${indent}generate('$current', open=$open, close=$close)")
 *
 *     if (current.length == 2 * n) {
 *         println("${indent}✓ НАШЛИ: $current")
 *         return
 *     }
 *
 *     if (open < n) {
 *         println("${indent}→ добавляем '('")
 *         generate("$current(", open + 1, close, n, depth + 1)
 *     }
 *
 *     if (close < open) {
 *         println("${indent}→ добавляем ')'")
 *         generate("$current)", open, close + 1, n, depth + 1)
 *     }
 * }
 *
 * fun main() {
 *     generate("", 0, 0, 2)
 * }
 * ```
 *
 * **Это выведет весь процесс построения!**
 *
 * ## Самый простой вариант для начала
 *
 * ```kotlin
 * fun main() {
 *     val n = 2
 *
 *     // Просто выведем готовые ответы для n=2
 *     if (n == 2) {
 *         println("(())")
 *         println("()()")
 *     }
 *
 *     // И для n=3
 *     if (n == 3) {
 *         println("((()))")
 *         println("(()())")
 *         println("(())()")
 *         println("()(())")
 *         println("()()()")
 *     }
 * }
 * ```
 *
 * ## Совет для понимания рекурсии:
 *
 * 1. **Сначала напиши решение для маленьких n вручную**
 * 2. **Потом попробуй понять КАК ты это делаешь вручную**
 * 3. **Затем переведи свои действия в код**
 *
 * Рекурсия - это просто формализация твоих мысленных действий!
 *
 * Хочешь, мы можем сначала порешать более простые задачи на рекурсию? Например, генерацию всех чисел от 1 до n, или подсчет факториала? Это поможет понять концепцию постепенно!
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

    //--- Упростим еще больше - сделаем ВИЗУАЛИЗАЦИЮ
    generate("", 0, 0, 2)
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

fun generate(current: String, open: Int, close: Int, n: Int, depth: Int = 0) {
    val indent = "  ".repeat(depth)
    println("${indent}generate('$current', open=$open, close=$close)")

    if (current.length == 2 * n) {
        println("${indent}✓ НАШЛИ: $current")
        return
    }

    if (open < n) {
        println("${indent}→ добавляем '('")
        generate("$current(", open + 1, close, n, depth + 1)
    }

    if (close < open) {
        println("${indent}→ добавляем ')'")
        generate("$current)", open, close + 1, n, depth + 1)
    }
}