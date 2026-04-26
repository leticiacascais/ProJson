package projson

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonPrimitiveTest {

    @Test
    fun testStringPrimitive() {
        val string = JsonPrimitive("hello")
        assertEquals("\"hello\"", string.toString())
    }

    @Test
    fun testNumberPrimitive() {
        val number = JsonPrimitive(13)
        assertEquals("13", number.toString())
    }

    @Test
    fun testBooleanPrimitive() {
        val bool = JsonPrimitive(true)
        assertEquals("true", bool.toString())
    }

    @Test
    fun testNullPrimitive() {
        val valuenull = JsonPrimitive(null)
        assertEquals("null", valuenull.toString())
    }
}