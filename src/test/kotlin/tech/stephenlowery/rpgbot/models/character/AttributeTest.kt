package tech.stephenlowery.rpgbot.models.character

import org.junit.Test
import kotlin.test.assertEquals

class AttributeTest {

    data class AttributeTestCase(
        val base: Double,
        val addMod: Double,
        val expected: Double
    )

    @Test
    fun `fresh attribute has calculated value equal to its base`() {
        val attribute = Attribute("", 1.0)
        assertEquals(expected = attribute.base.toInt(), actual = attribute.value())
    }

    @Test
    fun `attribute modifier modifies attribute value`() {

        val numberTestCases = listOf(
            AttributeTestCase(1.0, 1.0, 2.0),
            AttributeTestCase(1.0, 0.0, 1.0),
            AttributeTestCase(1.0, 3.0, 4.0),
            AttributeTestCase(1.0, -1.0, 0.0)
        )

        numberTestCases.forEach {
            val attribute = Attribute("", it.base)
            val attributeModifier = AttributeModifier(it.addMod, -1)
            attribute.additiveModifiers.add(attributeModifier)
            assertEquals(expected = it.expected.toInt(), actual = attribute.value())
        }
    }
}