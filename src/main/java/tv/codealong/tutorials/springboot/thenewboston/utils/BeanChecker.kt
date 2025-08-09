package tv.codealong.tutorials.springboot.thenewboston.utils

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.stereotype.Component
import java.lang.reflect.Modifier

/**
 * Проверяет beans в контексте приложения.
 * А также пример того, как можно создавать этот бин проверяя наличие проперти в контексте.
 */
@Component
class BeanChecker(private val context: GenericApplicationContext): Condition {

    /**
     * создаем бин только тогда, когда проперти есть в контексте и matches вернет true
     */
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val env = context.environment
        val checkBeansEnabled = env.getProperty("check.beans.enables")
        //можно проверить еще какую-нибудь проперть и на основании их значений выдать true or false
        val mainBalancingGroup = env.getProperty("main.balancing.group")
        return (checkBeansEnabled == "true" && mainBalancingGroup == "DUPLICATE")
                || (checkBeansEnabled == null && mainBalancingGroup == "DUPLICATE")
    }
    /**
     * Проверка beans in the Application context
     */
    fun checkBean() {
        println("BeanChecker is working. Check beans:")
        println("Total Beans: ${context.beanDefinitionNames.size}")
        println("Total Beans: ${context.beanDefinitionCount}")
        context.beanDefinitionNames.forEach {
            val beanClass: Class<*>? = context.getType(it)
            if (beanClass != null) {
                if (!Modifier.isAbstract(beanClass.modifiers) && !Modifier.isInterface(beanClass.modifiers)) {
                    //if (beanClass.isAnnotationPresent(Component::class.java)) {
                    println("Found Bean Name: $it with class: ${beanClass.name}")
                }

                if (it.isNotEmpty()) {
                    println("Bean Name (еще так): $it")

                }

            } else {
                println("No Bean found for name: $it")

            }
        }
    }


}