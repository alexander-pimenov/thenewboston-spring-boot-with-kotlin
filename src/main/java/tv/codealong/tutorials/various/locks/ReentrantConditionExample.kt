package tv.codealong.tutorials.various.locks

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

class ReentrantConditionExample {
    private val store: Store
    private val sdf: SimpleDateFormat
    private val GOODS = arrayOf("Milk", "Kefir", "Ryazhenka", "Coffee", "Tea")
    private val goods = mutableListOf<String>()

    init {
        store = Store()
        sdf = SimpleDateFormat("HH:mm:ss  ")

        val producer = Thread(Producer())
        val consumer = Thread(Consumer())

        println("Start producer...")
        producer.start()
        println("Start consumer...")
        consumer.start()

        while (producer.isAlive || consumer.isAlive) {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        println("\nCompleting the example")
        System.exit(0)
    }

    private fun printMessage(msg: String?) {
        if (msg != null) {
            val text = sdf.format(Date()) + msg
            println(text)
        } else {
            println("\tGoods in stock: ${goods.size}")
        }
    }

    inner class Consumer : Runnable {
        override fun run() {
            println("Consumer is running... with ${Thread.currentThread().name}")
            if (goods.isEmpty()) {
                println("#Consumer. Goods in stock: ${goods.size}")
            }

            for (i in GOODS.indices) {
                try {
                    Thread.sleep(8000)
                } catch (ignored: InterruptedException) {
                }
                store.get()
            }
        }
    }

    inner class Producer : Runnable {
        override fun run() {
            println("Producer is running... with ${Thread.currentThread().name}")
            for (good in GOODS) {
                store.put(good)
                try {
                    Thread.sleep(2000)
                } catch (ignored: InterruptedException) {
                }
            }
        }
    }

    inner class Store {
        private val lock: ReentrantLock
        private val cond: Condition

        init {
            lock = ReentrantLock()
            cond = lock.newCondition()
        }

        fun get() {
            println("#get - ${Thread.currentThread().name}")
            lock.lock()
            try {
                // ✅ ИСПРАВЛЕНО: Добавлены фигурные скобки!
                while (goods.isEmpty()) {
                    println("#wait - ${Thread.currentThread().name}")
                    cond.await() // Теперь внутри цикла!
                }

                printMessage("Realization: ${goods.first()}")
                goods.removeFirst()
                printMessage(null)

                // Сигнализация
                cond.signalAll()
            } catch (ignored: InterruptedException) {
            } finally {
                lock.unlock()
            }
        }

        fun put(good: String) {
            println("#put - ${Thread.currentThread().name}")
            lock.lock()
            try {
                // Ожидание освобождения места
                while (goods.size >= 3) {
                    cond.await()
                }

                goods.add(good)
                printMessage("Delivery: $good")
                printMessage(null)

                // Сигнализация
                cond.signalAll()
            } catch (e: InterruptedException) {
            } finally {
                lock.unlock()
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ReentrantCondExample()
        }
    }
}