package tv.codealong.tutorials.springboot.thenewboston.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

/**
 * Так можно настроить один глобальный объект для всех мапперов и использовать его,
 * где нужно. Но конечно же можно использовать и мапперы настроенные в JacksonConfig.
 */
object JacksonMapper {

    val INSTANCE: JsonMapper =
        JsonMapper.builder()
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)

            .build()
            .apply {
                registerModule(KotlinModule.Builder().build())
                registerModule(JavaTimeModule())
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
            }
}