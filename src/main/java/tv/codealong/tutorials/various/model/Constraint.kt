package tv.codealong.tutorials.various.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Size

data class Constraint(
    @get:Size(max = 100)
    @get:JsonProperty("list", required = true) val list: List<String>
) {
}