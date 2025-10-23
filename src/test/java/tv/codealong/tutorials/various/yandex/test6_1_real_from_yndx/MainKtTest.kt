package tv.codealong.tutorials.various.yandex.test6_1_real_from_yndx

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.text.contains

class CalculatorTest {

    private lateinit var calculator: Calculator

    @BeforeEach
    fun setUp() {
        calculator = Calculator()
    }

    @Test
    fun `should add two numbers correctly`() {
        // Given
        val a = 5
        val b = 3

        // When
        val result = calculator.add(a, b)

        // Then
        assertEquals(8, result)
    }

    @Test
    fun `should multiply two numbers correctly`() {
        assertEquals(15, calculator.multiply(5, 3))
        assertEquals(0, calculator.multiply(5, 0))
        assertEquals(-10, calculator.multiply(5, -2))
    }
}

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService()
    }

    @Test
    fun `should create user with auto-increment id`() {
        // When
        val user1 = userService.createUser("Alice", "alice@example.com")
        val user2 = userService.createUser("Bob", "bob@example.com")

        // Then
        assertEquals(1, user1.id)
        assertEquals("Alice", user1.name)
        assertEquals("alice@example.com", user1.email)

        assertEquals(2, user2.id)
        assertEquals("Bob", user2.name)
    }

    @Test
    fun `should create user with correct email`() {
        val user = userService.createUser("John", "john.doe@example.com")
        assertTrue(user.email.contains("@"))
        assertEquals("john.doe@example.com", user.email)
    }
}

// тест для примера
class LimitSystemTest {

    @Test
    fun `should process payment correctly`() {

        assertTrue(true) // заглушка
    }
}