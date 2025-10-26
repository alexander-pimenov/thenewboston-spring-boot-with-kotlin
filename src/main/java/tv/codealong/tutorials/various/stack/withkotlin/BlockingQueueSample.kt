package tv.codealong.tutorials.various.stack.withkotlin

import java.lang.StringBuilder
import java.util.concurrent.ArrayBlockingQueue

/**
 * BlockingQueueSample
 * Вариант записанный на Котлин
 */
object BlockingQueueSample {
    @JvmStatic
    fun main(args: Array<String>) {
        println("BlockingQueueSample: main thread started...")
        val queue = ArrayBlockingQueue<String?>(3)


        val producer = Thread(Runnable {
            val words: List<String?> = listOf("123", "abc", "qwerty", "queue", "stack", "array", "list")
            var i = 0
            while (i < words.size && !Thread.interrupted()) {
                try {
                    Thread.sleep(4000)
                    queue.put(words[i])
                    println("producer: записал в очередь " + words[i] + ", число элементов в очереди: " + queue.size)
                    i++
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }, "Producer")

        val consumer = Thread(Runnable {
            val sb = StringBuilder()
            while (!Thread.interrupted()) {
                try {
                    sb.setLength(0)
                    Thread.sleep(7000)
                    sb.append(queue.take())
                    println("consumer: обработал из очереди " + sb.reverse() + ", число элементов в очереди: " + queue.size)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }, "Consumer")

        producer.start()
        try {
            Thread.sleep(5000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        consumer.start()
        println("BlockingQueueSample: main thread finished")

    }
}
































