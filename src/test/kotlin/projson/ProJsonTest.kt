package projson

import projson.core.JsonIgnore
import projson.core.JsonProperty
import projson.core.JsonString
import projson.core.JsonStringSerializer
import projson.core.ProJson
import projson.core.Reference
import projson.model.JsonArray
import projson.model.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProJsonTest {

    //Fase 1: data class sem @JsonString (serializa como objeto com $type)
    data class PlainDate(val day: Int, val month: Int, val year: Int)

    // Fase 2: a classe Date pode ser serializada como texto via @JsonString
    @JsonString(DateAsText::class)
    data class Date(val day: Int, val month: Int, val year: Int)

    class Task1(
        val description: String,
        val deadline: PlainDate?,
        @Reference
        val dependencies: List<Task1>
    )

    class Task2(
        @JsonProperty("desc")
        val description: String,
        @JsonIgnore
        val deadline: PlainDate?,
        @Reference
        @JsonProperty("deps")
        val dependencies: List<Task2>
    )

    object DateAsText : JsonStringSerializer<Date> {
        override fun serialize(value: Date): String =
            "%02d/%02d/%04d".format(value.day, value.month, value.year)
    }

    @Test
    fun testObjectArray() {
        /** Fase 1: objeto → `JsonObject` com `$type` + manipulação (`setProperty`) */
        val d = PlainDate(31, 4, 2026)
        val json = ProJson().toJson(d) as JsonObject
        json.setProperty("year", 2027)
        assertEquals("{\"\$type\": \"PlainDate\", \"day\": 31, \"month\": 4, \"year\": 2027}", json.toString())
    }

    @Test
    fun testJsonArray() {
        /** Fase 1: coleção → `JsonArray` + manipulação (`add`) */
        val list = listOf("a", null, "b")
        val json = ProJson().toJson(list) as JsonArray
        json.add("c")
        assertEquals("[\"a\", null, \"b\", \"c\"]", json.toString())
    }

    @Test
    fun testReferences() {
        // Fase 2: `@Reference` + `@JsonProperty` + `@JsonIgnore` (`$id` são UUID — evita assert no texto inteiro)
        val t1 = Task1("T1", PlainDate(30, 2, 2026), emptyList())
        val t2 = Task1("T2", PlainDate(31, 4, 2026), emptyList())
        val t3 = Task1("T3", null, listOf(t1, t2))

        val all = listOf(t1, t2, t3)
        val json = ProJson().toJson(all).toString()

        assertTrue(json.contains("\"${'$'}type\": \"Task1\""))
        assertTrue(json.contains("\"description\": \"T1\""))
        assertTrue(json.contains("\"description\": \"T2\""))
        assertTrue(json.contains("\"description\": \"T3\""))
        assertTrue(json.contains("\"dependencies\": []"))
        assertTrue(json.contains("\"deadline\""))

        assertTrue(json.contains("\"${'$'}id\""))
        assertTrue(json.contains("\"${'$'}ref\""))

        val refMarker = "{\"${'$'}ref\": \""
        assertEquals(2, json.split(refMarker).size - 1)
    }

    @Test
    fun testObjectProperties() {
        // Fase 2: `@JsonProperty` / `@JsonIgnore` — o `toString()` do modelo usa espaço após `:`
        val t = Task2("T1", PlainDate(30, 2, 2026), emptyList())
        val json = ProJson().toJson(t) as JsonObject

        assertEquals(
            "{\"" + "${'$'}type\": \"Task2\", \"desc\": \"T1\", \"deps\": []}",
            json.toString()
        )
    }

    @Test
    fun testJsonStringPluginSerializesAsText() {
        // Fase 2 (Plugin): `@JsonString` → array de strings formatadas
        val d1 = Date(30, 2, 2026)
        val d2 = Date(31, 4, 2026)
        val json = ProJson().toJson(listOf(d1, d2))

        assertEquals("[\"30/02/2026\", \"31/04/2026\"]", json.toString())
    }
}

