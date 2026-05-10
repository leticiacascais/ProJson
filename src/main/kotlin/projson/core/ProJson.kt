package projson.core

import projson.model.JsonArray
import projson.model.JsonObject
import projson.model.JsonPrimitive
import projson.model.JsonReference
import projson.model.JsonValue
import java.util.IdentityHashMap
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * Serializa valores Kotlin para o modelo JSON em memória ([JsonObject], [JsonArray], etc.).
 *
 * Suporta:
 * * **Fase 1:** primitivas, `null`, [Map] (objeto sem `"$type"`), coleções ([Iterable]) como array,
 *   restantes objetos com `"$type"` e propriedades.
 * * **Fase 2:** [Reference], [JsonProperty], [JsonIgnore], [JsonString] com [JsonStringSerializer].
 *
 * Referências: propriedades anotadas com [Reference] serializam grafos com `"$id"` nos alvos
 * e `"$ref"` onde a referência aparece; o cliente não gere UUIDs manualmente.
 */
class ProJson {

    private data class RefState(
        val ids: IdentityHashMap<Any, String> = IdentityHashMap(),
        val shouldHaveId: MutableSet<Any> = java.util.Collections.newSetFromMap(IdentityHashMap())
    )

    /**
     * Converte [value] para [JsonValue].
     *
     * @param value instância Kotlin, [Map], coleção, primitiva ou `null`
     * @return modelo JSON correspondente
     */
    fun toJson(value: Any?): JsonValue {
        val state = RefState()
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

        if (kClass.hasAnnotation<JsonString>()) return

        for (prop in kClass.declaredMemberProperties) {
            if (prop.hasAnnotation<JsonIgnore>()) continue
            val propValue = prop.getter.call(value)

            val isReference = prop.hasAnnotation<Reference>()
            if (isReference) {
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

    /** Fase 1: núcleo em `when`; fase 2 entra via [RefState] dentro de [serializeObject] e referências. */
    private fun toJsonInternal(value: Any?, state: RefState): JsonValue {
        return when (value) {
            null -> JsonPrimitive(null)
            is JsonValue -> value
            is String, is Number, is Boolean -> JsonPrimitive(value)
            is Map<*, *> -> {
                val obj = JsonObject()
                for ((k, v) in value.entries) {
                    val key = k as? String
                        ?: throw IllegalArgumentException("Map keys must be String")
                    obj.setProperty(key, toJsonInternal(v, state))
                }
                obj
            }
            is Iterable<*> -> {
                val arr = JsonArray()
                for (e in value) {
                    arr.add(toJsonInternal(e, state))
                }
                arr
            }
            else -> serializeObject(value, state)
        }
    }

    /**
     * Fase 1: objeto Kotlin → [JsonObject] com `"$type"` e propriedades (ordem do construtor primário).
     * Fase 2: acrescenta `@JsonString`, `@JsonIgnore`, `@JsonProperty`, `@Reference` e `"$id"`.
     */
    private fun serializeObject(instance: Any, state: RefState): JsonValue {
        val kClass = instance::class

        /** Fase 2: serialização como string (plugin) */
        if (kClass.hasAnnotation<JsonString>()) {
            val jsonString = kClass.findAnnotation<JsonString>()!!
            val serializer = instantiateSerializer(jsonString.serializer)
            @Suppress("UNCHECKED_CAST")
            val text = (serializer as JsonStringSerializer<Any>).serialize(instance)
            return JsonPrimitive(text)
        }

        val obj = JsonObject()

        /** Fase 2: alvos de referência recebem "$id" /
        if (state.shouldHaveId.contains(instance)) {
            val id = state.ids.getOrPut(instance) { UUID.randomUUID().toString() }
            obj.setProperty("\$id", JsonPrimitive(id))
        }

        obj.setProperty("\$type", JsonPrimitive(kClass.simpleName ?: kClass.toString()))

        /** Igual à parte 1: mapa nome → propriedade + ordem do construtor; fase 2 ignora @JsonIgnore */
        val propsByName =
            kClass.declaredMemberProperties
                .filterIsInstance<KProperty1<Any, *>>()
                .filterNot { it.hasAnnotation<JsonIgnore>() }
                .associateBy { it.name }

        val propsInOrder =
            kClass.primaryConstructor?.parameters
                ?.mapNotNull { p -> p.name?.let(propsByName::get) }
                ?: propsByName.values.sortedBy { it.name }

        for (prop in propsInOrder) {
            val propAnnotation = prop.findAnnotation<JsonProperty>()
            val propName = propAnnotation?.name ?: prop.name
            val propValue = prop.get(instance)

            /** Fase 2: referências → "$ref" (lista ou valor único) /
            if (prop.hasAnnotation<Reference>()) {
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

        val id = state.ids.getOrPut(value) { UUID.randomUUID().toString() }
        state.shouldHaveId.add(value)
        return JsonReference(id)
    }

    private fun instantiateSerializer(kClass: KClass<out JsonStringSerializer<*>>): JsonStringSerializer<*> {
        kClass.objectInstance?.let { return it }
        return kClass.createInstance()
    }
}

