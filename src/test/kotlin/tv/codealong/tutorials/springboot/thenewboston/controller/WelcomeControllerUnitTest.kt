package tv.codealong.tutorials.springboot.thenewboston.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import tv.codealong.tutorials.springboot.thenewboston.service.WelcomeService

/*
Spring Boot Testing a REST Controller with Unit, Integration & Acceptance Tests
https://www.youtube.com/watch?v=aEW8ZH6wj2o&t=2s
https://github.com/kriscfoster/spring-boot-testing-pyramid/tree/master/src
*/
@DisplayName("Пример Unit-тестирования контроллера")
internal class WelcomeControllerUnitTest{

    lateinit var welcomeController: WelcomeController

    @BeforeEach
    fun setup() {
        val welcomeService: WelcomeService = Mockito.mock(WelcomeService::class.java)
        `when`(welcomeService.getWelcomeMessage("Stranger")).thenReturn("Welcome Stranger!")
        `when`(welcomeService.getWelcomeMessage("John")).thenReturn("Welcome John!")
        welcomeController = WelcomeController(welcomeService)
    }

    @Test
    fun shouldGetDefaultWelcomeMessage() {
        assertEquals("Welcome Stranger!", welcomeController.welcome("Stranger"))
    }

    @Test
    fun shouldGetCustomWelcomeMessage() {
        assertEquals("Welcome John!", welcomeController.welcome("John"))
    }
}