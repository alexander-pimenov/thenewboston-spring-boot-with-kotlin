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

    //TODO - продолжить с примерами отсюда
    //Пример 2: Бесконечные последовательности

}

val users = listOf(
    User("Alice", 25),
    User("Bob", 30),
    User("Charlie", 35),
    User("David", 40)
)

data class User(val name: String, val age: Int)
