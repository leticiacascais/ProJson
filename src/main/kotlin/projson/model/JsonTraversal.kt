package projson.model

/**
 * Percorre todos os nós da árvore JSON em profundidade, na ordem definida por
 * [JsonValue.accept] (pai antes dos filhos).
 * @param action chamado por nó 
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
