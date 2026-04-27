package projson


sealed interface JsonValue {
    fun accept(visitor: JsonVisitor)
    override fun toString(): String
}