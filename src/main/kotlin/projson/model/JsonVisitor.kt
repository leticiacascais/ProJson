package projson.model

//Visitor para travessia do modelo JSON
interface JsonVisitor {

    fun visitPrimitive(value: JsonPrimitive)
    fun visitObject(value: JsonObject)
    fun visitArray(value: JsonArray)
    fun visitReference(value: JsonReference)

}