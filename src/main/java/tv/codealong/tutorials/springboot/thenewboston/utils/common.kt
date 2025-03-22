package tv.codealong.tutorials.springboot.thenewboston.utils

import org.awaitility.Awaitility
import org.awaitility.core.ConditionTimeoutException
import java.time.Duration

fun <T> T.andAwait(
    condition: (T) -> Boolean,
    timeout: Duration = Duration.ofSeconds(10),
    pollInterval: Duration = Duration.ofMillis(100)
): T {
    val start = System.currentTimeMillis() // время начала выполнения
    try {
        Awaitility.await()
            .pollInterval(pollInterval)
            .timeout(timeout)
            .until { condition.invoke(this) }
            .also {
                println("Time elapsed: ${(System.currentTimeMillis() - start)} ms")
            }
        return this
    } catch (e: ConditionTimeoutException) {
        throw ConditionTimeoutException("Время ожидания условия истекло, timeout = $timeout", e)
    }
}