package tv.codealong.tutorials.various.model

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime
import java.util.UUID

data class DictionaryHistory(
    val dictionaryId: UUID,
    @get:Size(max = 255)
    val code: String,
    val createdAt: OffsetDateTime,
    @get:Size(max = 255)
    val updatedBy: String,
    @get:Size(max = 255)
    val description: String? = null,
    @field:Valid
    val rowSchema: Any? = null,
    @field:Valid
    val constraints: Constraint? = null,
    @field:Valid
    val references: Reference? = null,
    @get:Size(max = 255)
    val action: String? = null,
    @get:Size(max = 255)
    val traceId: String? = null

) {
}