package tv.codealong.tutorials.springboot.thenewboston.helper

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions

/*
* https://github.com/arhohuttunen/junit5-examples/blob/main/junit5-gradle-kotlin/src/test/kotlin/com/arhohuttunen/junit5/kotlin/RepeatableAnnotationTest.kt
*
* https://github.com/arhohuttunen/junit5-examples/tree/main/junit5-gradle-kotlin/src/test/kotlin/com/arhohuttunen/junit5/kotlin
*/
@Extensions(
    ExtendWith(SecondBestExtension::class),
    ExtendWith(CoolestEverExtension::class)
)
@Tags(
    Tag("fist"),
    Tag("second")
)
class RepeatableAnnotationTest {
    @Test
    fun `multiple extensions and tags`() {
        println("Inside the test")
    }
}