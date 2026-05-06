package projson.core

import projson.JsonArray
import projson.JsonObject
import projson.JsonPrimitive
import projson.JsonReference
import projson.JsonValue
import java.util.IdentityHashMap
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.findAnnotation

//classe responsável pela serialização de objetos para JSON
class ProJson {

    //guarda estado da serialização
    private data class RefState(
        //associa ao respetivo id
        val ids: IdentityHashMap<Any, String> = IdentityHashMap(),
        //objetos que precisam de id
        val shouldHaveId: MutableSet<Any> = java.util.Collections.newSetFromMap(IdentityHashMap())
    )

    //objetos: `JsonObject`, coleções: `JsonArray`, com customização via anotações.
    fun toJson(value: Any?): JsonValue {
        val state = RefState()
        //decidir antecipadamente quais instâncias precisam de `$id`.
        preScanReferences(value, state)
        //serialização principal
        return toJsonInternal(value, state)
    }

    //identifica referências antes da serialização
    private fun preScanReferences(root: Any?, state: RefState) {
        val visited = java.util.Collections.newSetFromMap(IdentityHashMap<Any, Boolean>())
        preScanAny(root, state, visited)
    }

    //percorre os valores
    private fun preScanAny(value: Any?, state: RefState, visited: MutableSet<Any>) {
        if (value == null) return
        //já está serializado
        if (value is JsonValue) return
        //não precisam de processamento
        if (value is String || value is Number || value is Boolean) return

        //se for map, serializa como objeto JSON
        if (value is Map<*, *>) {
            for ((k, v) in value.entries) {
                preScanAny(v, state, visited)
            }
            return
        }

        //Collections elemento a elemento
        if (value is Iterable<*>) {
            for (e in value) preScanAny(e, state, visited)
            return
        }

        //evita repetição
        if (!visited.add(value)) return

        val kClass = value::class

        //classes com `@JsonString` são serializadas como String
        //não são expandidas em propriedades
        if (kClass.hasAnnotation<JsonString>()) return

        //reflection: percorre todas as propriedades da classe
        for (prop in kClass.declaredMemberProperties) {
            //ignora o jsonignore
            if (prop.hasAnnotation<JsonIgnore>()) continue
            val propValue = prop.getter.call(value)

            //ref: marca para receber id
            val isReference = prop.hasAnnotation<Reference>()
            if (isReference) {
                //alvo de `$ref` deve possuir `$id`.
                markReferenced(propValue, state)
                preScanAny(propValue, state, visited)
            } else {
                preScanAny(propValue, state, visited)
            }
        }
    }

    //objeto como alvo de ref JSON
    private fun markReferenced(value: Any?, state: RefState) {
        if (value == null) return
        if (value is JsonValue) return
        if (value is String || value is Number || value is Boolean) return
        if (value is Map<*, *>) return

        //collection podem conter múltiplos objetos
        if (value is Iterable<*>) {
            for (e in value) markReferenced(e, state)
            return
        }

        //apenas gera id uma vez por objeto
        state.ids.getOrPut(value) { UUID.randomUUID().toString() }
        state.shouldHaveId.add(value)
    }

    //converte qualquer valor kotlin em JsonValue
    private fun toJsonInternal(value: Any?, state: RefState): JsonValue {
        if (value == null) return JsonPrimitive(null)
        //já pronto
        if (value is JsonValue) return value
        //primitivas JSON
        if (value is String || value is Number || value is Boolean) {
            return JsonPrimitive(value)
        }

        //maps tornam-se JsonObject
        //não recebem type
        if (value is Map<*, *>) {
            val obj = JsonObject()
            for ((k, v) in value.entries) {
                val key = k as? String
                    ?: throw IllegalArgumentException("Map keys must be String")
                obj.setProperty(key, toJsonInternal(v, state))
            }
            return obj
        }

        //collection torna-se JsonArray
        if (value is Iterable<*>) {
            val arr = JsonArray()
            for (e in value) {
                arr.add(toJsonInternal(e, state))
            }
            return arr
        }

        //tratados como object
        return serializeObject(value, state)
    }

    private fun serializeObject(instance: Any, state: RefState): JsonValue {
        val kClass = instance::class

        if (kClass.hasAnnotation<JsonString>()) {
            val jsonString = kClass.findAnnotation<JsonString>()!!
            //converter instância para texto via serializer.
            val serializer = instantiateSerializer(jsonString.serializer)
            @Suppress("UNCHECKED_CAST")
            val text = (serializer as JsonStringSerializer<Any>).serialize(instance)
            return JsonPrimitive(text)
        }

        val obj = JsonObject()

        if (state.shouldHaveId.contains(instance)) {
            //`$id` só em objetos que são alvo de referência
            val id = state.ids.getOrPut(instance) { UUID.randomUUID().toString() }
            obj.setProperty("\$id", JsonPrimitive(id))
        }

        // Enunciado (Fase 1): `$type` em objetos (exceto Map).
        obj.setProperty("\$type", JsonPrimitive(kClass.simpleName ?: kClass.toString()))

        for (prop in kClass.declaredMemberProperties) {
            if (prop.hasAnnotation<JsonIgnore>()) continue

            val propAnnotation = prop.findAnnotation<JsonProperty>()
            val propName = propAnnotation?.name ?: prop.name
            val propValue = prop.getter.call(instance)

            val isReference = prop.hasAnnotation<Reference>()
            if (isReference) {
                // em propriedades @Reference, o valor vira "$ref" em vez de objeto completo.
                obj.setProperty(propName, toJsonWithReferences(propValue, state))
            } else {
                obj.setProperty(propName, toJsonInternal(propValue, state))
            }
        }

        return obj
    }

    private fun toJsonWithReferences(value: Any?, state: RefState): JsonValue {
        if (value == null) return JsonPrimitive(null)

        if (value is Iterable<*>) {
            val arr = JsonArray()
            for (e in value) {
                arr.add(toReferenceOrInline(e, state))
            }
            return arr
        }

        return toReferenceOrInline(value, state)
    }

    private fun toReferenceOrInline(value: Any?, state: RefState): JsonValue {
        if (value == null) return JsonPrimitive(null)
        if (value is String || value is Number || value is Boolean) return JsonPrimitive(value)
        if (value is Map<*, *>) return toJsonInternal(value, state)
        if (value is Iterable<*>) return toJsonWithReferences(value, state)

        //nos campos @Reference, objetos tornam-se {"$ref": "<uuid>"}.
        val id = state.ids.getOrPut(value) { UUID.randomUUID().toString() }
        state.shouldHaveId.add(value)
        return JsonReference(id)
    }

    private fun instantiateSerializer(kClass: KClass<out JsonStringSerializer<*>>): JsonStringSerializer<*> {
        //depois de obter um `KClass`, instanciar com `createInstance()`
        kClass.objectInstance?.let { return it }
        return kClass.createInstance()
    }
}

