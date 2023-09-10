package tv.codealong.tutorials.springboot.thenewboston.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration
import java.beans.ConstructorProperties


@Configuration
@ConstructorBinding
@ConfigurationProperties(prefix = "another")
class AnotherWebClientProperties(
    val host: String,
    val port: Int,
    val basePath: String,
    val timeout: Long
)