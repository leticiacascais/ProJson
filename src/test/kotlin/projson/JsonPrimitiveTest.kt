package projson

import projson.model.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonPrimitiveTest {

    @Test
    fun testStringPrimitive() {
        //string: JSON string com aspas
        val string = JsonPrimitive("hello")
        assertEquals("\"hello\"", string.toString())
    }

    @Test
    fun testNumberPrimitive() {
        //number: JSON number
        val number = JsonPrimitive(13)
        assertEquals("13", number.toString())
    }

    @Test
    fun testBooleanPrimitive() {
        //boolean: JSON boolean
        val bool = JsonPrimitive(true)
        assertEquals("true", bool.toString())
    }

    @Test
    fun testNullPrimitive() {
        //null: JSON null
        val valuenull = JsonPrimitive(null)
        assertEquals("null", valuenull.toString())
    }
}