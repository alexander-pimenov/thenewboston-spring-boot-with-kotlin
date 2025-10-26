package tv.codealong.tutorials.various.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

/**
 * Dictionary history list
 */
data class DictionaryHistoryList(

    @field:Valid
    @get:Size(max = 10000)
    @get:JsonProperty("list", required = true) val list: List<DictionaryHistory>

) {

}