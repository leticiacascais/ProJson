package projson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JsonObjectTest {

        @Test
        fun testSetProperty() {
            // Fase 1: manipulação — escrita de propriedades no objeto
            val obj = JsonObject()
            obj.setProperty("name", JsonPrimitive("Paulo"))
            obj.setProperty("age", JsonPrimitive(30))

            assertEquals("{\"name\": \"Paulo\", \"age\": 30}", obj.toString())
        }

        @Test
        fun testGetProperty() {
            // Fase 1: manipulação — leitura de propriedades no objeto
            val obj = JsonObject()
            val value = JsonPrimitive("Json")
            obj.setProperty("message", value)

            assertEquals(value, obj.getProperty("message"))
        }

        @Test
        fun testRemoveProperty() {
            // Fase 1: manipulação — remoção de propriedades no objeto
            val obj = JsonObject()
            obj.setProperty("project", JsonPrimitive("JsonPro"))
            obj.removeProperty("project")

            assertNull(obj.getProperty("project"))
        }

    }
