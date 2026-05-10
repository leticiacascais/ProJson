package projson.model

import projson.model.JsonValue
import projson.model.JsonVisitor

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