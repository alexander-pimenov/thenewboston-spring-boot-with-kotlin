package tv.codealong.tutorials.springboot.thenewboston.controller

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tv.codealong.tutorials.springboot.thenewboston.service.WelcomeService

/*
Spring Boot Testing a REST Controller with Unit, Integration & Acceptance Tests
https://www.youtube.com/watch?v=aEW8ZH6wj2o&t=2s
https://github.com/kriscfoster/spring-boot-testing-pyramid/tree/master/src
*/
@WebMvcTest(WelcomeController::class)
internal class WelcomeControllerIntegrationTest @Autowired constructor(private val mockMvc: MockMvc) {

    //можно и так инжектить, а не через конструктор
//    @Autowired
//    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var welcomeService: WelcomeService

    @Test
    fun shouldGetDefaultWelcomeMessage() {
        `when`(welcomeService.getWelcomeMessage("Stranger")).thenReturn("Welcome Stranger!")
        mockMvc.perform(get("/welcome"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().string(equalTo("Welcome Stranger!")))
        verify(welcomeService).getWelcomeMessage("Stranger")
    }

    @Test
    fun shouldGetCustomWelcomeMessage() {
        `when`(welcomeService.getWelcomeMessage("John")).thenReturn("Welcome John!")
        mockMvc.perform(get("/welcome?name=John"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().string(equalTo("Welcome John!")))
        verify(welcomeService).getWelcomeMessage("John")
    }
}