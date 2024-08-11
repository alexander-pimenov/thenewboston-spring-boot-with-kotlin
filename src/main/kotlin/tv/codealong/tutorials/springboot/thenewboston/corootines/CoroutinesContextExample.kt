package tv.codealong.tutorials.springboot.thenewboston.corootines

import kotlinx.coroutines.*

/**
 * Каждая корутина выполняется в каком то контексте.
 * Ключ типа Key<T> знает какое должно вернуться значение.
 * Каждое значение из контекста является также контекстом.
 * Явно создавать контекст не нужно, т.к. он определяется либо в Scope, либо создается при запуске корутины.
 * Также можно объединить несколько контекстов в один или удалить элементы из него.
 * Это так же делается для переопределения. Например, это часто делается для Диспетчеров.
 * Диспетчер (Dispatcher) - это элемент контекста, который отвечает на каком потоке (потоках) будет выполняться
 * соответствующая ему корутина.
 * Он решает какой пул потоков использовать, на каком потоке будет выполняться корутина или вовсе не менять поток и
 * продолжить выполнение корутины в том же потоке в котором она запускалась.
 * Вот набор диспетчеров корутин - Default, Main, Unconfined, IO.
 *
 * Default: По умолчанию. Используется всеми билдерами корутин. launch, async
 * Приводит к смене потока выполнения корутины.
 * Количество потоков в пуле соответствует количеству ядер процессора. Но никогда не меньше 2.
 *
 * IO: для выполнения операций ввода/вывода на специальном пуле потоков.
 *
 * Maine: диспетчер, который выполняет работу на главном потоке.
 * По умолчанию этот диспетчер не определен. Поэтому чтобы он заработал, нужно добавить зависимость, где есть
 * этот MaineCoroutineDispatcher диспетчер. Самостоятельно его не стоит реализовывать. Можно взять зависимость:
 * "org.jetbrains.kotlinx:kotlinx-coroutines-android"
 *
 * Unconfined: диспетчер, который не привязан к какому-либо потоку.
 * И выполнение корутины происходит в том же потоке, в котором происходит её создание и запуск.
 * Т.е. если мы по какой-то причине не хотим менять поток выполнения корутины (это происходит по дефолту),
 * то Unconfined нам подойдет.
 *
 * Не используйте диспетчеры на прямую, а лучше создавайте обертку над ними.
 *
 *
 *
 */
class CoroutinesContextExample {
}

class AppDispatchers(
    val main: MainCoroutineDispatcher = Dispatchers.Main,
    val default: CoroutineDispatcher = Dispatchers.Default,
    val io: CoroutineDispatcher = Dispatchers.IO,
    val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
)

class ViewModel constructor(dispatchers: AppDispatchers)

/**
 * Job. Объект корутины.
 * Это выполняемая в фоне задача.
 * val job: Job = launch {}
 * Жизненный цикл Job состоит из 6-ти состояний.
 * New, Active, Completing, Canceling, Cancelled, Completed.
 * После стадии Completing корутина дожидается завершения дочерних Job (задач).
 * После завершения задач, она становится Completed.
 * Отмена Job приводит корутину к отмене, а также отмене всех её дочерних Job.
 * Также это работает и в обратном порядке: в случае возникновения ошибки или отмене у дочерней Job,
 * будет отменена родительская корутина. И все другие дочерние Job от этого родителя.
 * Чтобы этого не происходило, а отменялись только родительские Job, то нужно использовать реализацию
 * Job - SupervisorJob. Она является основной.
 *
 */
fun main(): Unit = runBlocking {
    println("main runBlockig: I'm working in thread ${Thread.currentThread().name} ")
    val job: Job = launch {
        val doWork = doWork("Broadcast. #корутина")
    }
}
