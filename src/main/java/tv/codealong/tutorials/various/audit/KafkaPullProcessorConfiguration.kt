package tv.codealong.tutorials.various.audit

class KafkaPullProcessorConfiguration(
    var topic: String,
    var initialDelayPeriod: Long,
    var delayPeriod: Long,
    var properties: Map<String, String>,
) {
    constructor() : this(
        "",
        0,
        0,
        mutableMapOf()
    )
}
