package projson

data class JsonReference(
    val reference: String
) : JsonValue {

    override fun toString(): String {
        return "{\"" + "$" + "ref\": \"$reference\"}"
    }

    override fun accept(visitor: JsonVisitor) {
        //
    }
}