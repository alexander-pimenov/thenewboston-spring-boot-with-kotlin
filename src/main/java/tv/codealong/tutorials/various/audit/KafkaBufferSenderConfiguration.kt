package tv.codealong.tutorials.various.audit

class KafkaBufferSenderConfiguration(
    var name: String? = null,
    var properties: Map<String, String>,
    var baseHeaders: Map<String, String>,
    var maxConsumerLagLimit: Long
) {
    constructor() : this("", emptyMap(), emptyMap(), 0)

}
