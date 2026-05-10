package projson.model

/** Objeto JSON: mapa de chaves (string) para [JsonValue]. */
class JsonObject : JsonValue {

    private val properties = mutableMapOf<String, JsonValue>()

    fun setProperty(key: String, value: JsonValue) {
        properties[key] = value
    }

    fun setProperty(key: String, value: Any?) {
        properties[key] = jsonPrimitiveFromAny(value)
    }

    fun getProperty(key: String): JsonValue? {
        return properties[key]
    }

    fun removeProperty(key: String) {
        properties.remove(key)
    }

    fun keys(): Set<String> = properties.keys

    override fun toString(): String {
        return properties.entries.joinToString(prefix = "{", postfix = "}") { (key, value) -> "\"$key\": $value"
        }
    }

    override fun accept(visitor: JsonVisitor) {
        visitor.visitObject(this)
        for (value in properties.values) {
            value.accept(visitor)
        }
    }

}