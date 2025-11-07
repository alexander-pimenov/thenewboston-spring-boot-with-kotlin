package tv.codealong.tutorials.springboot.thenewboston.utils

import com.fasterxml.jackson.databind.json.JsonMapper

@JvmInline
value class JsonString(
    val value: String,
) {
    inline fun <reified T> deserialize(jsonMapper: JsonMapper = JacksonMapper.INSTANCE): T =
        jsonMapper.readValue(value, T::class.java)

    fun asMap(): Map<*, *> = JacksonMapper.INSTANCE.readValue(value, Map::class.java)

    companion object {
        @Suppress("unused")
        val EMPTY_OBJ = JsonString("{}") //

        @Suppress("unused")
        val EMPTY_ARR = JsonString("[]")

        fun serialize(obj: Any, jsonMapper: JsonMapper = JacksonMapper.INSTANCE): JsonString =
            JsonString(jsonMapper.writeValueAsString(obj))

        fun serializeIfNotString(obj: Any, jsonMapper: JsonMapper = JacksonMapper.INSTANCE): JsonString =
            if (obj is String) JsonString(obj) else serialize(obj, jsonMapper)
    }
}