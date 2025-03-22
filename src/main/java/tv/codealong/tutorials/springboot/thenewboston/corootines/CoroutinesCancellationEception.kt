package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.*

/**
 *
 * Продолжить отсюда про отмену КорутинСкоупа -
 *
 *
 */
class CoroutinesCancellationException {
}

suspend fun main(): Unit = coroutineScope {
    //тут начинает работать основная корутина

    //тут запускаем дочернюю корутину, но в неё ничего не передаем
    val job = launch {
        repeat(1000000) { i ->
            //проверяем в каком статусе находимся
            if (isActive) {
                //делаем какую-то работу
                //тут дочернюю корутину задерживаем на 100 мс
                delay(10)
                println("index: $i")
            } else {
                //чистим ресурсы
                return@launch
            }

            //или просто вызвать функцию из библиотеки, которая сама кинет CancellationException
            //чтобы как можно быстрее выйти из текущей корутины и передать информацию об отмене
            //её родителю.
            // ensureActive() //!!!!! можно применять
        }
    }

    //тут основная корутина задерживается на 320 мс
    delay(320)
    //тут отменяем основную корутину и она отменяет дочернюю, это видно
    //по тому, что всего два раза напечатается:
    //index: 0
    //index: 1
    job.cancel()
    println("Cancelled")
}