package tv.codealong.tutorials.various.sendingresulasynchronously

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.min
import kotlin.random.Random

@Serializable
data class TaskResult(val taskId: Int, val success: Boolean)

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

/**
 * 🧠 Важно:
 * - Если используешь setBody() с сериализуемым объектом, обязательно подключи ContentNegotiation.
 * - Если отправляешь «сырой» JSON-строкой — обязательно добавь Content-Type вручную.
 */
suspend fun sendResultAsync(taskId: Int, success: Boolean) {
    try {
        val response: HttpResponse = client.post("http://localhost:8098/task/result") {
            headers {
                append("Content-Type", "application/json")
            }
            setBody(TaskResult(taskId, success))
        }
        println("API response for task $taskId: ${response.status}")
    } catch (e: Exception) {
        println("Ошибка при отправке результата задачи $taskId: ${e.message}")
        e.printStackTrace()
    }
}

fun exponentialBackoffWithJitter(attempt: Int): Long {
    val baseDelay = 100L
    val maxDelay = 2000L
    val expDelay = min(baseDelay * (1 shl attempt), maxDelay)
    return Random.nextLong(expDelay / 2, expDelay)
}

/**
 * Встраиваем в задачу с Backoff
 */
fun CoroutineScope.launchTask(taskId: Int, resources: List<ReentrantLock>) = launch {
    var attempt = 1
    val maxAttempts = 5
    while (attempt < maxAttempts) {
        val acquired = mutableListOf<ReentrantLock>()
        var success = false

        for (lock in resources) {
            if (lock.tryLock()) {
                acquired.add(lock)
            } else {
                break
            }
        }

        if (acquired.size == resources.size) {
            println("Задача №_$taskId: успешно захватила все ресурсы")
            delay(500)
            success = true
        }

        acquired.forEach { it.unlock() }

        if (success) {
            sendResultAsync(taskId, true)
            break
        } else {
            val waitTime = exponentialBackoffWithJitter(attempt)
            println("Задача №_$taskId: ждёт $waitTime мс перед повтором")
            delay(waitTime)
            attempt++
        }
    }

    if (attempt == maxAttempts) {
        sendResultAsync(taskId, false)
    }
}

/**
 * запуск задач
 */
fun main(): Unit = runBlocking {

    // создаём WireMock для имитации API - не работает, выдает ошибку - Caused by: java.net.ConnectException: Connection refused: getsockopt
//    val wmServer = WireMockServer(8098)
//    wmServer.start()
//    while (!wmServer.isRunning) {
//        Thread.sleep(100)
//    }
//    println("WireMock запущен на порту ${wmServer.port()}")
//    Thread.sleep(500)
    //создадим стаб для ответа
//    wmServer.stubFor(
//        post(urlPathTemplate("/task/result"))
//            .willReturn(
//                aResponse().withStatus(200)
//            )
//    )
    try {
        val lockA = ReentrantLock()
        val lockB = ReentrantLock()
        val lockC = ReentrantLock()

        launchTask(1, listOf(lockA, lockB))
        launchTask(2, listOf(lockB, lockC))
        launchTask(3, listOf(lockA, lockC))
    } finally {
        //wmServer.stop()
    }
}
