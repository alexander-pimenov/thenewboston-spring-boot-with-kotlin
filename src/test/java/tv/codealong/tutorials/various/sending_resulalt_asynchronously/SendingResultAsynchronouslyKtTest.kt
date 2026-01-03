package tv.codealong.tutorials.various.sending_resulalt_asynchronously

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.concurrent.locks.ReentrantLock

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SendingResultAsynchronouslyKtTes {

   // private lateinit var wireMockServer: WireMockServer

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    companion object {
        @RegisterExtension
        val wireMockServer: WireMockExtension = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().dynamicPort())
            .build()
    }


//    @BeforeAll
//    fun setup() {
//        wireMockServer = WireMockServer(WireMockConfiguration.options().port(8099))
//        wireMockServer.start()
//        configureFor("localhost", 8099)
//
//
//    }

//    @AfterAll
//    fun teardown() {
//        wireMockServer.stop()
//    }

//    @BeforeEach
//    fun setup() {
//        wireMockServer.start()
//
//    }

    @AfterEach
    fun teardown() {
        wireMockServer.resetAll()
    }

    @Test
    fun `should send task result to API`() = runBlocking {
        // given
        wireMockServer.stubFor(
            post(urlEqualTo("/task/result"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200))
        )

        val lockA = ReentrantLock()
        val lockB = ReentrantLock()
        val lockC = ReentrantLock()

        launchTask(1, listOf(lockA, lockB))
        launchTask(2, listOf(lockB, lockC))
        launchTask(3, listOf(lockA, lockC))

//        wireMockServer.verify(
//            postRequestedFor(urlEqualTo("/task/result"))
//                .withRequestBody(containing("\"taskId\":42"))
//                .withRequestBody(containing("\"success\":true"))
//        )

        //можно получить список всех запросов:
        val requests = wireMockServer.findAll(postRequestedFor(urlEqualTo("/task/result")))
        requests.forEach {
            println("Запрос: ${it.bodyAsString}")
        }
    }
}

// when
//        val response: HttpResponse = client.post("http://localhost:8099/task/result") {
//            contentType(ContentType.Application.Json)
//            setBody(TaskResult(42, true))
//        }

// then
//assertEquals(200, response.status.value)
