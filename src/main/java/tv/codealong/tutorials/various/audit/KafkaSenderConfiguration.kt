package tv.codealong.tutorials.various.audit

class KafkaSenderConfiguration {
    var name: String? = null
    var baseHeaders: Map<String, String> = emptyMap()
    var routeResolvers: List<TransportRouteConfig> = emptyList()
    var properties: Map<String, String> = emptyMap()

    fun validate() {
        require(name?.isNotBlank() == true) { "Sender name must be specified" }
        require(properties.containsKey("bootstrap.servers")) { "Bootstrap servers must be specified" }
    }
}