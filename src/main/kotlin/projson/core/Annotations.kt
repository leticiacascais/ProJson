package projson.core

import kotlin.reflect.KClass

// (Fase 2): marcar propriedades serializadas por referência (`$ref`). 
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Reference

// (Fase 2): ignorar propriedades na geração do JSON.
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonIgnore


// (Fase 2): renomear o identificador da propriedade no JSON. 
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonProperty(val name: String)

// (Fase 2): plugin — serializar instâncias da classe como `String` no JSON. 
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonString(val serializer: KClass<out JsonStringSerializer<*>>)