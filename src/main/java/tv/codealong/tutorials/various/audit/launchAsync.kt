package tv.codealong.tutorials.various.audit

import kotlinx.coroutines.*
import kotlinx.coroutines.slf4j.MDCContext
import tv.codealong.tutorials.various.logerMDCContext.logger


@OptIn(DelicateCoroutinesApi::class)
fun launchAsync(coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO, lambda: () -> Unit) {
    GlobalScope.launch(coroutineDispatcher + MDCContext()) {
        runCatching {
            lambda.invoke()
        }.onFailure {
            logger.info("Событие не выполнено в следствии исключения: [ $it ]")
        }
    }
}