package projson.model

import projson.model.JsonPrimitive
import projson.model.JsonValue

fun jsonPrimitiveFromAny(value: Any?): JsonValue {
    if (value is JsonValue) return value
    return when (value) {
        null -> JsonPrimitive(null)
        is String, is Number, is Boolean -> JsonPrimitive(value)
        else -> throw IllegalArgumentException("Valor inválido")
    }
}

