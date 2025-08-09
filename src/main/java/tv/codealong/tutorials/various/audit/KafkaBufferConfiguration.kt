package tv.codealong.tutorials.various.audit

class KafkaBufferConfiguration(
    var topic: String?,
    var sender: KafkaBufferSenderConfiguration?,
    var maxConsumerLagLimit: Long?,
    var consumerLagInitialDelayPeriodMs: Long?,
    var consumerLagDelayMs: Long?
) {
    constructor() : this(null, null, null, null, null)

}
