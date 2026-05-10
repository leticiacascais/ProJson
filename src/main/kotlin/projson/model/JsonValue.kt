package projson.model

import projson.model.JsonVisitor

//Raiz do modelo JSON (Composite)
sealed interface JsonValue {
    fun accept(visitor: JsonVisitor)
    override fun toString(): String
}