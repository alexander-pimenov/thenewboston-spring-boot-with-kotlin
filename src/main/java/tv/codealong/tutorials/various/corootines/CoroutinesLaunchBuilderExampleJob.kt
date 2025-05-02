package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.*
import java.util.Random
import kotlin.system.measureTimeMillis

/**
 * https://youtu.be/ITLe4FIrrTg
 *
 */
class CoroutinesLaunchBuilderExampleExampleJob {

}

/**
 * КорутинСкоуп - является входной точкой в корутины.
 * Его задача - это ограничивать область действия корутин.
 * Корутина действует в рамках скоупа.
 * КорутинСкоуп: launch и async запускаются из корутинСкоупа в отличие от runBlocking,
 * который может быть запущен из обычной функции.
 * launch и async - это extension функции CoroutineScope
 *
 * Связь дочерних корутин с родительской проходит до самого верха. Т.е. если что-то случается с
 * дочерней корутиной, то всё это узнает самая верхняя родительская корутина. (Это и есть
 * принцип structured concurrency)
 *
 */
suspend fun main(): Unit = coroutineScope {
    val job1 = launch {
        delay(2000)
        println("Text 1")
    }
    val job2 = launch {
        delay(500)
        println("Text 2")
    }
    job1.join()
    job2.join()
    println("Done") // Вызов join() убивает корутину

    println("job1: ${job1.isActive}") // false
    println("job: ${job2.isCompleted}") // true

}





