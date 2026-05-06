package projson.core

import kotlin.reflect.KClass

//marcar propriedades serializadas por referência (`$ref`)
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Reference

//ignorar propriedades na criação do JSON
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonIgnore


//renomear o identificador da propriedade no JSON
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonProperty(val name: String)

//plugin: serializar instâncias da classe como `String` no JSON
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
//serialização da instância para texto
annotation class JsonString(val serializer: KClass<out JsonStringSerializer<*>>)