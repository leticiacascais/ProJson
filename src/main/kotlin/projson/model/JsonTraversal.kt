package projson.model

/**
 * Percorre todos os nós da árvore JSON em profundidade, na ordem definida por
 * [JsonValue.accept] (pai antes dos filhos).
 *
 * Exemplo: contar primitivos
 * ```kotlin
 * var n = 0
 * json.forEachDepthFirst { if (it is JsonPrimitive) n++ }
 * ```
 */
fun JsonValue.forEachDepthFirst(action: (JsonValue) -> Unit) {
    accept(
        object : JsonVisitor {
            override fun visitPrimitive(value: JsonPrimitive) {
                action(value)
            }

            override fun visitObject(value: JsonObject) {
                action(value)
            }

            override fun visitArray(value: JsonArray) {
                action(value)
            }

            override fun visitReference(value: JsonReference) {
                action(value)
            }
        }
    )
}
