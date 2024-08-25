package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.*

/**
 * https://youtu.be/ITLe4FIrrTg?t=1078
 */
class CoroutinesRunBlockingBuilderExampleBuilderExample {
}

/**
 * runBlocking запускает новую корутину, но блокирует поток, на котором был запущен.
 * И поток будет заблокирован до тех пор, пока корутина не будет завершена.
 * runBlocking - особенность такая, что выйти из этого блока мы сможем только,
 * когда внутри него выполнятся все корутины, от родителей до всех дочерних.
 * Наша корутина родитель будет ожидать завершения всех дочерних.
 *
 * runBlocking можно использовать для тестов. В боевом коде он не используется!!!!
 * Потому что блочит поток.
 * Также для тестов есть другой билдер - runTests.
 *
 *
 */

 fun main(): Unit = runBlocking {
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