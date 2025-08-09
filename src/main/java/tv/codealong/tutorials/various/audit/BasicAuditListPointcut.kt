package tv.codealong.tutorials.various.audit

import org.springframework.aop.Pointcut

class BasicAuditListPointcut(val pointcut: Pointcut): AuditListPointcut() {
}