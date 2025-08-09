package tv.codealong.tutorials.various.audit

interface AuditSaver<T : Any> {
    fun save(event: T)
}
