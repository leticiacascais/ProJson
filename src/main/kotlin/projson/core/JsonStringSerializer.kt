package projson.core

// (Fase 2): contrato do plugin para serializar uma classe como string. 
interface JsonStringSerializer<T : Any> {
    fun serialize(value: T): String
}

