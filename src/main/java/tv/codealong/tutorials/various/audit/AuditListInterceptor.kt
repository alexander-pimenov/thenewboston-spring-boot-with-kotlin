package tv.codealong.tutorials.various.audit

import org.aopalliance.aop.Advice

class AuditListInterceptor(
    auditSaver: AuditSaver<IProcessedInvocation>,
    handlers: Map<HandlerName, AuditEventHandler<IProcessedInvocation>>,
    validators: Collection<AuditEventMessageValidator>,
    matchers: Collection<AuditEventMetamodelMatcher>
) : Advice {

}
