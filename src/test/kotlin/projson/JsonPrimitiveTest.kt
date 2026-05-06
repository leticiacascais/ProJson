package projson

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonPrimitiveTest {

    @Test
    fun testStringPrimitive() {
        // Fase 1: string → JSON string com aspas
        val string = JsonPrimitive("hello")
        assertEquals("\"hello\"", string.toString())
    }

    @Test
    fun testNumberPrimitive() {
        // Fase 1: number → JSON number
        val number = JsonPrimitive(13)
        assertEquals("13", number.toString())
    }

    @Test
    fun testBooleanPrimitive() {
        // Fase 1: boolean → JSON boolean
        val bool = JsonPrimitive(true)
        assertEquals("true", bool.toString())
    }

    @Test
    fun testNullPrimitive() {
        // Fase 1: null → JSON null
        val valuenull = JsonPrimitive(null)
        assertEquals("null", valuenull.toString())
    }
}