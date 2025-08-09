package tv.codealong.tutorials.various.audit

import org.springframework.aop.Pointcut

class BasicAuditPointcut(
    val pointcut: Pointcut
) : AuditPointcut() {
}