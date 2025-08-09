package tv.codealong.tutorials.various.audit

import java.util.regex.Pattern

data class TransportRouteConfig(
    val headerName: String,
    val pattern: Pattern,
    val topic: String,
    val isDefault: Boolean = false
) {
    init {
        require(headerName.isNotBlank()) { "Header name must not be blank" }
        require(topic.isNotBlank()) { "Topic must not be blank" }
    }
}