package projson.model

import projson.JsonPrimitive
import projson.JsonValue

/**
 * Converte valores Kotlin “simples” num `JsonValue` válido.
 *
 * Motivação (Phase 1 - JSON manipulation / evitar JSON inválido):
 * o utilizador pode chamar APIs como `JsonObject.setProperty("x", 1)` e `JsonArray.add("a")`
 * (como no enunciado) sem ter de criar manualmente `JsonPrimitive(...)`.
 *
 * Regras:
 * - `null`, `String`, `Number`, `Boolean` → `JsonPrimitive`
 * - `JsonValue` → devolve tal como está
 * - outros tipos → erro (não é primitivo JSON)
 */
fun jsonPrimitiveFromAny(value: Any?): JsonValue {
    if (value is JsonValue) return value
    return when (value) {
        null -> JsonPrimitive(null)
        is String, is Number, is Boolean -> JsonPrimitive(value)
        else -> throw IllegalArgumentException("Valor inválido")
    }
}

