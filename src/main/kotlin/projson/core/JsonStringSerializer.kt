package projson.core

//serializers custom usados pela annotation @JsonString
//como o objeto deve ser convertido para JSON
interface JsonStringSerializer<T : Any> {

    //converte um objeto para a sua representação textual JSON
    fun serialize(value: T): String
}

