package tv.codealong.tutorials.various.audit

import org.slf4j.LoggerFactory
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor

class AsyncAuditSaver(
    private val auditSender: AuditSender,
    private val taskExecutor: TaskExecutor = SimpleAsyncTaskExecutor()
) : AuditSaver<IProcessedInvocation> {

    override fun save(event: IProcessedInvocation) {
        taskExecutor.execute {
            try {
                // Асинхронная отправка аудит-события
                auditSender.send(event)
            } catch (ex: Exception) {
                // Логирование ошибок, но без проброса исключения
                // Можно добавить дополнительную логику обработки ошибок
                log.error("Failed to save audit event asynchronously", ex)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AsyncAuditSaver::class.java)
    }
}