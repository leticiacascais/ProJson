package projson.core

import kotlin.reflect.KClass

/** Marca uma propriedade cujo valor deve serializar-se como referências (`"$ref"`) em vez de objeto completo. */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Reference

/** Exclui a propriedade da saída JSON. */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonIgnore

/** Define o nome da propriedade no JSON (alias). */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonProperty(val name: String)

/** Serializa instâncias desta classe como string JSON via [serializer]. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonString(val serializer: KClass<out JsonStringSerializer<*>>)