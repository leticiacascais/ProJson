package projson.core

import kotlin.reflect.KClass

// “By annotating class properties, we can instruct the JSON generation to use references
// instead of the regular objects.”

// Quando uma propriedade tem `@Reference`, o `ProJson` gera `{"$ref": "<uuid>"}` em vez de
// serializar o objeto completo nesse ponto (e garante que o objeto alvo tem `"$id"`).
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Reference

// “Ignoring class fields when generating JSON.”
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonIgnore


// “Customizing JSON object property identifiers.”
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonProperty(val name: String)

// “Develop a plugin mechanism that allows objects of a class to be serialized as strings in JSON.”

// Indica que instâncias desta classe devem ser serializadas como `String` no JSON,
// usando o serializer fornecido (ex.: `Date` → `"30/02/2026"`).
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonString(val serializer: KClass<out JsonStringSerializer<*>>)