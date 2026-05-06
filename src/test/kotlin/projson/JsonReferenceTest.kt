package projson

import projson.core.ProJson
import projson.core.Reference
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonReferenceTest {

    data class Date(val day: Int, val month: Int, val year: Int)

    class Task(
        val description: String,
        val deadline: Date?,
        @Reference
        val dependencies: List<Task>
    )

    @Test
    fun testReferenceToString() {
        //serialização do nó `{"$ref": "<uuid>"}`
        val ref = JsonReference("123")

        assertEquals("{\"" + "$" + "ref\": \"123\"}", ref.toString())
    }

    @Test
    fun testReferenceToJson() {
        val t1 = Task("T1", Date(30, 2, 2026), emptyList())
        val t2 = Task("T2", Date(31,4,2026), emptyList())
        val t3 = Task("T3", null, listOf(t1, t2))
        val all = listOf(t1, t2, t3)
        val json = ProJson().toJson(all) as JsonArray


    }
}