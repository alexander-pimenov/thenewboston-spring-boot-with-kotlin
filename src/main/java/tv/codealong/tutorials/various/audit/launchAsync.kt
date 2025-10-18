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

//val logger = LoggerFactory.getLogger("MyLogger")
//
//@OptIn(DelicateCoroutinesApi::class)
//fun main() {
//    MDC.put("requestId", "12345") // Set MDC on the current thread
//
//    GlobalScope.launch(MDCContext()) {
//        logger.info("Inside coroutine, requestId: {}", MDC.get("requestId")) // MDC will be available here
//    }
//
//    // MDC might not be available or might have a different value on the main thread after the launch
//    logger.info("Outside coroutine, requestId: {}", MDC.get("requestId"))
//}