package tv.codealong.tutorials.springboot.thenewboston.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
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