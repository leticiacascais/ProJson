package projson


// (Fase 1): raiz do modelo JSON (Composite). 
sealed interface JsonValue {
    fun accept(visitor: JsonVisitor)
    override fun toString(): String
}