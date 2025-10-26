package tv.codealong.tutorials.various.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.UUID
import java.time.OffsetDateTime

data class Tenant(

    @get:JsonProperty("id", required = true) val id: UUID,

    @get:Size(max = 255)
    @get:JsonProperty("resourceName", required = true) val resourceName: String,

    @get:JsonProperty("updatedAt", required = true) val updatedAt: OffsetDateTime,

    @get:Size(max = 2000)
    @get:JsonProperty("description") val description: String?

){

}