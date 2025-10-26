package tv.codealong.tutorials.various.model

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime
import java.util.UUID

data class DictionaryValueView(
    val id: UUID,
    val `value`: Any,
    val versionId: UUID,
    @get:Pattern(regexp = "^v(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$|latest")
    @get:Size(max = 255)
    val version: String,
    val dictionaryId: UUID,
    @get:Size(max = 255)
    val code: String,
    @field:Valid
    val rowSchema: Any,
    @field:Valid
    val constraints: Constraint,
    @field:Valid
    val references: Reference,
    val tenantId: UUID,
    val resourceName: String,
    val updatedAt: OffsetDateTime,

    )
