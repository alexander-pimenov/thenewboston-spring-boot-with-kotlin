package tv.codealong.tutorials.various.audit

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig {

    /**
     * Если вам нужен специальный ObjectMapper только для Kafka:
     */
    @Bean("kafkaObjectMapper")
    fun kafkaObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }

    //Это для примера, если нужно передавать бин с указанием Qualifier
//    @Bean
//    fun kafkaSender(
//        kafkaSenderConfiguration: KafkaSenderConfiguration,
//        pvmSdkMonitoringService: PvmSdkMonitoringService,
//        @Qualifier("kafkaObjectMapper") objectMapper: ObjectMapper
//    ) = KafkaSender(kafkaSenderConfiguration, pvmSdkMonitoringService, objectMapper)
}