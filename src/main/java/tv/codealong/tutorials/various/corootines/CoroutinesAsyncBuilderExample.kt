package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.*

/**
 * https://youtu.be/ITLe4FIrrTg?t=966
 */
class CoroutinesAsyncBuilderExample {
}

/**
 * КорутинСкоуп - является входной точкой в корутины.
 * launch и async запускаются из корутинСкоупа в отличие от runBlocking,
 * который может быть запущен из обычной функции.
 * launch и async - это икстеншен функции CoroutineScope
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
}