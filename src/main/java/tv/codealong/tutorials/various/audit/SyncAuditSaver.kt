package tv.codealong.tutorials.various.audit

import org.slf4j.LoggerFactory

class SyncAuditSaver(
    private val auditSender: AuditSender
) : AuditSaver<IProcessedInvocation> {

    override fun save(event: IProcessedInvocation) {
        try {
            // Синхронная отправка аудит-события
            auditSender.send(event)
        } catch (ex: Exception) {
            // Обработка ошибок при синхронной отправке
            throw AuditSavingException("Failed to save audit event synchronously", ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}