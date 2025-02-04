package tv.codealong.tutorials.springboot.thenewboston.docprocessor

@JvmInline
value class JsonString(
    val value: String
) {
    inline fun <reified T> deserialize(): T =
        JacksonMapper.INSTANCE.readValue(value, T::class.java)

    companion object {
        val EMPTY = JsonString("{}")

        fun serialize(obj: Any): JsonString =
            JsonString(JacksonMapper.INSTANCE.writeValueAsString(obj))
    }
}