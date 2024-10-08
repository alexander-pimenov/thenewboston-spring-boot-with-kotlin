package tv.codealong.tutorials.springboot.thenewboston

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@EnableConfigurationProperties
@SpringBootApplication
class TheNewBostonApplication

fun main(args: Array<String>) {
	runApplication<TheNewBostonApplication>(*args)
}
