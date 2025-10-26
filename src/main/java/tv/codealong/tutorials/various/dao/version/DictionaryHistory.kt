package tv.codealong.tutorials.various.dao.version

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLJsonPGObjectJsonbType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import tv.codealong.tutorials.various.dao.dictionary.Constraint
import tv.codealong.tutorials.various.dao.dictionary.Reference
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "dictionary_history")
@EntityListeners(AuditingEntityListener::class)
class DictionaryHistory(
    @Column(name = "dictionary_id", nullable = false, updatable = false)
    var dictionaryId: UUID?,
    @Column(name = "code", nullable = false, updatable = false)
    var code: String,
    @Column(name = "description", nullable = false, updatable = false)
    var description: String?,
    @Column(name = "row_schema", nullable = false, updatable = false)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType::class)
    var rowSchema: String,

    @Column(name = "constraints", nullable = false, updatable = false)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType::class)
    var constraints: Constraint,

    @Column(name = "references", nullable = false, updatable = false)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType::class)
    var references: Reference,

    @Column(name = "action", updatable = false)
    var action: String? = null,

    @Column(name = "trace_id", updatable = false)
    var traceId: String? = null,

    @Column(name = "updated_by", nullable = false, updatable = false)
    var updatedBy: String?,
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null
) {
}