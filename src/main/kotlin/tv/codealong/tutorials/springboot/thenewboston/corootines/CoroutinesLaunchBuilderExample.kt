package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.*
import java.util.Random
import kotlin.system.measureTimeMillis

/**
 * https://youtu.be/ITLe4FIrrTg
 *
 */
class CoroutinesLaunchBuilderExampleExample {

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
    launch {
        delay(2000)
        println("Text 1")
    }
    launch {
        delay(500)
        println("Text 2")
    }
}




