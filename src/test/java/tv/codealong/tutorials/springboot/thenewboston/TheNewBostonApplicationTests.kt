package tv.codealong.tutorials.springboot.thenewboston

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import tv.codealong.tutorials.springboot.thenewboston.utils.SpringContext

@SpringBootTest
@ContextConfiguration(classes = [TheNewBostonApplication::class, SpringContext::class])
class TheNewBostonApplicationTests {

    @Test
    fun contextLoads() {
    }

}
