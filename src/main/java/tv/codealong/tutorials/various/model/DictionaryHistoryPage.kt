package tv.codealong.tutorials.various.model

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class DictionaryHistoryPage(
    @get:Min(0)
    @get:Max(2147483647)
    val pageNumber: Int,

    @get:Min(0)
    @get:Max(1000)
    val pageSize: Int,

    val next: Boolean,
    val previous: Boolean,

    @get:DecimalMin("0")
    @get:DecimalMax("2147483647")
    val total: BigDecimal,

    @get:Min(0)
    @get:Max(2147483647)
    val totalPage: Int,

    @field:Valid
    @get:Size(max = 1000)
    val list: List<DictionaryHistory>


) {
}