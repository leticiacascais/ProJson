package projson.model

import projson.model.JsonValue
import projson.model.JsonVisitor

class JsonArray : JsonValue {

    //Lista de JsonValue
    private val elements = mutableListOf<JsonValue>()

    //Adiciona JsonValue ao Array
    fun add(value: JsonValue) {
        elements.add(value)
    }

    //Adiciona valor kotlin ao Array, sendo automaticamente convertido para JsonPrimitive
    fun add(value: Any?) {
        elements.add(jsonPrimitiveFromAny(value))
    }

    //Remove elemento do Array
    fun remove(value: JsonValue) {
        elements.remove(value)
    }

    //Através do índice, obtém o elemento
    fun get(index: Int): JsonValue {
        return elements[index]
    }

    //Retorna número de elementos no Array
    fun size(): Int = elements.size

    //Converte para formato JSON
    override fun toString(): String {
        return elements.joinToString(prefix = "[", postfix = "]")
    }

    //Aceita visitor para percorrer a árvore JSON
    override fun accept(visitor: JsonVisitor) {
        visitor.visitArray(this)

        for (value in elements) {
            value.accept(visitor)
        }
    }

}