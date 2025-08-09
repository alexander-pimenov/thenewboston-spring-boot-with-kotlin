package tv.codealong.tutorials.springboot.thenewboston.utils

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class BeanCheck(private val beanChecker: BeanChecker) {
    @PostConstruct
    fun beanCheck() {
        beanChecker.checkBean()
    }
}