package tv.codealong.tutorials.springboot.thenewboston.docprocessor

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

object JacksonMapper {
    val INSTANCE: JsonMapper =
        JsonMapper.builder()
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .build()
            .apply{
                registerModule(KotlinModule.Builder().build())
                registerModule(JavaTimeModule())
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
            }

    val READER: ObjectReader = INSTANCE.reader()
    val WRITER: ObjectWriter = INSTANCE.writer()

}