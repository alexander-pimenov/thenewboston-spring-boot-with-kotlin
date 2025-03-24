package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    launch {
        delay(2000L) // Неблокирующая задержка на 2 секунды
        println("World!")
        println("Поток-2:" + Thread.currentThread().name)
    }

    println("Hello,")
    println("Поток-1:" + Thread.currentThread().name)

}