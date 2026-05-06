package projson

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonArrayTest {

    @Test
    fun testAddElement() {
        // Fase 1: manipulação — adicionar elementos ao array
        val array = JsonArray()
        array.add(JsonPrimitive("Json"))
        array.add(JsonPrimitive(10))

        assertEquals("[\"Json\", 10]", array.toString())
    }

    @Test
    fun testRemoveElement() {
        // Fase 1: manipulação — remover elementos do array
        val array = JsonArray()
        val value = JsonPrimitive("Json")

        array.add(value)
        array.remove(value)

        assertEquals("[]", array.toString())
    }

    @Test
    fun testGetElement() {
        // Fase 1: leitura — obter elemento por índice
        val array = JsonArray()
        val value = JsonPrimitive(false)
        array.add(value)

        assertEquals(value, array.get(0))
    }

    @Test
    fun testEmptyArray() {
        // Fase 1: serialização — array vazio
        val array = JsonArray()

        assertEquals("[]", array.toString())
    }
}