package projson.model

/**
 * Visitante para percorrer uma árvore [JsonValue] (padrão Visitor / Composite).
 *
 * Todos os métodos têm implementação vazia por omissão, para poder
 * sobrescrever só o que precisar. A travessia em profundidade é feita
 * por [JsonValue.accept]: cada nó notifica o visitante e, em seguida,
 * delega nos filhos (por exemplo, [JsonObject] visita as propriedades).
 */
interface JsonVisitor {

    /** Chamado para cada [JsonPrimitive] (string, número, boolean ou null). */
    fun visitPrimitive(value: JsonPrimitive) {}

    /** Chamado para cada [JsonObject], antes dos valores das propriedades. */
    fun visitObject(value: JsonObject) {}

    /** Chamado para cada [JsonArray], antes dos elementos. */
    fun visitArray(value: JsonArray) {}

    /** Chamado para cada [JsonReference] (`{ "$ref": "..." }`). */
    fun visitReference(value: JsonReference) {}
}