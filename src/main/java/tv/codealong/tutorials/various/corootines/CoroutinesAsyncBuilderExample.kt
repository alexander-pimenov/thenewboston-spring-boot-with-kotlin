package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

/**
 * https://youtu.be/ITLe4FIrrTg?t=966
 */
class CoroutinesAsyncBuilderExample {
}

/**
 * КорутинСкоуп - является входной точкой в корутины.
 * launch и async запускаются из корутинСкоупа в отличие от runBlocking,
 * который может быть запущен из обычной функции.
 * launch и async - это экстеншен (расширение) функции CoroutineScope
 *
 * Deferred - аналог Future из Java, это тоже результат отложенных вычислений.
 * В строке с result пытаемся получить результат
 */

suspend fun main(): Unit = coroutineScope {
    val text1: Deferred<String> = async {
        delay(2000)
        "Text 1"
    }
    val text2: Deferred<String> = async {
        delay(500)
        "Text 2"
    }
    val result = text1.await() + text2.await()
    println(result)

    val startTime = System.currentTimeMillis()
    println("start")
    val numDeferred1 = async { sum(1, 2)}
    val numDeferred2 = async { sum(3, 4)}
    val numDeferred3 = async { sum(5, 6)}
    val num1 = numDeferred1.await()
    val num2 = numDeferred2.await()
    val num3 = numDeferred3.await()

    println("number1: $num1, number2: $num2, number3: $num3")
    val endTime = System.currentTimeMillis()
    println("Finish. Time taken: " + (endTime - startTime) + " ms")
}

suspend fun sum(a: Int, b: Int): Int {
    delay(500) //иммитирует задержку
    return a + b
}