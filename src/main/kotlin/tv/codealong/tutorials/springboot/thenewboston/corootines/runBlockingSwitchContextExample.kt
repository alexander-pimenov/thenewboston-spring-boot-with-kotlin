package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.*
//
//fun main() = runBlocking {
//    launch(Dispatchers.Default) {
//        println("Выполняется в фоновом потоке: ${Thread.currentThread().name}")
//        withContext(Dispatchers.Main) {
//            println("Переключились на основной поток: ${Thread.currentThread().name}")
//        }
//    }
//}