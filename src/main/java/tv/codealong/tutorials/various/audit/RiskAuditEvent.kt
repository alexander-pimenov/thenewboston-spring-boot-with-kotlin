package tv.codealong.tutorials.various.audit

class RiskAuditEvent(
    val metamodelVersion: String?,

) {

    data class RiskAuditEventParams(
        val name: String?,
        val value: String
    )

    data class RiskAuditEventChangedParams(
        val name: String,
        val value: String,
        val oldValue: String
    )

}
