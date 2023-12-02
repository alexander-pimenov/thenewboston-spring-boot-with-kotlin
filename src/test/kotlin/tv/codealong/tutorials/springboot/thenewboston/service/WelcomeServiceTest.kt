package tv.codealong.tutorials.springboot.thenewboston.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class WelcomeServiceTest {

    private val welcomeService: WelcomeService = WelcomeService()

    @ParameterizedTest(name = "Greeting of {0} should be {1}")
    @MethodSource("names")
    fun `Different names`(input: String, expected: String) {
        assertEquals(expected, welcomeService.getWelcomeMessage(input))
    }

    companion object {
        @JvmStatic
        fun names() = listOf(
            Arguments.of("Sasha", "Welcome Sasha!"),
            Arguments.of("Boss", "Welcome Boss!"),
            Arguments.of("Gosha", "Welcome Gosha!")
        )
    }

    @TestFactory
    fun `Parameterized Different names`() = listOf(
        "Sasha" to "Welcome Sasha!",
        "Boss" to "Welcome Boss!",
        "Gosha" to "Welcome Gosha!"
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("Greeting of $input should be $expected") {
            assertEquals(expected, welcomeService.getWelcomeMessage(input))
        }
    }
}