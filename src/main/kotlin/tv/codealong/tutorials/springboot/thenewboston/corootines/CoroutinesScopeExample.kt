package tv.codealong.tutorials.springboot.thenewboston.corootines

/**
 * Они являются расширениями к типу CoroutineScope:
 * fun CoroutineScope.launch()
 * fun CoroutineScope.async()
 * fun runBlocking()
 *
 * Любая асинхронная операция, которая запускается в нашем приложении,
 * должна быть остановлена, когда результат её выполнения больше нам не нужен.
 * Это позволит не занимать лишние ресурсы.
 * Например, когда пользователь уходит с экрана, то эти данные можно уже не получать, т.к.
 * пользователь закрыл экран.
 * В корутинах для этого используется CoroutineScope.
 * CoroutineScope - Жизненный цикл для выполнения асинхронных операций.
 * Также он отвечает за все дочерние корутины в рамках него.
 * И все запускающиеся корутины должны быть привязаны к какому-то CoroutineScope.
 * Именно поэтому все корутин-билдеры являются расширением к типу CoroutineScope.
 * fun runBlocking() является исключением, ему скоуп не нужен, ведь это
 * специальный корутин-билдер для блокировки потока с которого запускается
 * корутина, пока она не будет закончена. Это некий мост между блокирующим походом и
 * походом основанным на прерываниях (корутинах).
 * ОЧЕНЬ ВАЖНО: используй fun runBlocking() только для запуска тестов или в
 * main() функции.
 * В других случаях это может привести к плохим последствиям и нарушить работу приложения.
 *
 * Во время работы с корутинами мы будем сталкиваться с понятием structured concurrency.
 * Structured concurrency - это когда мы можем выполнять асинхронные операции
 * на разных потоках одновременно.
 * Structured concurrency - это механизм, предоставляющий иерархическую структуру для
 * организации работы корутин.
 * Все принципы Structured concurrency строятся на основе CoroutineScope.
 * А под капотом, через отношение родитель-ребенок у Job.
 *
 * Принципы работы CoroutineScope:
 * 1) Отмена Scope - отмена корутин. Scope может отменить выполнение всех дочерних корутин, если возникает
 * ошибка или операция будет отменена.
 * 2) Scope знает про все корутины. Любая корутина, запускаемая в скоупе, будет храниться ссылкой в нём через
 * отношение "родитель-ребенок" у Job.
 * 3) Я тебя буду ждать. Scope автоматически ожидает выполнения всех дочерних корутин, но не обязательно
 * завершается вместе с ними. Не важно, как они выполнятся Успешно или с Ошибкой.
 *
 *
 * API CoroutineScope:
 * @code
 * interface CoroutineScope {
 *   val coroutineContext: CoroutineContext
 * }
 *
 * Нужно разобраться в чем отличие для CoroutineScope от CoroutineContext. Вроде CoroutineScope это просто обёртка
 * над CoroutineContext. Целевое назначение и использование - это и есть отличие.
 * CoroutineContext - это набор параметров для выполнения корутин, который обязательно есть у любой из них.
 * CoroutineScope хоть и является оберткой над CoroutineContext, но предназначен для объединения всех запущенных
 * корутин в рамках него, а так же под капотом передает им Job, которая будет их объединять и будет родительской для
 * всех CoroutineScope.
 *
 * GlobalScope - это специальный CoroutineScope, который не привязан к какой-либо Job (не имеет Job).
 * Все корутины, запущенные в рамках него, будут работать до своей остановки или остановки процесса.
 * И могут стать источником утечки ресурсов. Не рекомендуется его использовать.
 * Лучше создать свои скоупы.
 * Для создания скоупов можно использовать функцию CoroutineScope:
 * @code
 * CoroutineScope(Job() + Dispatcher.Default)
 *
 * В неё мы можем передать элементы CoroutineContext.
 * Хорошей практикой использовать SupervisorJob, потому что ошибки в дочерней корутине не приведут к остановке
 * всех корутин в скоупе.
 * (https://youtu.be/gmGJqoiifOk?list=PL0SwNXKJbuNmsKQW9mtTSxNn00oJlYOLA&t=332)ы
 *
 *
 */
class CoroutinesScopeExample {
}