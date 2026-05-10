package projson.model

/** Valor JSON atómico: [String], [Number], [Boolean] ou `null`. */
data class JsonPrimitive(

    val value: Any?

) : JsonValue {

    override fun toString(): String {
        return when (value) {
            is String -> "\"$value\""
            is Number, is Boolean -> value.toString()
            null -> "null"
            else -> throw IllegalArgumentException("Valor inválido")
        }
    }

    override fun accept(visitor: JsonVisitor) {
        visitor.visitPrimitive(this)
    }
}