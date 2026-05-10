package projson.model

/** Raiz do modelo JSON em memória (Composite). */
sealed interface JsonValue {
    fun accept(visitor: JsonVisitor)
    override fun toString(): String
}