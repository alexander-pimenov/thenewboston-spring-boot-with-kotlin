package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.*

/**
 * Dispatchers — определяют, в каком потоке будет выполняться корутина:
 *  - Dispatchers.Main — основной поток (UI поток в Android).
 *  - Dispatchers.IO — для операций ввода-вывода (сеть, файлы).
 *  - Dispatchers.Default — для CPU-интенсивных задач.
 */
fun main(): Unit = runBlocking {
    launch(Dispatchers.Default) {
        println("Выполняется в фоновом потоке: ${Thread.currentThread().name}")
        withContext(Dispatchers.Main) {
            println("Переключились на основной поток: ${Thread.currentThread().name}")
        }
    }
}