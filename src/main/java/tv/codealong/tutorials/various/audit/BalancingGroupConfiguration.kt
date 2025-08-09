package tv.codealong.tutorials.various.audit

data class BalancingGroupConfiguration(
    var name: String,
    var isMain: Boolean,
    var providerClass: String,
    var breakerConfiguration: CircuitBreakerConfiguration,
    var priority: Int,
    var providerNameToWeight: MutableMap<String, Int> = mutableMapOf()

) {
    // Конструктор по умолчанию, который установит значения по умолчанию
    constructor() : this(
        "",
        false,
        "",
        CircuitBreakerConfiguration(),
        0
    )

}
