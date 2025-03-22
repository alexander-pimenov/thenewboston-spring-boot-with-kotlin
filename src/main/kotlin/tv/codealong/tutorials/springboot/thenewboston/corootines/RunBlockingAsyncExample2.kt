package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class RunBlockingAsyncExample2 {

    fun main() = runBlocking {
        println("точка 1. поток:" + Thread.currentThread().name)
        val result = async {
            delay(2000L) // Неблокирующая задержка на 2 секунды
            println("точка 3. поток:" + Thread.currentThread().name)
            "Hello, World!"
        }
        println("точка 2. поток:" + Thread.currentThread().name)
        println(result.await()) // Ожидаем результат
    }

}

