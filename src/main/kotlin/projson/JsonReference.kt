package projson

//{"$ref": "<uuid>"}, isto é, o objeto aponta/usa para outros
data class JsonReference(

    //Identificador do objeto referenciado
    val reference: String

) : JsonValue {

    //Converte a ref para a forma JSON correta
    override fun toString(): String {
        return "{\"" + "$" + "ref\": \"$reference\"}"
    }

    //Aceita visitor para percorrer a árvore JSON
    override fun accept(visitor: JsonVisitor) {
        visitor.visitReference(this)
    }
}