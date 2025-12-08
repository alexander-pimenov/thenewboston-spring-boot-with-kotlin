package tv.codealong.tutorials.various.sequenceAndStream

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * ⚡ Ключевое отличие: Lazy vs Eager Evaluation
 * Stream - ленивые операции, но создает новый Stream на каждом шаге
 * Sequence - полностью ленивые вычисления, обрабатывает элементы по одному
 *
 * 🚀 Полезные методы для группировки
 * Kotlin методы:
 * groupBy() - базовая группировка
 * groupingBy() - для последующей агрегации
 * eachCount() - подсчет количества
 * fold() / reduce() - агрегация
 * aggregate() - кастомная агрегация
 */
class SequenceAndStreamKtTest {
    @Test
    fun `classic test`() {
        val list = listOf("aaa1", "aa2", "bbb1", "cc2", "ccc1")
        val result = list.asSequence()
            .filter { it.length > 3 }
            .map { it.uppercase() }
            .toList()

        println(result)
    }

    @Test
    @DisplayName("1. Порядок выполнения операций. Sequence (полностью ленивая обработка)")
    fun `lazy evaluation test`() {
        listOf("a1", "a2", "b1", "c2", "c1")
            .asSequence()
            .filter {
                println("stage filter: $it")
                it.startsWith("c")
            }
            .map {
                println("stage map: $it")
                it.uppercase()
            }
            .forEach {
                println("stage forEach: $it")
            }
        //stage filter: a1
        //stage filter: a2
        //stage filter: b1
        //stage filter: c2
        //stage map: c2
        //stage forEach: C2
        //stage filter: c1
        //stage map: c1
        //stage forEach: C1
    }

    @Test
    @DisplayName("2. Производительность на больших данных")
    fun `performance test`() {
        val list = (1..1_000_000)
            .filter { it % 2 == 0 }     // создается List ~500,000 элементов
            .map { it * 2 }             // создается еще один List
            .take(10)                   // берем только 10
            .toList()
        println(list)
        //[4, 8, 12, 16, 20, 24, 28, 32, 36, 40]

        // С Sequence - обрабатываются только нужные элементы
        val result2 = (1..1_000_000)
            .asSequence()
            .filter { it % 2 == 0 }     // проверяем до нахождения 10 элементов
            .map { it * 2 }
            .take(10)                   // берем 10 и останавливаемся
            .toList()
        println(result2)
        //[4, 8, 12, 16, 20, 24, 28, 32, 36, 40]
    }

    @Test
    @DisplayName("Пример 1: Поиск первого подходящего элемента")
    fun `find first test`() {
        // Без sequence - все операции выполняются на всех элементах
        val result1 = users
            .filter {
                println("Filtering ${it.name}")
                it.age > 30
            }
            .map {
                println("Mapping ${it.name}")
                it.name
            }
            .firstOrNull()
        println(result1)
        //Filtering Alice
        //Filtering Bob
        //Filtering Charlie
        //Filtering David
        //Mapping Charlie
        //Mapping David
        //Charlie

        // С sequence - операции выполняются только до нахождения результата
        val result2 = users.asSequence()
            .filter {
                println("Filtering ${it.name}")
                it.age > 30
            }
            .map {
                println("Mapping ${it.name}")
                it.name
            }
            .firstOrNull()
        println(result2)
        //Filtering Alice
        //Filtering Bob
        //Filtering Charlie
        //Mapping Charlie
        //Charlie
    }

    @Test
    @DisplayName("Пример 2: Бесконечные последовательности")
    fun `infinite sequence test`() {
        val result = infiniteSequence
            .filter { it % 2 == 0 }
            .map { it * it }
            .take(5)
            .toList()
        println(result) //[4, 16, 36, 64, 100]
    }

    @Test
    @DisplayName("Пример 3: Напишите код, который демонстрирует ленивость Sequence")
    fun `lazy sequence test`() {
        val numbers = (1..10).asSequence()

        val result = numbers
            .map {
                println("#map: Mapping $it")
                it * 2
            }
            .filter {
                println("#filter: Filtering $it")
                it > 5
            }
            .first()
        println(result)
        //#map: Mapping 1
        //#filter: Filtering 2
        //#map: Mapping 2
        //#filter: Filtering 4
        //#map: Mapping 3
        //#filter: Filtering 6
        //6
    }

    @Test
    @DisplayName("1. Простая группировка по отделу")
    fun testSimpleGrouping4() {
        val byDeptSequence = employees.asSequence()
            .groupBy(Employee::department)
        println(byDeptSequence)

        val byDeptSequence2 = employees
            .groupBy { it.department }

        println(byDeptSequence)
        println(byDeptSequence2)
        //{
        // Engineering=[
        //      Employee(name=Alice, position=, department=Engineering, salary=5000),
        //      Employee(name=Bob, position=, department=Engineering, salary=6000),
        //      Employee(name=Eve, position=, department=Engineering, salary=5500)
        //      ],
        // Marketing=[
        //      Employee(name=Charlie, position=, department=Marketing, salary=4500),
        //      Employee(name=David, position=, department=Marketing, salary=4000)
        //      ]
        //}
    }

    @Test
    @DisplayName("2. Группировка с подсчетом количества")
    fun testSimpleGrouping5() {
        val countByDept = employees
            .groupBy { it.department }
            .mapValues { it.value.size }
        println(countByDept)
        //{Engineering=3, Marketing=2}

        // Группировка имен сотрудников по отделам
        val namesByDept = employees
            .groupBy(
                keySelector = { it.department },
                valueTransform = { it.name }

            )
        println(namesByDept)
        //{Engineering=[Alice, Bob, Eve], Marketing=[Charlie, David]}

        // Группировка зарплат
        val salaryByDept = employees
            .groupBy(
                keySelector = { it.department },
                valueTransform = { it.salary }
            )

        val salaryByDept2 = employees
            .groupBy { it.department }
            .mapValues {
                it.value.map { employee ->
                    employee.salary
                }
            }

        println(salaryByDept)
        println(salaryByDept2)
        //{Engineering=[5000, 6000, 5500], Marketing=[4500, 4000]}
    }

    @Test
    @DisplayName("3. Группировка с агрегацией. Вариант 1")
    fun testSimpleGrouping6() {
        // Средняя зарплата по отделам
        val avgSalaryByDept = employees
            .groupBy { it.department }
            .mapValues {
                it.value.sumOf { employee -> employee.salary } / it.value.size.toDouble() //.average {}
            }
        println(avgSalaryByDept)
        //{Engineering=5500.0, Marketing=4250.0}

        // Сумма зарплат по отделам
        val sumSalaryByDept = employees
            .groupBy { it.department }
            .mapValues {
                it.value.sumOf { employee ->
                    employee.salary
                }
            }
        println(sumSalaryByDept)
        //{Engineering=16500, Marketing=8500}

        // Минимальная зарплата по отделам
        val minSalaryByDept = employees
            .groupBy { it.department }
            .mapValues {
                it.value.minByOrNull { employee ->
                    employee.salary
                }
            }
        println(minSalaryByDept)
        //{
        // Engineering=Optional[
        //      Employee(name=Alice, position=, department=Engineering, salary=5000)
        //      ],
        // Marketing=Optional[
        //      Employee(name=David, position=, department=Marketing, salary=4000)
        //      ]
        //}
    }

    @Test
    @DisplayName("3. Группировка с агрегацией. Вариант advanced")
    fun testAdvancedGrouping() {
        // Группировка с агрегацией - статистика по департаментам
        val statsByDepartment = employees.groupingBy { it.department }
            .aggregate { key, accumulator: EmployeeStats?, employee, first ->
                if (first) { //когда первый раз встречается элемент
                    EmployeeStats(employee.salary, employee.salary, employee.salary, 1)

                } else {
                    //удобно создаем копию объекта и наполняем её (т.к. не можем модифицировать объект)
                    accumulator!!.copy(
                        minSalary = minOf(accumulator.minSalary, employee.salary),
                        maxSalary = maxOf(accumulator.maxSalary, employee.salary),
                        totalSalary = accumulator.totalSalary + employee.salary,
                        count = accumulator.count + 1
                    )
                }
            }
        println(statsByDepartment)
        // {
        // Engineering=EmployeeStats(minSalary=5000, maxSalary=6000, totalSalary=16500, count=3),
        // Marketing=EmployeeStats(minSalary=4000, maxSalary=4500, totalSalary=8500, count=2)
        // }
    }

    @Test
    @DisplayName("4. Многоуровневая группировка")
    fun testMultiLevelGrouping() {
        // Группировка по отделу, а затем по диапазону зарплат
        val multiLevel: Map<String, List<Pair<Employee, String>>> = employees.groupBy(
            keySelector = { it.department }, //ключ - департамент
            valueTransform = {
                it to (if (it.salary < 4500) "LOW"
                else if (it.salary < 5500) "MEDIUM"
                else "HIGH")
            } // в качестве значения - возвращаем список пар с объектом и диапазоном: List<Pair<Employee, String>>
        )

        println(multiLevel)
        //{Engineering=[(Employee(name=Alice, position=, department=Engineering, salary=5000), MEDIUM), (Employee(name=Bob, position=, department=Engineering, salary=6000), HIGH), (Employee(name=Eve, position=, department=Engineering, salary=5500), HIGH)], Marketing=[(Employee(name=Charlie, position=, department=Marketing, salary=4500), MEDIUM), (Employee(name=David, position=, department=Marketing, salary=4000), LOW)]}

        val multiLevel2 = employees.groupBy(
            keySelector = { it.department },
            valueTransform = {
                it to (if (it.salary < 4500) "LOW"
                else if (it.salary < 5500) "MEDIUM"
                else "HIGH")
            }
        ).mapValues { (_, values) ->
            // values - это список пар с объектом и диапазоном, после очередной группировки это будет Map
            // с ключом - диапазоном зарплат ("LOW"; "MEDIUM"; "HIGH"), а значением - список объектов Employee
            values.groupBy { it.second }
                .mapValues {
                    it.value.map { pair ->
                        pair.first
                    }
                }
        }

        println(multiLevel2)
        // {
        // Engineering={
        //   MEDIUM=[Employee(name=Alice, position=, department=Engineering, salary=5000)],
        //   HIGH=[Employee(name=Bob, position=, department=Engineering, salary=6000), Employee(name=Eve, position=, department=Engineering, salary=5500)]},
        // Marketing={
        //   MEDIUM=[Employee(name=Charlie, position=, department=Marketing, salary=4500)],
        //   LOW=[Employee(name=David, position=, department=Marketing, salary=4000)]}
        // }
    }

    @Test
    @DisplayName("5. Группировка с фильтрацией")
    fun testSimpleGrouping7() {
        // Группировка только высокооплачиваемых сотрудников
        val highEarnersByDept = employees.asSequence()
            .filter { it.salary > 5000 }
            .groupBy { it.department }
        println(highEarnersByDept)
        //{
        //  Engineering=[Employee(name=Bob, position=, department=Engineering, salary=6000),
        //  Employee(name=Eve, position=, department=Engineering, salary=5500)]
        // }
    }

    @Test
    @DisplayName("6. Анализ слов в тексте")
    fun testWordCount() {
        val text = "hello world hello kotlin world java kotlin java hello"

        // Подсчет частоты слов
        val wordFrequency = text.split(" ").asSequence()
            .groupBy { it }
        println(wordFrequency)
        // {hello=[hello, hello, hello],
        // world=[world, world],
        // kotlin=[kotlin, kotlin],
        // java=[java, java]}
        val wordFrequency2 = text.split(" ")
            .groupingBy { it } //т.к. далее напротив ключа одно значение, то используем groupingBy
            .eachCount()
        println(wordFrequency2)
        //{hello=3, world=2, kotlin=2, java=2}

        // Группировка по длине слов
        val wordsByLength = text.split(" ").asSequence()
            .groupBy { it.length }
            .mapValues { (_, worlds) ->
                worlds.distinct()
            }
        println(wordsByLength)
        //{5=[hello, world], 6=[kotlin], 4=[java]}
    }

    @Test
    @DisplayName("7. Группировка заказов: Сумма заказов по клиентам")
    fun testOrderGrouping() {
        val totalByCustomer: Map<String, Double> = orders
            .groupBy { it.customer }
            .mapValues { (_, orders) ->
                orders.sumOf { it.amount }
            }
        println(totalByCustomer)
        //{Alice=175.0, Bob=200.0, Charlie=200.0}
    }

    @Test
    @DisplayName("7. Группировка заказов: Количество заказов по категориям")
    fun testCountByCategory() {
        val countByCategory: Map<String, Int> = orders
            .groupingBy { it.category }
            .eachCount()
        println(countByCategory)
        //{Books=2, Electronics=3}
    }

    @Test
    @DisplayName("7. Группировка заказов: Средний чек по категориям")
    fun testAvgByCategory() {
        val avgByCategory: Map<String, Double> = orders
            .groupBy { it.category }
            .mapValues { (_, orders) ->
                orders.sumOf { it.amount } / orders.size
            }
        println(avgByCategory)
        //{Electronics=150.0, Books=62.5}

        val avgByCategory2: Map<String, Double> = orders.asSequence()
            .groupBy { it.category }
            .mapValues { (_, orders) ->
                orders.map { it.amount }.average() //average() - среднее значение из списка Order.amount
            }
        println(avgByCategory2)
        //{Electronics=150.0, Books=62.5}
    }

    @Test
    @DisplayName("⚡ Производительность группировки")
    fun testPerformance() {
        // Обычная группировка
        val withRegularGrouping = largeEmployeeList
            .groupBy { it.department }
            .mapValues { (_, employees) ->
                employees.sumOf { it.salary }
            }
        println(withRegularGrouping)

        // Группировка с sequence
        val withSequenceGrouping: Map<String, Int> =
            largeEmployeeList.asSequence()
                .groupBy { it.department }
                .mapValues { (_, employees) ->
                    employees.sumOf { it.salary }
                }

        println(withSequenceGrouping)

        // Группировка с агрегацией (самый эффективный)
        val withAggregateGrouping: Map<String, Int> =
            largeEmployeeList.asSequence()
                .groupingBy { it.department }
                .aggregate { key, accumulator: Int?, employee, first ->
                    if (first) employee.salary else
                        accumulator!! + employee.salary
                }
        println(withAggregateGrouping)

    }

    @Test
    @DisplayName("Вопрос 1: сгруппировать сотрудников по отделам и вывести топ-2 по зарплате в каждом")
    fun testQuestion1() {
        val topEmployeesByDept: Map<String, List<Employee>> = employees
            .groupBy { it.department }
            .mapValues { (_, employees) ->
                employees.sortedByDescending { it.salary }.take(2)
            }
        println(topEmployeesByDept)
        // {
        // Engineering=[
        //      Employee(name=Bob, position=, department=Engineering, salary=6000),
        //      Employee(name=Eve, position=, department=Engineering, salary=5500)
        //   ],
        // Marketing=[
        //      Employee(name=Charlie, position=, department=Marketing, salary=4500),
        //      Employee(name=David, position=, department=Marketing, salary=4000)
        //   ]
        // }
    }

    @Test
    @DisplayName("Вопрос 2: найти отдел с максимальной суммарной зарплатой")
    fun testQuestion2() {
        val deptWithMaxSalary = employees
            .groupingBy { it.department }
            .aggregate { key, accumulator: Int?, employee, first ->
                if (first) employee.salary else
                    accumulator!! + employee.salary
            }
            .maxByOrNull { it.value }?.key
        println(deptWithMaxSalary)
        //Engineering

        //2-й вариант
        val deptWithMaxSalary2 = employees
            .groupBy { it.department }
            .maxByOrNull { empls ->
                empls.value.sumOf { employee ->
                    employee.salary
                }
            }?.key
        println(deptWithMaxSalary2)
        //Engineering

        //2-й вариант
        val deptWithMaxSalary3 = employees
            .groupBy { it.department }
            .maxByOrNull { empls ->
                empls.value.sumOf { employee ->
                    employee.salary
                }
            }
        println(deptWithMaxSalary3)
        //Engineering=[Employee(name=Alice, position=, department=Engineering, salary=5000), Employee(name=Bob, position=, department=Engineering, salary=6000), Employee(name=Eve, position=, department=Engineering, salary=5500)]

    }

    @Test
    @DisplayName("Вопрос 3: сгруппировать данные по нескольким полям")
    fun testQuestion3() {
        // Группировка по комбинации полей
        val byDeptAndSalaryRange: Map<String, List<Employee>> = employees
            .groupBy {
                "${it.department}_${if (it.salary < 5000) "JUNIOR" else "SENIOR"}"
            }

        println(byDeptAndSalaryRange)
        // {
        // Engineering_SENIOR=[
        //      Employee(name=Alice, position=, department=Engineering, salary=5000),
        //      Employee(name=Bob, position=, department=Engineering, salary=6000),
        //      Employee(name=Eve, position=, department=Engineering, salary=5500)
        //    ],
        //  Marketing_JUNIOR=[
        //          Employee(name=Charlie, position=, department=Marketing, salary=4500),
        //          Employee(name=David, position=, department=Marketing, salary=4000)
        //    ]
        // }
    }

    @Test
    @DisplayName("Задача 1: Анализ транзакций:  Найти общую сумму транзакций по категориям")
    fun testTask1() {
        val totalByCategory: Map<String, Double> =
            transactions.groupBy { it.category }
                .mapValues { (_, trans) ->
                    trans.sumOf { it.amount }
                }
        println(totalByCategory)
        //{Electronics=550.0, Food=125.0, Clothing=450.0}
    }

    @Test
    @DisplayName("Задача 1: Найти топ-3 клиента по общей сумме трат")
    fun testTask2() {
        val top3Customers: List<Pair<String, Double>> =
            transactions.groupingBy { it.customer }
                .aggregate { _, accumulator: Double?, transaction, first ->
                    if (first) transaction.amount else accumulator!! + transaction.amount
                }
                .toList()
                .sortedByDescending { it.second }
                .take(3)
        println(top3Customers)
        //[(Alice, 450.0), (Charlie, 300.0), (David, 250.0)]

        val top3Customers2: List<Pair<String, Double>> =
            transactions.groupingBy { it.customer }
                .aggregate { _, accumulator: Double?, transaction, first ->
                    if (first) transaction.amount else accumulator!! + transaction.amount
                }
                .entries.sortedByDescending { it.value }
                .take(3)
                .map { it.key to it.value }
        println(top3Customers2)
        //[(Alice, 450.0), (Charlie, 300.0), (David, 250.0)]

        val toList: List<Pair<String, Double>> = transactions.groupingBy { it.customer }
            .aggregate { _, accumulator: Double?, transaction, first ->
                if (first) transaction.amount else accumulator!! + transaction.amount
            }.toList()
        println(toList)
        //[(Alice, 450.0), (Bob, 125.0), (Charlie, 300.0), (David, 250.0)]


    }

    @Test
    @DisplayName("Задача 1: Найти города с максимальной транзакцией в каждой категории")
    fun testTask3() {
        val maxTransactionByCategoryPerCity: Map<String, Map<String, Transaction>> =
            transactions
                .groupBy { it.category }
                .mapValues { (_, categoryTrans) ->
                    categoryTrans.groupBy { it.city }
                        .mapValues { (_, cityTrans) ->
                            cityTrans.maxBy { it.amount }
                        }
                }
        println(maxTransactionByCategoryPerCity)
        // {
        //  Electronics={
        //          New York=Transaction(id=1, customer=Alice, amount=150.0, category=Electronics, city=New York, date=2024-01-15),
        //          Boston=Transaction(id=4, customer=Charlie, amount=300.0, category=Electronics, city=Boston, date=2024-01-16)
        //  },
        //  Food={
        //          Boston=Transaction(id=2, customer=Bob, amount=75.0, category=Food, city=Boston, date=2024-01-15)
        //  },
        //  Clothing={
        //          New York=Transaction(id=3, customer=Alice, amount=200.0, category=Clothing, city=New York, date=2024-01-16),
        //          Chicago=Transaction(id=7, customer=David, amount=250.0, category=Clothing, city=Chicago, date=2024-01-17)
        //  }
        // }
    }

    @Test
    @DisplayName("Задача 2: Найти среднее время выполнения для каждого действия")
    fun testTask4() {
        val averageDurationByAction: Map<String, Double> = logs.asSequence().groupBy { it.action }
            .mapValues { (_, logs) ->
                logs.map { it.duration }.average()
            }
        println(averageDurationByAction)
        //{login=287.5, view_page=62.5, logout=100.0}
    }

    @Test
    @DisplayName("Задача 2: Найти пользователей с количеством успешных и неуспешных операций")
    fun testTask5() {

        val userSuccessStats: Map<String, Pair<Int, Int>> =
            logs.groupBy { it.userId }
                .mapValues { (_, userlogs) ->
                    val success = userlogs.count { it.status == "SUCCESS" }
                    val failed = userlogs.count { it.status == "FAILED" }
                    success to failed
                }
        println(userSuccessStats)
        //{user1=(3, 0), user2=(2, 0), user3=(1, 1)}
    }

    @Test
    @DisplayName("Задача 2: Найти самое популярное действие для каждого пользователя")
    fun testTask6() {

        val groupByUser = logs.groupBy { it.userId }
        println(groupByUser)

        // {
        //    user1=[
        //      LogEntry(timestamp=2024-01-15 10:00:00, userId=user1, action=login, duration=150, status=SUCCESS),
        //      LogEntry(timestamp=2024-01-15 10:02:00, userId=user1, action=view_page, duration=50, status=SUCCESS),
        //      LogEntry(timestamp=2024-01-15 10:05:00, userId=user1, action=logout, duration=100, status=SUCCESS)
        //      ],
        //    user2=[
        //      LogEntry(timestamp=2024-01-15 10:01:00, userId=user2, action=login, duration=200, status=SUCCESS),
        //      LogEntry(timestamp=2024-01-15 10:04:00, userId=user2, action=view_page, duration=75, status=SUCCESS)
        //      ],
        //    user3=[
        //      LogEntry(timestamp=2024-01-15 10:03:00, userId=user3, action=login, duration=500, status=FAILED),
        //      LogEntry(timestamp=2024-01-15 10:06:00, userId=user3, action=login, duration=300, status=SUCCESS)
        //      ]
        // }

        val mostFrequentActionPerUser: Map<String, String> = logs.groupBy { it.userId }
            .mapValues { (_, userLogs) ->
                userLogs.groupingBy { it.action }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key ?: "N/A"
            }
        println(mostFrequentActionPerUser)
        //{user1=login, user2=login, user3=login}

    }

    @Test
    @DisplayName("Задача 3: Анализ продаж: Найти общую выручку по регионам")
    fun testTask7() {
        val revenueByRegion: Map<String, Double> = sales.groupBy { it.region }
            .mapValues { (_, regionSales) ->
                regionSales.sumOf { it.price * it.quantity }
            }
        println(revenueByRegion)
        //{North=2250.0, South=500.0, East=4900.0, West=250.0}
    }

    @Test
    @DisplayName("Задача 3: Анализ продаж: Найти лучшего продавца в каждой категории")
    fun testTask8() {

        val bestSellerByCategory: Map<String, String> = sales
            .groupBy { it.category }
            .mapValues { (_, categorySales) -> //тут List<Sale>
                categorySales.groupBy { it.seller }
                    .mapValues { (_, sellerSales) -> //тут List<Sale>
                        sellerSales.sumOf { it.price * it.quantity }
                    }
                    .maxByOrNull { it.value }?.key ?: "N/A"
            }
        println(bestSellerByCategory)
        //{Electronics=Bob, Education=Alice}
    }

    @Test
    @DisplayName("Задача 3: Анализ продаж: Найти продукты, которые продавались в количестве больше 10 штук")
    fun testTask9() {

        val popularProducts: List<String> = sales
            .filter { it.quantity > 10 }
            .map { it.product }
            .distinct()
            .sorted()
            .toList()
        println(popularProducts)
        //[Book, Notebook, Pen]
    }

    @Test
    @DisplayName(" Задача 4: Сложная аналитика: Найти категории, где средняя цена товара выше 100")
    fun testTask10() {

        val expensiveCategories: List<String> = sales
            .groupBy { it.category }
            .mapValues { (_, categorySales) -> //тут List<Sale>
                categorySales.map { it.price }.average()
            }
            .filter { it.value > 100 }
            .map { it.key }
        println(expensiveCategories)
        //[Electronics]

        val stringDoubleMap = sales
            .groupBy { it.category }
            .mapValues { (_, categorySales) -> //тут List<Sale>
                categorySales.map { it.price }.average()
            }
        println(stringDoubleMap)
        //{Electronics=456.25, Education=7.333333333333333}
    }

    @Test
    @DisplayName(" Задача 4: Сложная аналитика: Найти регионы, где продавцы продали более 2 различных продуктов")
    fun testTask11() {

        val diverseRegions: List<String> = sales
            .groupBy { it.region }
            .mapValues { (_, regionSales) ->
                regionSales.distinctBy { it.product }.size
            }
            .filter { it.value > 2 }
            .map { it.key }
            .toList()
        println(diverseRegions)
        //[North]

    }

    @Test
    @DisplayName(" Задача 4: Сложная аналитика: Рейтинг продавцов по общей выручке")
    fun testTask12() {

        val sellerRanking: List<Pair<String, Double>> = sales
            .groupingBy { it.seller }
            .aggregate { key, accumulator: Double?, sale, first ->
                val revenue = sale.price * sale.quantity //доход
                if (first) revenue else accumulator!! + revenue
            }
            .entries.sortedByDescending { it.value }
            .map { it.key to it.value }
        println(sellerRanking)
        //список пар:
        //[(Bob, 4900.0), (John, 2500.0), (Alice, 500.0), (Charlie, 250.0)]

        val sortedByDescending: List<Map.Entry<String, Double>> = sales
            .groupingBy { it.seller }
            .aggregate { key, accumulator: Double?, sale, first ->
                val revenue = sale.price * sale.quantity //доход
                if (first) revenue else accumulator!! + revenue
            }
            .entries.sortedByDescending { it.value }
        println(sortedByDescending)
        //список Map.Entry:
        //[Bob=4900.0, John=2500.0, Alice=500.0, Charlie=250.0]

    }

    @Test
    @DisplayName(" Задача 5: Вложенная группировка: Группировка по региону -> продавец -> категория")
    fun testTask13() {

        val nestedGrouping: Map<String, Map<String, Map<String, Double>>> = sales
            .groupBy { it.region }
            .mapValues { (_, regionSales) -> //тут List<Sale>
                regionSales.groupBy { it.seller }
                    .mapValues { (_, sellerSales) -> //тут List<Sale>
                        sellerSales.groupBy { it.category }
                            .mapValues { (_, categorySales) -> //тут List<Sale>
                                categorySales.sumOf { it.price * it.quantity }
                            }
                    }
            }
        println(nestedGrouping)
        //{North={John={Electronics=2250.0, Clothes=250.0}},
        // South={Alice={Education=500.0}},
        // East={Bob={Electronics=4900.0}},
        // West={Charlie={Education=250.0}}}

    }

    @Test
    @DisplayName(" Задача 6: Анализ временных промежутков")
    fun testTask14() {
        val intervalMillis = 5 * 60 * 1000L // 5 minutes in milliseconds

        val eventsBy5MinIntervals: Map<Long, Int> = events
            .groupBy { it.timestamp / intervalMillis }
            .mapValues { it.value.size }

        println(eventsBy5MinIntervals)
        //{5884064=228, 5884063=300, 5884062=300, 5884061=172}
    }
}

val events = (1..1000).map {
    Event("event_${it % 50}", System.currentTimeMillis() - (it * 1000), "user_${it % 100}")
}.asSequence()

data class Event(val name: String, val timestamp: Long, val userId: String)

// Большой набор данных
val largeEmployeeList: List<Employee> = (1..1_000_000).map {
    Employee(name = "Employee$it", department = "Dept${it % 100}", salary = 1000 + it % 5000)
}

// Sequence может работать с бесконечными данными
val infiniteSequence = generateSequence(1) { it + 1 }

val users = listOf(
    User("Alice", 25),
    User("Bob", 30),
    User("Charlie", 35),
    User("David", 40)
)

// Подготовим данные
val employees: List<Employee> = listOf(
    Employee("Alice", department = "Engineering", salary = 5000),
    Employee("Bob", department = "Engineering", salary = 6000),
    Employee("Charlie", department = "Marketing", salary = 4500),
    Employee("David", department = "Marketing", salary = 4000),
    Employee("Eve", department = "Engineering", salary = 5500)
)

data class User(val name: String, val age: Int)

data class EmployeeStats(
    val minSalary: Int,
    val maxSalary: Int,
    val totalSalary: Int,
    val count: Int,
) {
    val averageSalary get() = totalSalary.toDouble() / count
}

data class Order(val id: Int, val customer: String, val amount: Double, val category: String)

val orders = listOf(
    Order(1, "Alice", 100.0, "Electronics"),
    Order(2, "Bob", 50.0, "Books"),
    Order(3, "Alice", 75.0, "Books"),
    Order(4, "Charlie", 200.0, "Electronics"),
    Order(5, "Bob", 150.0, "Electronics")
)

data class Transaction(
    val id: Int,
    val customer: String,
    val amount: Double,
    val category: String,
    val city: String,
    val date: String,
)

val transactions = listOf(
    Transaction(1, "Alice", 150.0, "Electronics", "New York", "2024-01-15"),
    Transaction(2, "Bob", 75.0, "Food", "Boston", "2024-01-15"),
    Transaction(3, "Alice", 200.0, "Clothing", "New York", "2024-01-16"),
    Transaction(4, "Charlie", 300.0, "Electronics", "Boston", "2024-01-16"),
    Transaction(5, "Bob", 50.0, "Food", "Boston", "2024-01-17"),
    Transaction(6, "Alice", 100.0, "Electronics", "New York", "2024-01-17"),
    Transaction(7, "David", 250.0, "Clothing", "Chicago", "2024-01-17")
)

data class LogEntry(
    val timestamp: String,
    val userId: String,
    val action: String,
    val duration: Int, // milliseconds
    val status: String,
)

val logs = listOf(
    LogEntry("2024-01-15 10:00:00", "user1", "login", 150, "SUCCESS"),
    LogEntry("2024-01-15 10:01:00", "user2", "login", 200, "SUCCESS"),
    LogEntry("2024-01-15 10:02:00", "user1", "view_page", 50, "SUCCESS"),
    LogEntry("2024-01-15 10:03:00", "user3", "login", 500, "FAILED"),
    LogEntry("2024-01-15 10:04:00", "user2", "view_page", 75, "SUCCESS"),
    LogEntry("2024-01-15 10:05:00", "user1", "logout", 100, "SUCCESS"),
    LogEntry("2024-01-15 10:06:00", "user3", "login", 300, "SUCCESS")
)

data class Sale(
    val product: String,
    val category: String,
    val price: Double,
    val quantity: Int,
    val seller: String,
    val region: String,
)

val sales = listOf(
    Sale("Laptop", "Electronics", 1000.0, 2, "John", "North"),
    Sale("Mouse", "Electronics", 25.0, 10, "John", "North"),
    Sale("Cap", "Clothes", 5.0, 50, "John", "North"),
    Sale("Book", "Education", 15.0, 20, "Alice", "South"),
    Sale("Pen", "Education", 2.0, 100, "Alice", "South"),
    Sale("Phone", "Electronics", 500.0, 5, "Bob", "East"),
    Sale("Tablet", "Electronics", 300.0, 8, "Bob", "East"),
    Sale("Notebook", "Education", 5.0, 50, "Charlie", "West")
)