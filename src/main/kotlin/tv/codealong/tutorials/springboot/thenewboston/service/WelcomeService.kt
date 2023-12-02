package tv.codealong.tutorials.springboot.thenewboston.service

import org.springframework.stereotype.Service

@Service
class WelcomeService {

    fun getWelcomeMessage(name: String): String {
        return "Welcome ${name}!"
    }
}