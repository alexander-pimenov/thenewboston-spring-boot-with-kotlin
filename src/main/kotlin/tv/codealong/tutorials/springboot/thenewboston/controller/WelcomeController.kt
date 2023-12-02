package tv.codealong.tutorials.springboot.thenewboston.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tv.codealong.tutorials.springboot.thenewboston.service.WelcomeService


@RestController
class WelcomeController(private val welcomeService: WelcomeService) {


    @GetMapping("/welcome")
    fun welcome(@RequestParam(defaultValue = "Stranger") name: String): String {
        return welcomeService.getWelcomeMessage(name)
    }
}