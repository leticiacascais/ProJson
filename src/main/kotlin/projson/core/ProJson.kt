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

class ProJson {

    private data class RefState(
        val ids: IdentityHashMap<Any, String> = IdentityHashMap(),
        val shouldHaveId: MutableSet<Any> = java.util.Collections.newSetFromMap(IdentityHashMap())
    )

    // objetos → `JsonObject`, coleções → `JsonArray`, com customização via anotações. 
    fun toJson(value: Any?): JsonValue {
        val state = RefState()
        // decidir antecipadamente quais instâncias precisam de `$id`.
        preScanReferences(value, state)
        return toJsonInternal(value, state)
    }

    private fun preScanReferences(root: Any?, state: RefState) {
        val visited = java.util.Collections.newSetFromMap(IdentityHashMap<Any, Boolean>())
        preScanAny(root, state, visited)
    }

    private fun preScanAny(value: Any?, state: RefState, visited: MutableSet<Any>) {
        if (value == null) return
        if (value is JsonValue) return
        if (value is String || value is Number || value is Boolean) return

        if (value is Map<*, *>) {
            for ((k, v) in value.entries) {
                preScanAny(v, state, visited)
            }
            return
        }

        if (value is Iterable<*>) {
            for (e in value) preScanAny(e, state, visited)
            return
        }

        if (!visited.add(value)) return

        val kClass = value::class

        // classes com `@JsonString` não são expandidas em propriedades.
        if (kClass.hasAnnotation<JsonString>()) return

        for (prop in kClass.declaredMemberProperties) {
            if (prop.hasAnnotation<JsonIgnore>()) continue
            val propValue = prop.getter.call(value)

            val isReference = prop.hasAnnotation<Reference>()
            if (isReference) {
                // alvo de `$ref` deve possuir `$id`.
                markReferenced(propValue, state)
                preScanAny(propValue, state, visited)
            } else {
                preScanAny(propValue, state, visited)
            }
        }
    }

    private fun markReferenced(value: Any?, state: RefState) {
        if (value == null) return
        if (value is JsonValue) return
        if (value is String || value is Number || value is Boolean) return
        if (value is Map<*, *>) return

        if (value is Iterable<*>) {
            for (e in value) markReferenced(e, state)
            return
        }

        state.ids.getOrPut(value) { UUID.randomUUID().toString() }
        state.shouldHaveId.add(value)
    }

    private fun toJsonInternal(value: Any?, state: RefState): JsonValue {
        if (value == null) return JsonPrimitive(null)
        if (value is JsonValue) return value

        if (value is String || value is Number || value is Boolean) {
            return JsonPrimitive(value)
        }

        if (value is Map<*, *>) {
            val obj = JsonObject()
            for ((k, v) in value.entries) {
                val key = k as? String
                    ?: throw IllegalArgumentException("Map keys must be String")
                obj.setProperty(key, toJsonInternal(v, state))
            }
            return obj
        }

        if (value is Iterable<*>) {
            val arr = JsonArray()
            for (e in value) {
                arr.add(toJsonInternal(e, state))
            }
            return arr
        }

        return serializeObject(value, state)
    }

    private fun serializeObject(instance: Any, state: RefState): JsonValue {
        val kClass = instance::class

        if (kClass.hasAnnotation<JsonString>()) {
            val jsonString = kClass.findAnnotation<JsonString>()!!
            // converter instância para texto via serializer.
            val serializer = instantiateSerializer(jsonString.serializer)
            @Suppress("UNCHECKED_CAST")
            val text = (serializer as JsonStringSerializer<Any>).serialize(instance)
            return JsonPrimitive(text)
        }

        val obj = JsonObject()

        if (state.shouldHaveId.contains(instance)) {
            // Enunciado (References): `$id` só em objetos que são alvo de referência.
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

        // em campos @Reference, objetos viram {"$ref": "<uuid>"}.
        val id = state.ids.getOrPut(value) { UUID.randomUUID().toString() }
        state.shouldHaveId.add(value)
        return JsonReference(id)
    }

    private fun instantiateSerializer(kClass: KClass<out JsonStringSerializer<*>>): JsonStringSerializer<*> {
        // depois de obter um `KClass`, instanciar com `createInstance()`.
        kClass.objectInstance?.let { return it }
        return kClass.createInstance()
    }
}

