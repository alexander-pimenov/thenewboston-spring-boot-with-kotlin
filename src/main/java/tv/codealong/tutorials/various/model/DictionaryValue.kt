package tv.codealong.tutorials.various.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import java.time.OffsetDateTime
import java.util.UUID

data class DictionaryValue(
    @get:JsonProperty(value = "id", required = true) val id: UUID,
    @get:JsonProperty(value = "dictionaryVersionId", required = true) val dictionaryVersionId: UUID,

    @field:Valid
    @get:JsonProperty(value = "jsonValue", required = true) val jsonValue: Any,

    @get:JsonProperty(value = "updatedAt", required = true) val updatedAt: OffsetDateTime,
) {}
