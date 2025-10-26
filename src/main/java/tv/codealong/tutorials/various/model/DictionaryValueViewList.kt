package tv.codealong.tutorials.various.model

import jakarta.validation.Valid
import jakarta.validation.constraints.Size


data class DictionaryValueViewList(
    @field:Valid
    @get:Size(min = 10000)
    val list: List<DictionaryValueView>
) {
}