package projson

class JsonObject : JsonValue {

    private val properties = mutableMapOf<String, JsonValue>()

    fun setProperty(key: String, value: JsonValue) {
        properties[key] = value
    }

    fun getProperty(key: String): JsonValue? {
        return properties[key]
    }

    fun removeProperty(key: String) {
        properties.remove(key)
    }

    override fun toString(): String {
        return properties.entries.joinToString(prefix = "{", postfix = "}") { (key, value) -> "\"$key\": $value"
        }
    }
}