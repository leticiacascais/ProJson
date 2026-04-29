package projson.core

// “Develop a plugin mechanism that allows objects of a class to be serialized as strings in JSON.”
// Implementações desta interface definem como transformar um objeto num `String` JSON.
interface JsonStringSerializer<T : Any> {
    fun serialize(value: T): String
}

