package projson

import projson.core.JsonIgnore
import projson.core.JsonProperty
import projson.core.JsonString
import projson.core.JsonStringSerializer
import projson.core.ProJson
import projson.core.Reference
import projson.JsonArray
import projson.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProJsonTest {

    //Fase 1: data class sem @JsonString (serializa como objeto com $type)
    data class PlainDate(val day: Int, val month: Int, val year: Int)

    // Fase 2: a classe Date pode ser serializada como texto via @JsonString
    @JsonString(DateAsText::class)
    data class Date(val day: Int, val month: Int, val year: Int)

    class Task(
        @JsonProperty("desc")
        val description: String,
        @JsonIgnore
        val deadline: PlainDate?,
        @Reference
        @JsonProperty("deps")
        val dependencies: List<Task>
    )

    object DateAsText : JsonStringSerializer<Date> {
        override fun serialize(value: Date): String =
            "%02d/%02d/%04d".format(value.day, value.month, value.year)
    }
}