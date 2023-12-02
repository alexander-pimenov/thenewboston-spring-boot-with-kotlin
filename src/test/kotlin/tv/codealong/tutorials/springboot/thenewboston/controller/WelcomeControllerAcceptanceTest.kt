package tv.codealong.tutorials.springboot.thenewboston.controller

import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.client.RestTemplate

/*
Spring Boot Testing a REST Controller with Unit, Integration & Acceptance Tests
https://www.youtube.com/watch?v=aEW8ZH6wj2o&t=2s
https://github.com/kriscfoster/spring-boot-testing-pyramid/tree/master/src
*/
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
internal class WelcomeControllerAcceptanceTest @Autowired constructor(private val mockMvc: MockMvc) {

    @LocalServerPort
    var randomServerPort = 0

    private var restTemplate: RestTemplate? = null
    private var url: String? = null

    @BeforeEach
    fun setUp() {
        restTemplate = RestTemplate()
        url = "http://localhost:$randomServerPort/welcome"
    }

    @Test
    fun shouldGetDefaultWelcomeMessage() {
        val responseEntity: ResponseEntity<*> = restTemplate!!.getForEntity(url!!, String::class.java)
        println(responseEntity)
        assertEquals(OK, responseEntity.getStatusCode())
        assertEquals("Welcome Stranger!", responseEntity.getBody())
    }

    @Test
    fun shouldGetCustomWelcomeMessage() {
        val responseEntity: ResponseEntity<*> = restTemplate!!.getForEntity("$url?name=John", String::class.java)
        println(responseEntity)
        assertEquals(OK, responseEntity.statusCode)
        assertEquals("Welcome John!", responseEntity.body)
    }

    @Test
    fun shouldGetDefaultWelcomeMessage2() {
        mockMvc.perform(MockMvcRequestBuilders.get("/welcome"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(Matchers.equalTo("Welcome Stranger!")))
    }

    @Test
    fun shouldGetCustomWelcomeMessage2() {
        mockMvc.perform(MockMvcRequestBuilders.get("/welcome?name=John"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(Matchers.equalTo("Welcome John!")))
    }
}