package projson

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonReferenceTest {

    @Test
    fun testReferenceToString() {
        //serialização do nó `{"$ref": "<uuid>"}`
        val ref = JsonReference("123")

        assertEquals("{\"" + "$" + "ref\": \"123\"}", ref.toString())
    }
}