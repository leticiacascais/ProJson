package projson.core

/** Plugin usado com [JsonString]: converte um valor de negócio numa única string JSON. */
interface JsonStringSerializer<T : Any> {

    fun serialize(value: T): String
}

