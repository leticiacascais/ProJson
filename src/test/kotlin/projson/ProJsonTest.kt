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

    // Fase 1: data class sem @JsonString (serializa como objeto com $type)
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

    @Test
    fun testCollectionGeneratesArrayAndAllowsManipulation() {
        // Fase 1: coleção → `JsonArray` + manipulação (`add`)
        val list = listOf("a", null, "b")
        val json = ProJson().toJson(list) as JsonArray
        json.add("c")
        assertEquals("[\"a\", null, \"b\", \"c\"]", json.toString())
    }

    @Test
    fun testObjectGeneratesTypeAndAllowsPropertyChange() {
        // Fase 1: objeto → `JsonObject` com `$type` + manipulação (`setProperty`)
        val d = PlainDate(31, 4, 2026)
        val json = ProJson().toJson(d) as JsonObject
        json.setProperty("year", 2027)
        assertEquals("{\"\$type\": \"PlainDate\", \"day\": 31, \"month\": 4, \"year\": 2027}", json.toString())
    }

    @Test
    fun testMapHasNoTypeProperty() {
        // Fase 1: `Map` → `JsonObject` sem `$type`
        val json = ProJson().toJson(mapOf("a" to 1, "b" to "x")) as JsonObject
        assertEquals("{\"a\": 1, \"b\": \"x\"}", json.toString())
    }

    @Test
    fun testReferencesAndPropertyCustomization() {
        // Fase 2: `@Reference` + `@JsonProperty` + `@JsonIgnore`
        val t1 = Task("T1", PlainDate(30, 2, 2026), emptyList())
        val t2 = Task("T2", PlainDate(31, 4, 2026), emptyList())
        val t3 = Task("T3", null, listOf(t1, t2))

        val json = ProJson().toJson(listOf(t1, t2, t3)).toString()

        // renamed fields + ignored deadline
        assertTrue(json.contains("\"$" + "type\": \"Task\""))
        assertTrue(json.contains("\"desc\": \"T1\""))
        assertTrue(json.contains("\"deps\": []"))
        assertTrue(!json.contains("\"deadline\""))

        // references appear + referenced objects have $id somewhere in output
        assertTrue(json.contains("\"$" + "id\""))
        assertTrue(json.contains("\"$" + "ref\""))
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

