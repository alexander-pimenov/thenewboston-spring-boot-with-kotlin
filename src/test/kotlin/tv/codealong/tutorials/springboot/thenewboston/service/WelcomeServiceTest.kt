package tv.codealong.tutorials.springboot.thenewboston.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class WelcomeServiceTest {

    private val welcomeService: WelcomeService = WelcomeService()

    @ParameterizedTest(name = "Greeting of {0} should be {1}")
    @MethodSource("squares")
    fun `Different names`(input: String, expected: String) {
        assertEquals(expected, welcomeService.getWelcomeMessage(input))
    }

    companion object {
        @JvmStatic
        fun squares() = listOf(
            Arguments.of("Sasha", "Welcome Sasha!"),
            Arguments.of("Boss", "Welcome Boss!"),
            Arguments.of("Gosha", "Welcome Gosha!")
        )
    }
}