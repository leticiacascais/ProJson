package projson.model

/**
 * Converte um valor Kotlin para [JsonPrimitive] (ou devolve [JsonValue] se já for modelo JSON).
 * Lança se o tipo não for JSON válido na escrita direta.
 */
fun jsonPrimitiveFromAny(value: Any?): JsonValue {
    if (value is JsonValue) return value
    return when (value) {
        null -> JsonPrimitive(null)
        is String, is Number, is Boolean -> JsonPrimitive(value)
        else -> throw IllegalArgumentException("Valor inválido")
    }
}

