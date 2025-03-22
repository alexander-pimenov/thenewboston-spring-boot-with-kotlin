package tv.codealong.tutorials.springboot.thenewboston.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tv.codealong.tutorials.springboot.thenewboston.domain.Deal
import tv.codealong.tutorials.springboot.thenewboston.domain.DealRequest
import tv.codealong.tutorials.springboot.thenewboston.domain.DetDealRq
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/hello")
class HelloWorldController {

    @GetMapping("/1")
    @ResponseStatus(HttpStatus.OK)
    fun helloWorld1(): String =
        "Hello, this is a REST endpoint! It's ${parseTimeToString(LocalDateTime.now())} o'clock now."

    @GetMapping(value = ["/2"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun helloWorld2(): ResponseEntity<String> =
        ResponseEntity.ok().body("Hello, this is a REST endpoint! It's ${parseTimeToString(LocalDateTime.now())} o'clock now.")

    @GetMapping(value = ["/3"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun helloWorld3(): ResponseEntity<*> {
        val time = LocalDateTime.now()
        val req = DealRequest(
            "b9a2af82a76e4c928b71c2fd0c70dd95",
            DetDealRq(
                "b9a2af82a76e4c928b71c2fd0c70dd95",
                time, Deal("YGT-1GH1")
            )
        )
        return ResponseEntity.ok().body(req)
    }

    @PostMapping(value = ["/deal"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun helloWorld4(@RequestBody request: DealRequest): ResponseEntity<*> {
        println(request)
        val map = LinkedHashMap<String, String>()
        map["RqUID"] = request.rqUID
        map["messageID"] = request.getDealRq.messageID
        map["cfleID"] = request.getDealRq.deal.cfleID
        return ResponseEntity.ok().body(map)
    }

    /**
     * Преобразовывает строковое представление времени вида "1986-04-08 12:30:15" в тип LocalDateTime
     */
    fun parseStringToTime(strTime: String): LocalDateTime {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT)
        return LocalDateTime.parse(strTime, formatter)
    }

    /**
     * Преобразовывает LocalDateTime в строковое представление времени вида "1986-04-08 12:30:15"
     */
    fun parseTimeToString(time: LocalDateTime): String {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT)
        return formatter.format(time)
    }

    companion object {
        private const val TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    }
}