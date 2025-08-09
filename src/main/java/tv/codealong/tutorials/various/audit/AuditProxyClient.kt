package tv.codealong.tutorials.various.audit

// AuditProxyClient - вероятно, клиент для отправки аудит-событий
class AuditProxyClient private constructor(
    private val baseUrl: String,
    private val nodeId: String,
    private val pvmSdkConfiguration: PvmSdkConfiguration,
    private val senders: List<Any>, // HttpSender, KafkaSender или KafkaBuffer
    val balancingSender: BalancingSender
) {

    data class Builder(
        var baseUrl: String = "",
        var nodeId: String = "",
        private var pvmSdkConfiguration: PvmSdkConfiguration? = null,
        private var senders: List<Any> = emptyList()
    ) {
        fun baseUrl(baseUrl: String) = apply { this.baseUrl = baseUrl }
        fun nodeId(nodeId: String) = apply { this.nodeId = nodeId }
        fun setPvmSdkConfiguration(config: PvmSdkConfiguration, senders: List<Any>) = apply {
            this.pvmSdkConfiguration = config
            this.senders = senders
        }

        fun build(): AuditProxyClient {
            require(baseUrl.isNotBlank()) { "baseUrl must be set" }
            require(nodeId.isNotBlank()) { "nodeId must be set" }
            requireNotNull(pvmSdkConfiguration) { "pvmSdkConfiguration must be set" }

            return AuditProxyClient(baseUrl, nodeId, pvmSdkConfiguration!!, senders, BalancingSender())
        }
    }

    companion object {
        fun builder() = Builder()
    }

    // Дополнительные методы для работы с клиентом
}

