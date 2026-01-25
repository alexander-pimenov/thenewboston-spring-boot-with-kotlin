package tv.codealong.tutorials.springboot.thenewboston.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ExtensionsTest {

    @Test
    fun `should mask string version to int`() {
        assertEquals(66850, "v1.5.34".maskVersion())
    }

    @Test
    fun `should unmask string version to int`() {
        assertEquals("v1.5.34", 66850.unmaskVersion())
    }
}