package tv.codealong.tutorials.various.model

import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class DictionaryList(
    @field:Valid
    @get:Size(max = 1000)
    val list: List<Dictionary>
) {
}