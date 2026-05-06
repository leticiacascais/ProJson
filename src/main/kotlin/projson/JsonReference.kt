package projson

// (Fase 2): nó especial que representa `{"$ref": "<uuid>"}`. 
data class JsonReference(
    val reference: String
) : JsonValue {

    override fun toString(): String {
        return "{\"" + "$" + "ref\": \"$reference\"}"
    }

    override fun accept(visitor: JsonVisitor) {
        visitor.visitReference(this)
    }
}