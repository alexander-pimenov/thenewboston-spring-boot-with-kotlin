package tv.codealong.tutorials.various.yandex.test1_1_introductory_contest

/**
 * ```
 * Не секрет, что некоторые программисты очень любят путешествовать. Хорошо всем известный программист Петя тоже очень любит путешествовать, посещать музеи и осматривать достопримечательности других городов.
 *
 * Для перемещений между из города в город он предпочитает использовать машину. При этом он заправляется только на станциях в городах, но не на станциях по пути. Поэтому он очень аккуратно выбирает маршруты, чтобы машина не заглохла в дороге. А ещё Петя очень важный член команды, поэтому он не может себе позволить путешествовать слишком долго. Он решил написать программу, которая поможет ему с выбором очередного путешествия. Но так как сейчас у него слишком много других задач, он попросил вас помочь ему.
 *
 * Расстояние между двумя городами считается как сумма модулей разности по каждой из координат. Дороги есть между всеми парами городов.
 *
 * Формат ввода
 * В первой строке входных данных записано количество городов
 * n
 * n (
 * 2
 * ≤
 * n
 * ≤
 * 1000
 * 2≤n≤1000). В следующих
 * n
 * n строках даны два целых числа: координаты каждого города, не превосходящие по модулю миллиарда. Все города пронумерованы числами от 1 до
 * n
 * n в порядке записи во входных данных.
 *
 * В следующей строке записано целое положительное число
 * k
 * k, не превосходящее двух миллиардов, — максимальное расстояние между городами, которое Петя может преодолеть без дозаправки машины.
 *
 * В последней строке записаны два различных числа — номер города, откуда едет Петя, и номер города, куда он едет.
 *
 * Формат вывода
 * Если существуют пути, удовлетворяющие описанным выше условиям, то выведите минимальное количество дорог, которое нужно проехать,
 * чтобы попасть из начальной точки маршрута в конечную. Если пути не существует, выведите -1.
 *
 * Пример 1
 * Ввод
 * 7
 * 0 0
 * 0 2
 * 2 2
 * 0 -2
 * 2 -2
 * 2 -1
 * 2 1
 * 2
 * 1 3
 * Вывод
 * 2
 * Пример 2
 * Ввод
 * 4
 * 0 0
 * 1 0
 * 0 1
 * 1 1
 * 2
 * 1 4
 * Вывод
 * 1
 * Пример 3
 * Ввод
 * 4
 * 0 0
 * 2 0
 * 0 2
 * 2 2
 * 1
 * 1 4
 * Вывод
 * -1
 * ```
 * Да, эта задача действительно сложнее! Это задача на **поиск кратчайшего пути в графе**. Давай разберем ее по шагам.
 *
 * ## Анализ задачи
 *
 * У нас есть:
 * - **n** городов с координатами (x, y)
 * - **Расстояние** между городами: `|x1 - x2| + |y1 - y2|` (манхэттенское расстояние)
 * - **k** - максимальное расстояние, которое можно проехать без дозаправки
 * - Нужно найти **минимальное количество дорог** от старта до финиша
 *
 * ## Ключевые моменты:
 *
 * 1. **Граф полный** - дороги есть между всеми парами городов
 * 2. **Ребро существует** только если расстояние ≤ k
 * 3. **Вес всех ребер = 1** (нас интересует количество дорог, а не расстояние)
 * 4. Нужен **кратчайший путь** в таком невзвешенном графе
 *
 * ## Решение: BFS (поиск в ширину)
 *
 * BFS идеально подходит, так как:
 * - Граф невзвешенный (все ребра имеют вес 1)
 * - BFS находит кратчайший путь в невзвешенном графе
 *
 * ```kotlin
 * import java.util.*
 *
 * fun main() {
 *     // Чтение входных данных
 *     val n = readLine()!!.toInt()
 *     val cities = Array(n + 1) { Pair(0L, 0L) } // индексация с 1
 *
 *     for (i in 1..n) {
 *         val (x, y) = readLine()!!.split(" ").map { it.toLong() }
 *         cities[i] = Pair(x, y)
 *     }
 *
 *     val k = readLine()!!.toLong()
 *     val (start, end) = readLine()!!.split(" ").map { it.toInt() }
 *
 *     // Вычисляем расстояния между всеми парами городов
 *     val distances = Array(n + 1) { LongArray(n + 1) }
 *     for (i in 1..n) {
 *         for (j in 1..n) {
 *             if (i != j) {
 *                 val (x1, y1) = cities[i]
 *                 val (x2, y2) = cities[j]
 *                 distances[i][j] = Math.abs(x1 - x2) + Math.abs(y1 - y2)
 *             }
 *         }
 *     }
 *
 *     // BFS для поиска кратчайшего пути
 *     val visited = BooleanArray(n + 1)
 *     val queue: Queue<Pair<Int, Int>> = LinkedList() // (город, количество дорог)
 *     queue.add(Pair(start, 0))
 *     visited[start] = true
 *
 *     var result = -1
 *
 *     while (queue.isNotEmpty()) {
 *         val (current, roads) = queue.poll()
 *
 *         // Если дошли до конечного города
 *         if (current == end) {
 *             result = roads
 *             break
 *         }
 *
 *         // Проверяем всех соседей
 *         for (next in 1..n) {
 *             if (!visited[next] && distances[current][next] <= k) {
 *                 visited[next] = true
 *                 queue.add(Pair(next, roads + 1))
 *             }
 *         }
 *     }
 *
 *     println(result)
 * }
 * ```
 *
 * ## Более эффективная версия (без предвычисления всех расстояний):
 *
 * ```kotlin
 * import java.util.*
 *
 * fun main() {
 *     val n = readLine()!!.toInt()
 *     val cities = Array(n + 1) { Pair(0L, 0L) }
 *
 *     for (i in 1..n) {
 *         val (x, y) = readLine()!!.split(" ").map { it.toLong() }
 *         cities[i] = Pair(x, y)
 *     }
 *
 *     val k = readLine()!!.toLong()
 *     val (start, end) = readLine()!!.split(" ").map { it.toInt() }
 *
 *     // BFS
 *     val visited = BooleanArray(n + 1)
 *     val queue: Queue<Pair<Int, Int>> = LinkedList()
 *     queue.add(Pair(start, 0))
 *     visited[start] = true
 *
 *     var result = -1
 *
 *     while (queue.isNotEmpty()) {
 *         val (current, roads) = queue.poll()
 *
 *         if (current == end) {
 *             result = roads
 *             break
 *         }
 *
 *         val (x1, y1) = cities[current]
 *
 *         // Проверяем все города как возможных соседей
 *         for (next in 1..n) {
 *             if (!visited[next]) {
 *                 val (x2, y2) = cities[next]
 *                 val distance = Math.abs(x1 - x2) + Math.abs(y1 - y2)
 *
 *                 if (distance <= k) {
 *                     visited[next] = true
 *                     queue.add(Pair(next, roads + 1))
 *                 }
 *             }
 *         }
 *     }
 *
 *     println(result)
 * }
 * ```
 *
 * ## Разберем на примерах:
 *
 * ### Пример 1:
 * ```
 * Города:
 * 1: (0,0)
 * 2: (0,2)
 * 3: (2,2)
 * 4: (0,-2)
 * 5: (2,-2)
 * 6: (2,-1)
 * 7: (2,1)
 * k = 2
 * start = 1, end = 3
 *
 * Расстояния ≤ 2:
 * 1-2: |0-0| + |0-2| = 2 ✓
 * 2-3: |0-2| + |2-2| = 2 ✓
 * 1-3: |0-2| + |0-2| = 4 ✗
 *
 * Путь: 1 → 2 → 3 (2 дороги)
 * ```
 *
 * ### Пример 2:
 * ```
 * Города:
 * 1: (0,0)
 * 2: (1,0)
 * 3: (0,1)
 * 4: (1,1)
 * k = 2
 * start = 1, end = 4
 *
 * Расстояния:
 * 1-2: 1 ✓, 1-3: 1 ✓, 1-4: 2 ✓
 * Прямой путь: 1 → 4 (1 дорога)
 * ```
 *
 * ### Пример 3:
 * ```
 * Города:
 * 1: (0,0)
 * 2: (2,0)
 * 3: (0,2)
 * 4: (2,2)
 * k = 1
 * start = 1, end = 4
 *
 * Все расстояния = 4 ✗
 * Пути нет → -1
 * ```
 *
 * ## Сложность:
 *
 * - **Время**: O(n²) - в худшем случае проверяем все пары городов
 * - **Память**: O(n) - для visited массива и очереди
 *
 * ## Почему BFS, а не Dijkstra?
 *
 * - Все ребра имеют **вес 1**
 * - В невзвешенном графе BFS находит кратчайший путь быстрее
 * - BFS проще в реализации
 *
 * ## Улучшенная версия с проверкой на прямой путь:
 *
 * ```kotlin
 * import java.util.*
 *
 * fun main() {
 *     val n = readLine()!!.toInt()
 *     val cities = Array(n + 1) { Pair(0L, 0L) }
 *
 *     for (i in 1..n) {
 *         val (x, y) = readLine()!!.split(" ").map { it.toLong() }
 *         cities[i] = Pair(x, y)
 *     }
 *
 *     val k = readLine()!!.toLong()
 *     val (start, end) = readLine()!!.split(" ").map { it.toInt() }
 *
 *     // Проверка прямого пути
 *     val (x1, y1) = cities[start]
 *     val (x2, y2) = cities[end]
 *     val directDistance = Math.abs(x1 - x2) + Math.abs(y1 - y2)
 *
 *     if (directDistance <= k) {
 *         println(1)
 *         return
 *     }
 *
 *     // BFS
 *     val visited = BooleanArray(n + 1)
 *     val queue: Queue<Pair<Int, Int>> = LinkedList()
 *     queue.add(Pair(start, 0))
 *     visited[start] = true
 *
 *     var result = -1
 *
 *     while (queue.isNotEmpty()) {
 *         val (current, roads) = queue.poll()
 *
 *         if (current == end) {
 *             result = roads
 *             break
 *         }
 *
 *         val (xCur, yCur) = cities[current]
 *
 *         for (next in 1..n) {
 *             if (!visited[next]) {
 *                 val (xNext, yNext) = cities[next]
 *                 val distance = Math.abs(xCur - xNext) + Math.abs(yCur - yNext)
 *
 *                 if (distance <= k) {
 *                     visited[next] = true
 *                     queue.add(Pair(next, roads + 1))
 *                 }
 *             }
 *         }
 *     }
 *
 *     println(result)
 * }
 * ```
 *
 * Это решение должно пройти все тесты! Как тебе объяснение? 😊
 *
 * Отличные вопросы! Давай разберем все эти понятия **на простых примерах**, как будто объясняю другу за чашкой кофе ☕
 *
 * ## 1. Что такое "граф"?
 *
 * **Граф** - это просто набор "точек" (городов) и "линий" между ними (дорог).
 *
 * Представь карту городов:
 * - **Города** = точки (вершины графа)
 * - **Дороги** = линии между городами (ребра графа)
 *
 * ```
 * Города: A, B, C, D
 *
 * A --- B
 * |     |
 * C --- D
 * ```
 *
 * ## 2. "Полный граф"
 *
 * **Полный граф** - это когда между КАЖДОЙ парой городов есть прямая дорога.
 *
 * ```
 * A ——— B
 * | ╲ ╱ |
 * |  X  |
 * | ╱ ╲ |
 * C ——— D
 * ```
 *
 * В нашей задаче: "Дороги есть между всеми парами городов" = полный граф.
 *
 * ## 3. "Ребро графа" и "Вес ребра"
 *
 * - **Ребро** = дорога между двумя городами
 * - **Вес ребра** = "стоимость" проезда по этой дороге
 *
 * **Пример:**
 * ```
 * A --5-- B --3-- C
 * ```
 * - Ребро A-B имеет вес 5 (расстояние = 5 км)
 * - Ребро B-C имеет вес 3 (расстояние = 3 км)
 *
 * ## 4. "Невзвешенный граф" vs "Взвешенный граф"
 *
 * **Взвешенный граф** (как карта с расстояниями):
 * ```
 * A --5-- B --3-- C
 * ```
 * Нас интересует СУММА расстояний: A→C = 5 + 3 = 8
 *
 * **Невзвешенный граф** (как схема метро):
 * ```
 * A — B — C
 * ```
 * Нас интересует только КОЛИЧЕСТВО перегонов: A→C = 2 перегона
 *
 * **В нашей задаче**: нам нужно КОЛИЧЕСТВО дорог, а не сумма расстояний → невзвешенный граф!
 *
 * ## 5. "Манхэттенское расстояние"
 *
 * Представь, что ты в Нью-Йорке и можешь ходить только по улицам (не через дома):
 *
 * ```
 * Точка A: (2, 3)    // 2-я авеню, 3-я улица
 * Точка B: (5, 7)    // 5-я авеню, 7-я улица
 *
 * Расстояние = |2-5| + |3-7| = 3 + 4 = 7 кварталов
 * ```
 *
 * **Формула:** `|x1 - x2| + |y1 - y2|`
 *
 * ## 6. Что такое BFS? (Поиск в ширину)
 *
 * BFS - это как "волна", которая расходится от стартовой точки.
 *
 * **Аналогия**: представь, что ты разливаешь воду:
 * 1. Вода начинает течь из точки A
 * 2. Она заполняет все соседние точки (B, C, D)
 * 3. Потом из этих точек вода течет дальше
 * 4. Как только вода дошла до точки Z - STOP!
 *
 * **Пример:**
 * ```
 *     1
 *    / \
 *   2   3
 *  / \   \
 * 4   5   6
 * ```
 *
 * BFS из точки 1:
 * - Шаг 0: [1]
 * - Шаг 1: [2, 3]
 * - Шаг 2: [4, 5, 6]
 *
 * ## 7. Теперь пересобираем нашу задачу:
 *
 * **Упрощенная версия задачи:**
 * ```kotlin
 * fun main() {
 *     // 1. Читаем города и их координаты
 *     val cities = listOf(
 *         Pair(0, 0),  // город 1
 *         Pair(0, 2),  // город 2
 *         Pair(2, 2)   // город 3
 *     )
 *
 *     val k = 2  // максимум 2 км без дозаправки
 *
 *     // 2. Строим "дорожную карту":
 *     // Город1-Город2: |0-0| + |0-2| = 2 ✓ (можно ехать)
 *     // Город1-Город3: |0-2| + |0-2| = 4 ✗ (нельзя - слишком далеко)
 *     // Город2-Город3: |0-2| + |2-2| = 2 ✓ (можно ехать)
 *
 *     // 3. Ищем путь из города 1 в город 3:
 *     // Прямого пути нет (4 > 2)
 *     // Но есть через город 2: 1 → 2 → 3 (2 дороги)
 *
 *     println(2) // ответ
 * }
 * ```
 *
 * ## 8. Упрощенный BFS на примере:
 *
 * ```kotlin
 * // Упрощенный пример BFS
 * fun findShortestPath() {
 *     // Граф: 1-2, 2-3 (как в задаче выше)
 *     val graph = mapOf(
 *         1 to listOf(2),    // из 1 можно в 2
 *         2 to listOf(1, 3), // из 2 можно в 1 и 3
 *         3 to listOf(2)     // из 3 можно в 2
 *     )
 *
 *     val start = 1
 *     val end = 3
 *
 *     // BFS волна:
 *     // Шаг 0: [1] (расстояние 0)
 *     // Шаг 1: [2] (расстояние 1)
 *     // Шаг 2: [3] (расстояние 2) - нашли!
 * }
 * ```
 *
 * ## 9. Полная аналогия с нашей задачей:
 *
 * **Петя-путешественник:**
 * 1. **Города** = вершины графа
 * 2. **Дороги** = ребра графа (только если расстояние ≤ k)
 * 3. **BFS** = Петя смотрит сначала на ближайшие города, потом на те, что подальше
 * 4. **Результат** = на каком "шаге" BFS он доехал до цели
 *
 * ## Самый простой код для понимания:
 *
 * ```kotlin
 * fun main() {
 *     // Просто считаем, что можем ехать между городами
 *     // если расстояние между ними ≤ k
 *
 *     // Если можем проехать напрямую - ответ 1
 *     // Если нет - ищем через другие города
 *
 *     // Это и есть BFS! :)
 * }
 * ```
 *
 * Теперь понятнее? Могу объяснить еще какую-то часть подробнее! 😊
 * Отличная идея! Запустить код и посмотреть как он работает - лучший способ разобраться. Давай сделаем версию для работы с файлами.
 *
 * ## Версия для чтения из файла `input.txt`
 *
 * ```kotlin
 * import java.util.*
 * import java.io.File
 *
 * fun main() {
 *     // Читаем все строки из файла
 *     val lines = File("input.txt").readLines()
 *
 *     var index = 0
 *     val n = lines[index++].toInt()
 *     val cities = Array(n + 1) { Pair(0L, 0L) }
 *
 *     for (i in 1..n) {
 *         val (x, y) = lines[index++].split(" ").map { it.toLong() }
 *         cities[i] = Pair(x, y)
 *     }
 *
 *     val k = lines[index++].toLong()
 *     val (start, end) = lines[index].split(" ").map { it.toInt() }
 *
 *     // Проверка прямого пути
 *     val (x1, y1) = cities[start]
 *     val (x2, y2) = cities[end]
 *     val directDistance = Math.abs(x1 - x2) + Math.abs(y1 - y2)
 *
 *     if (directDistance <= k) {
 *         println(1)
 *         return
 *     }
 *
 *     // BFS
 *     val visited = BooleanArray(n + 1)
 *     val queue: Queue<Pair<Int, Int>> = LinkedList()
 *     queue.add(Pair(start, 0))
 *     visited[start] = true
 *
 *     var result = -1
 *
 *     while (queue.isNotEmpty()) {
 *         val (current, roads) = queue.poll()
 *
 *         if (current == end) {
 *             result = roads
 *             break
 *         }
 *
 *         val (xCur, yCur) = cities[current]
 *
 *         for (next in 1..n) {
 *             if (!visited[next]) {
 *                 val (xNext, yNext) = cities[next]
 *                 val distance = Math.abs(xCur - xNext) + Math.abs(yCur - yNext)
 *
 *                 if (distance <= k) {
 *                     visited[next] = true
 *                     queue.add(Pair(next, roads + 1))
 *                 }
 *             }
 *         }
 *     }
 *
 *     println(result)
 * }
 * ```
 *
 * ## Создаем тестовые файлы:
 *
 * ### Файл `input1.txt` (Пример 1):
 * ```
 * 7
 * 0 0
 * 0 2
 * 2 2
 * 0 -2
 * 2 -2
 * 2 -1
 * 2 1
 * 2
 * 1 3
 * ```
 *
 * ### Файл `input2.txt` (Пример 2):
 * ```
 * 4
 * 0 0
 * 1 0
 * 0 1
 * 1 1
 * 2
 * 1 4
 * ```
 *
 * ### Файл `input3.txt` (Пример 3):
 * ```
 * 4
 * 0 0
 * 2 0
 * 0 2
 * 2 2
 * 1
 * 1 4
 * ```
 *
 * ## Универсальная версия (работает и с файлом и с консолью):
 *
 * ```kotlin
 * import java.util.*
 * import java.io.File
 *
 * fun main() {
 *     val lines = if (File("input.txt").exists()) {
 *         File("input.txt").readLines()
 *     } else {
 *         generateSequence { readLine() }.toList()
 *     }
 *
 *     var index = 0
 *     val n = lines[index++].toInt()
 *     val cities = Array(n + 1) { Pair(0L, 0L) }
 *
 *     for (i in 1..n) {
 *         val (x, y) = lines[index++].split(" ").map { it.toLong() }
 *         cities[i] = Pair(x, y)
 *     }
 *
 *     val k = lines[index++].toLong()
 *     val (start, end) = lines[index].split(" ").map { it.toInt() }
 *
 *     // Проверка прямого пути
 *     val (x1, y1) = cities[start]
 *     val (x2, y2) = cities[end]
 *     val directDistance = Math.abs(x1 - x2) + Math.abs(y1 - y2)
 *
 *     if (directDistance <= k) {
 *         println(1)
 *         return
 *     }
 *
 *     // BFS
 *     val visited = BooleanArray(n + 1)
 *     val queue: Queue<Pair<Int, Int>> = LinkedList()
 *     queue.add(Pair(start, 0))
 *     visited[start] = true
 *
 *     var result = -1
 *
 *     while (queue.isNotEmpty()) {
 *         val (current, roads) = queue.poll()
 *
 *         if (current == end) {
 *             result = roads
 *             break
 *         }
 *
 *         val (xCur, yCur) = cities[current]
 *
 *         for (next in 1..n) {
 *             if (!visited[next]) {
 *                 val (xNext, yNext) = cities[next]
 *                 val distance = Math.abs(xCur - xNext) + Math.abs(yCur - yNext)
 *
 *                 if (distance <= k) {
 *                     visited[next] = true
 *                     queue.add(Pair(next, roads + 1))
 *                 }
 *             }
 *         }
 *     }
 *
 *     println(result)
 * }
 * ```
 *
 * ## Версия с выводом в файл:
 *
 * ```kotlin
 * import java.util.*
 * import java.io.File
 *
 * fun main() {
 *     val inputLines = File("input.txt").readLines()
 *
 *     var index = 0
 *     val n = inputLines[index++].toInt()
 *     val cities = Array(n + 1) { Pair(0L, 0L) }
 *
 *     for (i in 1..n) {
 *         val (x, y) = inputLines[index++].split(" ").map { it.toLong() }
 *         cities[i] = Pair(x, y)
 *     }
 *
 *     val k = inputLines[index++].toLong()
 *     val (start, end) = inputLines[index].split(" ").map { it.toInt() }
 *
 *     // Проверка прямого пути
 *     val (x1, y1) = cities[start]
 *     val (x2, y2) = cities[end]
 *     val directDistance = Math.abs(x1 - x2) + Math.abs(y1 - y2)
 *
 *     if (directDistance <= k) {
 *         File("output.txt").writeText("1")
 *         return
 *     }
 *
 *     // BFS
 *     val visited = BooleanArray(n + 1)
 *     val queue: Queue<Pair<Int, Int>> = LinkedList()
 *     queue.add(Pair(start, 0))
 *     visited[start] = true
 *
 *     var result = -1
 *
 *     while (queue.isNotEmpty()) {
 *         val (current, roads) = queue.poll()
 *
 *         if (current == end) {
 *             result = roads
 *             break
 *         }
 *
 *         val (xCur, yCur) = cities[current]
 *
 *         for (next in 1..n) {
 *             if (!visited[next]) {
 *                 val (xNext, yNext) = cities[next]
 *                 val distance = Math.abs(xCur - xNext) + Math.abs(yCur - yNext)
 *
 *                 if (distance <= k) {
 *                     visited[next] = true
 *                     queue.add(Pair(next, roads + 1))
 *                 }
 *             }
 *         }
 *     }
 *
 *     File("output.txt").writeText(result.toString())
 * }
 * ```
 *
 * ## Как запустить:
 *
 * 1. **Создай файл `input.txt`** с тестовыми данными
 * 2. **Запусти программу**
 * 3. **Результат появится в консоли** или в файле `output.txt`
 *
 * ## Пример запуска для теста:
 *
 * **Создай `input.txt`:**
 * ```
 * 4
 * 0 0
 * 1 0
 * 0 1
 * 1 1
 * 2
 * 1 4
 * ```
 *
 * **Запусти программу → получишь в выводе:**
 * ```
 * 1
 * ```
 *
 * Теперь можешь экспериментировать с разными входными данными и смотреть как меняется результат! 😊
 *
 * Хочешь, я помогу разобрать выполнение программы пошагово на конкретном примере?
 *
 *
 * k - это максимальное расстояние между городами, которое можно проехать без дозаправки машины.
 */

import java.io.File
import java.util.*
import kotlin.math.abs

fun main() {

    exampleReadFromFile()

}

fun exampleReadFromConsole() {
    val n = readLine()!!.toInt()
    val cities = Array(n + 1) { Pair(0L, 0L) }

    for (i in 1..n) {
        val (x, y) = readLine()!!.split(" ").map { it.toLong() }
        cities[i] = Pair(x, y)
        println("Зарегистрирован город [$i] с координатами ($x, $y)")
    }

    val k = readLine()!!.toLong()
    val (start, end) = readLine()!!.split(" ").map { it.toInt() }
    println("Определено максимальное расстояние между городами, которое можно проехать без дозаправки машины к=$k")

    // Проверка прямого пути
    val (x1, y1) = cities[start]
    val (x2, y2) = cities[end]
    val directDistance = abs(x1 - x2) + abs(y1 - y2)

    if (directDistance <= k) {
        println(1)
        return
    }

    // BFS
    val visited = BooleanArray(n + 1)
    val queue: Queue<Pair<Int, Int>> = LinkedList()
    queue.add(Pair(start, 0))
    visited[start] = true

    var result = -1

    while (queue.isNotEmpty()) {
        val (current, roads) = queue.poll()

        if (current == end) {
            result = roads
            break
        }

        val (xCur, yCur) = cities[current]

        for (next in 1..n) {
            if (!visited[next]) {
                val (xNext, yNext) = cities[next]
                val distance = abs(xCur - xNext) + abs(yCur - yNext)

                if (distance <= k) {
                    visited[next] = true
                    queue.add(Pair(next, roads + 1))
                }
            }
        }
    }

    println(result)
}

fun exampleReadFromFile() {
    //    val lines = File("src/test/resources/inputCoordCities.txt").readLines()
//    val n = lines[0].toInt() // в файле на первой строке указано количество городов/элементов в массиве
//    if (n == 0) return
    // Читаем все строки из файла
    val lines = File("src/test/resources/inputCoordCities3.txt").readLines()

    var index = 0
    val n = lines[index++].toInt()
    val cities = Array(n + 1) { Pair(0L, 0L) }

    for (i in 1..n) {
        val (x, y) = lines[index++].split(" ").map { it.toLong() }
        cities[i] = Pair(x, y)
        println("Зарегистрирован город [$i] с координатами ($x, $y)")
    }

    val k = lines[index++].toLong()
    println("Определено максимальное расстояние между городами, которое можно проехать без дозаправки машины к=$k")
    val (start, end) = lines[index].split(" ").map { it.toInt() }

    // Проверка прямого пути
    val (x1, y1) = cities[start]
    val (x2, y2) = cities[end]
    val directDistance = abs(x1 - x2) + abs(y1 - y2)
    println("Необходимо вычислить расстояние/переходы между городами [$start] и [$end]")

    if (directDistance <= k) {
        println(1)
        return
    }

    // BFS
    val visited = BooleanArray(n + 1)
    val queue: Queue<Pair<Int, Int>> = LinkedList()
    queue.add(Pair(start, 0))
    visited[start] = true

    var result = -1

    while (queue.isNotEmpty()) {
        val (current, roads) = queue.poll()

        if (current == end) {
            result = roads
            break
        }

        val (xCur, yCur) = cities[current]

        for (next in 1..n) {
            if (!visited[next]) {
                val (xNext, yNext) = cities[next]
                val distance = Math.abs(xCur - xNext) + Math.abs(yCur - yNext)

                if (distance <= k) {
                    visited[next] = true
                    queue.add(Pair(next, roads + 1))
                }
            }
        }
    }

    println(result)
}