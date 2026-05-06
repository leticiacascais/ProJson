package projson.core

interface JsonStringSerializer<T : Any> {
    fun serialize(value: T): String
}

