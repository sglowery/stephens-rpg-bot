package tech.stephenlowery.rpgbot.models.character

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttributeTest {

    @Test
    fun `fresh attribute has calculated value equal to its base`() {
        val attribute = Attribute("", 1.0)
        assertEquals(expected = attribute.base.toInt(), actual = attribute.value())
    }

    @Test
    fun `attribute modifier modifies attribute value`() {

        data class AttributeTestCase(
            val base: Double,
            val addMod: Double,
            val expected: Double
        )

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

    @Test
    fun `bringPermanentModifiersWithinBounds will cap "overflow" modifiers and allow new modifiers to have an effect`() {
        val min = 0
        val max = 100
        val base = 50.0
        val attribute = Attribute("", base, min, max)

        attribute.addAdditiveMod(10000.0)
        assertEquals(actual = attribute.value(), expected = max)

        attribute.consolidateModifiers()
        assertEquals(actual = attribute.value(), expected = max)

        attribute.addAdditiveMod(-10.0)
        assertEquals(actual = attribute.value(), expected = max - 10)

        repeat(9) {
            attribute.addAdditiveMod(-50.0)
        }
        assertEquals(attribute.value(), 0)

        attribute.consolidateModifiers()
        assertEquals(attribute.value(), 0)

        attribute.addAdditiveMod(10.0)
        assertEquals(attribute.value(), 10)
    }
}