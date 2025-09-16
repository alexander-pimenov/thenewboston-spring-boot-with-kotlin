package tv.codealong.tutorials.various.locks

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class ImprovedReentrantConditionExample {
    private val goods = mutableListOf<String>()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val items = listOf("Milk", "Kefir", "Ryazhenka", "Coffee", "Tea")

    init {
        val producer = thread(name = "Producer") { produce() }
        val consumer = thread(name = "Consumer") { consume() }

        producer.join()
        consumer.join()

        println("\nExample completed")
    }

    private fun printMessage(message: String? = null) {
        val time = timeFormatter.format(LocalTime.now())
        if (message != null) {
            println("$time  $message")
        } else {
            println("$time  Goods in stock: ${goods.size}")
        }
    }

    private fun produce() {
        println("Producer started...")
        items.forEach { item ->
            lock.withLock {
                while (goods.size >= 3) {
                    println("Producer waiting - warehouse full")
                    condition.await()
                }

                goods.add(item)
                printMessage("Delivery: $item")
                printMessage()

                condition.signalAll()
            }
            Thread.sleep(2000)
        }
        println("Producer finished")
    }

    private fun consume() {
        println("Consumer started...")
        repeat(items.size) {
            lock.withLock {
                while (goods.isEmpty()) {
                    println("Consumer waiting - warehouse empty")
                    condition.await()
                }

                val item = goods.removeFirst()
                printMessage("Realization: $item")
                printMessage()

                condition.signalAll()
            }
            Thread.sleep(8000)
        }
        println("Consumer finished")
    }
}

fun main() {
    ImprovedReentrantConditionExample()
}