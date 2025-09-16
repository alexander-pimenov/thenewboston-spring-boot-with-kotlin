package tv.codealong.tutorials.various.locks

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

/**
 * В примере ImprovedReentrantLockExample, листинг которого представлен ниже, используется внутренний класс
 * LockClass для организации двух потоков. Константы TIME_WAIT и TIME_SLEEP используются потоками для
 * организации определенных задержек при выполнении. Текстовая переменная resource используется в качестве
 * общего ресурса, значение которого будет изменяться внутри потоков. Метод printMessage выводит в консоль
 * сообщения потоков с указанием времени.
 *
 * В конструкторе(init block) примера создается блокировка lock типа ReentrantLock и два потока, которые
 * будут использовать lock для блокирования доступа к текстовому ресурсу. Сначала каждый поток пытается в
 * течение определенного времени (TIME_WAIT, мс) блокировать доступ к ресурсу resource с использованием
 * метода tryLock. Если блокировка получена, то текст строки resource изменяется. После этого в потоке
 * выполняется некоторая задержка по времени (TIME_SLEEP, мс) и поток завершает свою работу с освобождением
 * блокировки методом unlock. Если поток в течение времени TIME_WAIT не смог блокировать ресурс, то он переходит
 * к стадии задержки и завершению работы.
 *
 * Оперируя временем ожидания блокировки TIME_WAIT и временем задержки TIME_SLEEP можно дать возможность либо
 * каждому из потоку изменить значение resource, либо только одному
 */
class ImprovedReentrantLockExample {
    private var resource = "Hello, World!"
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    private val lock = ReentrantLock()

    companion object {
        private const val TIME_WAIT = 7000L
        private const val TIME_SLEEP = 5000L
    }

    init {
        val threads = listOf(
            thread { LockClass("first", "Первый поток").run() },
            thread { LockClass("second", "Второй поток").run() }
        )

        printMessage(null)

        threads.forEach { it.join() }

        println("\nЗавершение работы примера")
    }

    private fun printMessage(msg: String?) {
        val time = timeFormatter.format(LocalDateTime.now())

        //выведем название потока
        val threadName = Thread.currentThread().name
        val currTest = "printMessage# поток: $threadName"
        println(currTest)
        val text = "$time  ${msg ?: resource}"
        println(text)
    }

    inner class LockClass(private val name: String, private val text: String) : Runnable {
        override fun run() {
            val locked = lock.tryLock(TIME_WAIT, TimeUnit.MILLISECONDS)
            try {
                if (locked) {
                    resource = text
                    printMessage(null)
                }
                Thread.sleep(TIME_SLEEP)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                println("Поток прерван: ${e.message}")
            } finally {
                printMessage("$name : завершил работу")
                if (locked) {
                    lock.unlock()
                }
            }
        }
    }
}

fun main() {
    ImprovedReentrantLockExample()
}