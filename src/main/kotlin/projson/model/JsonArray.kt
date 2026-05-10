package projson.model

/** Array JSON: lista ordenada de [JsonValue]. */
class JsonArray : JsonValue {

    private val elements = mutableListOf<JsonValue>()

    fun add(value: JsonValue) {
        elements.add(value)
    }

    fun add(value: Any?) {
        elements.add(jsonPrimitiveFromAny(value))
    }

    fun remove(value: JsonValue) {
        elements.remove(value)
    }

    fun get(index: Int): JsonValue {
        return elements[index]
    }

    fun size(): Int = elements.size

    override fun toString(): String {
        return elements.joinToString(prefix = "[", postfix = "]")
    }

    override fun accept(visitor: JsonVisitor) {
        visitor.visitArray(this)

        for (value in elements) {
            value.accept(visitor)
        }
    }

}