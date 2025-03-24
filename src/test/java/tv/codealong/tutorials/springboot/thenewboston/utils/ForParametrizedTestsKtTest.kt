package tv.codealong.tutorials.springboot.thenewboston.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

class ForParametrizedTestsKtTest {
    @ParameterizedTest
    @ValueSource(ints = [0, 2, 4, 6, 8])
    fun `test event numbers`(number: Int) {
        assertTrue(isEven(number))
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 3, 5, 7])
    fun `test odd numbers`(number: Int) {
        assertTrue(isOdd(number))
    }

    @ParameterizedTest(name = "Тест на сложение числа {0} и числа {1}. Результат должен быть {2}")
    @CsvSource(
        "1, 2, 3",
        "2, 2, 4",
        "3, 2, 5",
        "4, 2, 6",
    )
    fun `test adding numbers`(a: Int, b: Int, expected: Int) {
        assertEquals(expected, add(a, b))
    }

    @ParameterizedTest(name = "Тест на палиндром для ''{0}''")
    @CsvSource(
        "1, true",
        "121, true",
        "madam, true",
        "level, true",
    )
    fun `palindrome test`(input: String, expected: Boolean) {
        assertEquals(expected, isPalindrome(input))
    }

    @ParameterizedTest(name = "Тест на палиндром для ''{0}''")
    @CsvSource(
        "kayak",
        "121",
        "madam",
        "level",
    )
    fun `palindrome test `(input: String) {
        assertTrue(isPalindrome(input))
    }

    @ParameterizedTest(name = "Тест на палиндром для ''{0}''")
    @MethodSource("providePalindromes")
    fun `palindrome test 2`(input: String) {
        assertTrue(isPalindrome(input))
    }

    companion object {
        @JvmStatic
        fun providePalindromes(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("kayak"),
                Arguments.of("121"),
                Arguments.of("madam"),
                Arguments.of("level"),
                Arguments.of("12321")
            )
        }
    }


}