package projson

data class JsonPrimitive(

    //Valor armazenado
    val value: Any?

) : JsonValue {

    //Converte para o formato JSON adequado (Strings aspas, numbers/booleans direto e null é "null"
    override fun toString(): String {
        return when (value) {
            is String -> "\"$value\""
            is Number, is Boolean -> value.toString()
            null -> "null"
            //Caso não seja válido, lança exceção
            else -> throw IllegalArgumentException("Valor inválido")
        }
    }

    //Aceita visitor para percorrer a árvore JSON
    override fun accept(visitor: JsonVisitor) {
        visitor.visitPrimitive(this)
    }
}