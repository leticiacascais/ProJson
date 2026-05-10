package projson

import projson.model.JsonObject
import projson.model.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JsonObjectTest {

        @Test
        fun testSetProperty() {
            //manipulação — escrita de propriedades no objeto
            val obj = JsonObject()
            obj.setProperty("name", JsonPrimitive("Paulo"))
            obj.setProperty("age", JsonPrimitive(30))

            assertEquals("{\"name\": \"Paulo\", \"age\": 30}", obj.toString())
        }

        @Test
        fun testGetProperty() {
            //manipulação — leitura de propriedades no objeto
            val obj = JsonObject()
            val value = JsonPrimitive("Json")
            obj.setProperty("message", value)

            assertEquals(value, obj.getProperty("message"))
        }

        @Test
        fun testRemoveProperty() {
            //manipulação — remoção de propriedades no objeto
            val obj = JsonObject()
            obj.setProperty("project", JsonPrimitive("JsonPro"))
            obj.removeProperty("project")

            assertNull(obj.getProperty("project"))
        }

    }
