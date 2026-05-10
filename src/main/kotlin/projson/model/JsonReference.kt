package projson.model

/** Referência a outro objeto serializado com o mesmo identificador em `"$id"`. */
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