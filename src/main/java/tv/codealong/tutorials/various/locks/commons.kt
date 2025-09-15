package tv.codealong.tutorials.various.locks

// Модель данных
data class Data(
    var id: Long = 0,
    var value: String = "",
    var version: Int = 0,
    var timestamp: Long = System.currentTimeMillis()
) {
    fun copyWithNewValue(newValue: String): Data {
        return this.copy(
            value = newValue,
            version = this.version + 1,
            timestamp = System.currentTimeMillis()
        )
    }
}
