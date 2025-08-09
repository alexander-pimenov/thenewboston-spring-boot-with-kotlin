package tv.codealong.tutorials.springboot.thenewboston.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.Configuration
import java.beans.ConstructorProperties


//@ConstructorBinding - его лучше использовать уже внутри (как показано ниже),
// чтобы создать проверку мапинга пропертей из application.yml на свойства класса.
@ConfigurationProperties(prefix = "another")
class AnotherWebClientProperties(
    val host: String,
    val port: Int,
    val basePath: String,
    val timeout: Long
) {
    @ConstructorBinding
    constructor(host: String?, port: Int?, basePath: String?, timeout: Long?) : this(
        host = requireNotNull(host) {
            "another.host: Host is required"
        },
        port = requireNotNull(port) {
            "another.port: Port is required"
        },
        basePath = requireNotNull(basePath) {
            "another.basePath: Base path is required"
        },
        timeout = requireNotNull(timeout) {
            "another.timeout: Timeout is required"
        }
    ) {
        //тут можно добавить еще проверки:

        require(host.startsWith("http") || host.startsWith("https")) {
            "Host must start with http or https"
        }
        require(basePath.isNotEmpty()) {
            "BasePath must not be empty"
        }
        require(timeout > 0) {
            "Timeout must be greater than 0"
        }
    }
}