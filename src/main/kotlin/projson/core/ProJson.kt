package projson.core

import projson.JsonArray
import projson.JsonObject
import projson.JsonPrimitive
import projson.JsonValue
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

//classe responsável pela serialização de objetos para JSON
class ProJson {

    fun toJson(value: Any?): JsonValue {
        return toJsonInternal(value)
    }

    //converte qualquer valor kotlin em JsonValue
    private fun toJsonInternal(value: Any?): JsonValue {
        return when (value) {
            null -> JsonPrimitive(null)
            is JsonValue -> value
            is String, is Number, is Boolean -> JsonPrimitive(value)
            is Iterable<*> -> {
                val arr = JsonArray()
                for (e in value) arr.add(toJsonInternal(e))
                arr
            }
            else -> serializeObject(value)
        }
    }

    private fun serializeObject(instance: Any): JsonValue {
        val kClass = instance::class

        val obj = JsonObject()
        obj.setProperty("\$type", JsonPrimitive(kClass.simpleName ?: kClass.toString()))

        // Garante ordem determinística (o teste compara `toString()` literal).
        // Se houver construtor primário (ex: data class), usamos a ordem dos parâmetros.
        // Caso contrário, ordenamos alfabeticamente pelo nome da propriedade.
        val propsByName = kClass.declaredMemberProperties
            .filterIsInstance<KProperty1<Any, *>>()
            .associateBy { it.name }

        val propsInOrder = kClass.primaryConstructor?.parameters
            ?.mapNotNull { p -> p.name?.let(propsByName::get) }
            ?: propsByName.values.sortedBy { it.name }

        for (prop in propsInOrder) {
            val propValue = prop.get(instance)
            obj.setProperty(prop.name, toJsonInternal(propValue))
        }

        return obj
    }
}

