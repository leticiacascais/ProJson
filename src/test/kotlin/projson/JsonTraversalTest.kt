package projson

import projson.core.ProJson
import projson.model.JsonArray
import projson.model.JsonObject
import projson.model.JsonPrimitive
import projson.model.JsonVisitor
import projson.model.forEachDepthFirst
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonTraversalTest {

    @Test
    fun forEachDepthFirstVisitsAllNodesInOrder() {
        val root = JsonObject()
        root.setProperty("a", JsonPrimitive(1))
        val inner = JsonArray()
        inner.add(JsonPrimitive("x"))
        root.setProperty("b", inner)

        val order = mutableListOf<String>()
        root.forEachDepthFirst { node ->
            when (node) {
                is JsonObject -> order += "obj"
                is JsonArray -> order += "arr"
                is JsonPrimitive -> order += "prim:${node.value}"
                else -> order += "other"
            }
        }

        assertEquals(
            listOf("obj", "prim:1", "arr", "prim:x"),
            order
        )
    }

    @Test
    fun visitorDefaultMethodsAllowPartialVisitor() {
        val json = ProJson().toJson(listOf(1, 2)) as JsonArray
        var sum = 0
        json.accept(
            object : JsonVisitor {
                override fun visitPrimitive(value: JsonPrimitive) {
                    (value.value as? Number)?.toInt()?.let { sum += it }
                }
            }
        )
        assertEquals(3, sum)
    }
}
