package tv.codealong.tutorials.various.sequenceAndStream

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * ⚡ Ключевое отличие: Lazy vs Eager Evaluation
 * Stream - ленивые операции, но создает новый Stream на каждом шаге
 * Sequence - полностью ленивые вычисления, обрабатывает элементы по одному
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
    @DisplayName("3. Группировка с агрегацией")
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

    //TODO - 3. Продвинутая группировка

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
