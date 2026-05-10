package projson

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonPrimitiveTest {

    @Test
    fun testString() {
        val string = JsonPrimitive("hello")
        assertEquals("\"hello\"", string.toString())
    }

    @Test
    fun testNumber() {
        val number = JsonPrimitive(13)
        assertEquals("13", number.toString())
    }

    @Test
    fun testBoolean() {
        val bool = JsonPrimitive(true)
        assertEquals("true", bool.toString())
    }

    @Test
    fun testNull() {
        val valuenull = JsonPrimitive(null)
        assertEquals("null", valuenull.toString())
    }
}