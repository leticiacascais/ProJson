package projson

import projson.model.jsonPrimitiveFromAny

class JsonObject : JsonValue {

    //Propriedade do objeto
    private val properties = mutableMapOf<String, JsonValue>()

    //Adiciona ou altera uma propriedade usando um JsonValue
    fun setProperty(key: String, value: JsonValue) {
        properties[key] = value
    }

    //Adiciona ou altera uma propriedade usando um valor do kotlin, sendo automaticamente convertido para Json
    fun setProperty(key: String, value: Any?) {
        // só permitir primitivos JSON na escrita direta.
        properties[key] = jsonPrimitiveFromAny(value)
    }

    //Obtém o valor associado à chave
    fun getProperty(key: String): JsonValue? {
        return properties[key]
    }

    //Remove propriedade do objeto JSON
    fun removeProperty(key: String) {
        properties.remove(key)
    }

    //Retorna o conjunto de chaves presentes no objeto
    fun keys(): Set<String> = properties.keys

    //Converte para o formato JSON
    override fun toString(): String {
        return properties.entries.joinToString(prefix = "{", postfix = "}") { (key, value) -> "\"$key\": $value"
        }
    }

    //Aceita visitor para percorrer a árvore JSON
    override fun accept(visitor: JsonVisitor) {
        visitor.visitObject(this)

        for (value in properties.values) {
            value.accept(visitor)
        }
    }

}