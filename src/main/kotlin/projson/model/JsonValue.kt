package projson.model

import projson.model.JsonVisitor

sealed interface JsonValue {
    fun accept(visitor: JsonVisitor)
    override fun toString(): String
}