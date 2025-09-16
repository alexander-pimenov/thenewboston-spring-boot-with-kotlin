package tv.codealong.tutorials.various.locks

import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class ReentrantLockExample {
    private var resource = "Hello, World!"
    private val sdf = SimpleDateFormat("HH:mm:ss  ")
    private val lock = ReentrantLock()

    private val TIME_WAIT = 7000L
    private val TIME_SLEEP = 5000L

    init {
        val thread1 = Thread(LockClass("first", "Первый поток"))
        val thread2 = Thread(LockClass("second", "Второй поток"))

        thread1.start()
        thread2.start()

        printMessage(null)

        while (thread1.isAlive || thread2.isAlive) {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        println("\nЗавершение работы примера")
        System.exit(0)
    }

    private fun printMessage(msg: String?) {
        val text = sdf.format(Date()) + if (msg == null) resource else msg
        println(text)
    }

    inner class LockClass(private val name: String, private val text: String) : Runnable {
        override fun run() {
            var locked = false
            try {
                // Получение блокировки в течение TIME_WAIT
                locked = lock.tryLock(TIME_WAIT, TimeUnit.MILLISECONDS)
                if (locked) {
                    resource = text
                    printMessage(null)
                }
                Thread.sleep(TIME_SLEEP)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                // Убираем блокировку
                val message = "$name : завершил работу"
                printMessage(message)
                if (locked) {
                    lock.unlock()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ReentrantLockExample()
        }
    }
}