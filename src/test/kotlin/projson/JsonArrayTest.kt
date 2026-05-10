package projson

import projson.core.ProJson
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonArrayTest {

    @Test
    fun testAddElement() {
        val array = JsonArray()
        array.add(JsonPrimitive("Json"))
        array.add(JsonPrimitive(10))

        assertEquals("[\"Json\", 10]", array.toString())
    }

    @Test
    fun testRemoveElement() {
        val array = JsonArray()
        val value = JsonPrimitive("Json")

        array.add(value)
        array.remove(value)

        assertEquals("[]", array.toString())
    }

    @Test
    fun testGetElement() {
        val array = JsonArray()
        val value = JsonPrimitive(false)
        array.add(value)

        assertEquals(value, array.get(0))
    }

    @Test
    fun testEmpty() {
        val array = JsonArray()

        assertEquals("[]", array.toString())
    }

    @Test
    fun testJson() {
        val list = listOf("a", null, "b")
        val json = ProJson().toJson(list) as JsonArray
        json.add("c")

        assertEquals("[\"a\", null, \"b\", \"c\"]", json.toString())
    }
}