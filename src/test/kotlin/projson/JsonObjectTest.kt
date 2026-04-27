package projson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JsonObjectTest {

        @Test
        fun testSetProperty() {
            val obj = JsonObject()
            obj.setProperty("name", JsonPrimitive("Paulo"))
            obj.setProperty("age", JsonPrimitive(30))

            assertEquals("{\"name\": \"Paulo\", \"age\": 30}", obj.toString())
        }

        @Test
        fun testGetProperty() {
            val obj = JsonObject()
            val value = JsonPrimitive("Json")
            obj.setProperty("message", value)

            assertEquals(value, obj.getProperty("message"))
        }

        @Test
        fun testRemoveProperty() {
            val obj = JsonObject()
            obj.setProperty("project", JsonPrimitive("JsonPro"))
            obj.removeProperty("project")

            assertNull(obj.getProperty("project"))
        }

    }
