package tv.codealong.tutorials.various.logerMDCContext

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC

val logger = LoggerFactory.getLogger("MyLogger")

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    MDC.put("requestId", "12345") // Set MDC on the current thread

    GlobalScope.launch(MDCContext()) {
        logger.info("Inside coroutine, requestId: {}", MDC.get("requestId")) // MDC will be available here
    }

    // MDC might not be available or might have a different value on the main thread after the launch
    logger.info("Outside coroutine, requestId: {}", MDC.get("requestId"))
}