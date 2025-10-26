package tv.codealong.tutorials.various.model

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime
import java.util.UUID

data class Dictionary(
    val id: UUID,
    val tenantId: UUID,
    @get:Size(max = 255)
    val code: String,
    @field:Valid
    val rowSchema: Any,
    @field:Valid
    val constraints: Constraint,

    val updatedAt: OffsetDateTime,
    @get:Size(max = 2000)
    val description: String? = null,
    @field:Valid
    val references: Reference? = null,
) {
}