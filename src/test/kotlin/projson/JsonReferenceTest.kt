package projson

import projson.model.JsonReference
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonReferenceTest {

    @Test
    fun testReferenceToString() {
        val ref = JsonReference("123")

        assertEquals("{\"" + "$" + "ref\": \"123\"}", ref.toString())
    }
}