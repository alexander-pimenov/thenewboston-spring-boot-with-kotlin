package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

class ElementAndCoroutineContextExample {
}

fun checkCoroutineContext() {
    val context: CoroutineContext = CoroutineName("A Name")
    val coroutineName: CoroutineName? = context[CoroutineName.Key] // идем в Контекст с ключом CoroutineName.Key → A Name
    val job: Job? = context[Job] // null

    //or val job: Job?= context[Job.Key]
}

suspend fun main(): Unit = coroutineScope {
    // Создаем КорутинКонтекст на основе двух Элементов: CoroutineName и Job - родительская корутина.
    val context1 = CoroutineName("Name 1") + Job()

    // Создаем 2-й КорутинКонтекст на основе первого (берем всё от него) и присваиваем новое CoroutineName
    // (оно переопределяет имя из первой, скорее затирает значения от первой корутины родителя) и Job от контекста
    // родителя - вторая корутина.
    val context2 = context1 + CoroutineName("Name 2")
    println(context2[CoroutineName.Key]?.name) // Name 2
    println(context2[Job.Key]?.isActive) // true
}