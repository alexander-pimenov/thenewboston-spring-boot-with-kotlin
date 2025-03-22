package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    launch {
        delay(2000L) // Неблокирующая задержка на 2 секунды
        println("World!")
    }
    println("Hello,")
}