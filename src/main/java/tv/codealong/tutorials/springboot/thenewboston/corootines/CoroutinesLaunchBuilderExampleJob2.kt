package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.*

/**
 * https://youtu.be/ITLe4FIrrTg
 *
 */
class CoroutinesLaunchBuilderExampleExampleJob2 {

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
 * Здесь особенность Job - это контекст корутины.
 * При создании новой корутины у нас наследуются все Элементы из КорутинКонтекста из родительской корутины,
 * кроме Job!!! Для каждой корутины создается свой инстанс Job, который потом будет заасайнен в
 * КорутинКонтекст. Но этот Job будет наследоваться от Job родителя.
 *
 * (продолжить от сюда - https://youtu.be/ITLe4FIrrTg?t=2232)
 *
 *
 */
suspend fun main(): Unit = coroutineScope {
    // Создаем КорутинКонтекст на основе двух Элементов: CoroutineName и Job - родительская корутина.
    val name = CoroutineName("Some Name 1")
    val job = Job() //считаем что это родительский Job

    // ВАЖНО: При создании новой корутины всегда будет создаваться новый Job, который наследуется от
    // родительского Job.
    launch(name + job) {
        val childName = coroutineContext[CoroutineName.Key]
        println("1) ${childName == name}") // true

        val childJob = coroutineContext[Job.Key]
        println("2) ${childJob == job}") //false
        println("3) ${childJob == job.children.first()}") // true
    }
}





