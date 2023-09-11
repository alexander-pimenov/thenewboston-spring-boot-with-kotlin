package tv.codealong.tutorials.springboot.thenewboston.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import tv.codealong.tutorials.springboot.thenewboston.domain.Deal
import tv.codealong.tutorials.springboot.thenewboston.domain.DealRequest
import tv.codealong.tutorials.springboot.thenewboston.domain.DetDealRq
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
@SpringJUnitConfig
internal class ObjectMapperTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun generateUidTest() {
        println(UUID.randomUUID().toString())

        val time = LocalDateTime.now()

        val req = DealRequest(
            "b9a2af82a76e4c928b71c2fd0c70dd95",
            DetDealRq(
                "b9a2af82a76e4c928b71c2fd0c70dd95",
                time,
                Deal("YGT-1GH1")
            )
        )

        val json = objectMapper.writeValueAsString(req)
        println(json)

        val res1 = objectMapper.readValue(json, DealRequest::class.java)
        println(res1)

        val res2 = objectMapper.readValue(str, DealRequest::class.java)
        println(res2)

    }

    val str = """
        {
          "RqUID": "b9a2af82a76e4c928b71c2fd0c70dd95",
          "getDealRq": {
            "messageID": "b9a2af82a76e4c928b71c2fd0c70dd95",
            "messageDT": "2023-09-11T21:37:04",
        	"deal": {
        	  "cfleID": "YGT-1GH1"
        	}
          }
        }
    """.trimIndent()
}
