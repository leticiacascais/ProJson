package projson

class JsonArray : JsonValue {

    private val elements = mutableListOf<JsonValue>()

    fun add(value: JsonValue) {
        elements.add(value)
    }

    fun remove(value: JsonValue) {
        elements.remove(value)
    }

    fun get(index: Int): JsonValue {
        return elements[index]
    }

    override fun toString(): String {
        return elements.joinToString(prefix = "[", postfix = "]")
    }
}