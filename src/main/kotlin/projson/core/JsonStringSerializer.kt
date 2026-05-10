package projson.core

/** Plugin usado com [JsonString]: converte um valor de negócio numa única string JSON. */
interface JsonStringSerializer<T : Any> {

    /** @param value instância; @return texto para JSON string */
    fun serialize(value: T): String
}

