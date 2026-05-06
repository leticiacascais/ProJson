package projson

import projson.model.jsonPrimitiveFromAny

// (Fase 1): JSON object (Map<String, JsonValue>) com API de leitura/escrita. 
class JsonObject : JsonValue {

    private val properties = mutableMapOf<String, JsonValue>()

    fun setProperty(key: String, value: JsonValue) {
        properties[key] = value
    }

    fun setProperty(key: String, value: Any?) {
        // só permitir primitivos JSON na escrita direta.
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