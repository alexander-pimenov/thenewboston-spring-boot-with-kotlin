package tv.codealong.tutorials.various.audit

import org.springframework.aop.ClassFilter
import org.springframework.aop.MethodMatcher
import org.springframework.aop.Pointcut

open class AuditListPointcut : Pointcut {
    override fun getClassFilter(): ClassFilter {
        TODO("Not yet implemented")
    }

    override fun getMethodMatcher(): MethodMatcher {
        TODO("Not yet implemented")
    }

}
