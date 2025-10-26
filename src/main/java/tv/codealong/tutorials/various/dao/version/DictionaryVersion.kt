package tv.codealong.tutorials.various.dao.version

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcType
import org.hibernate.annotations.SQLRestriction
import org.hibernate.dialect.PostgreSQLEnumJdbcType
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "dictionary_version")
@SQLRestriction("deleted_at is null")
open class DictionaryVersion(
    @Column(name = "dictionary_id", nullable = false, updatable = false)
    var dictionaryId: UUID?,
    @Column(name = "dictionary_history_id", nullable = false, updatable = false)
    var dictionaryHistoryId: UUID?,
    @Column(name = "version", nullable = false, updatable = false)
    var version: String,
    @Column(name = "version_code", nullable = false, updatable = false)
    var versionCode: Int,
    @Column(name = "default")
    var default: Boolean = false,

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    var status: DictionaryStatus,
    @Column(name = "begin_date")
    var beginDate: Instant? = null,
    @Column(name = "end_date")
    var endDate: Instant? = null,

    ) {
}