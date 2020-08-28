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
    fun `attributes with non-null min or max will stay within those ranges`() {
        val base = 50.0
        val min = 25
        val max = 75
        val attribute = Attribute("", base, min, max)

        assertEquals(attribute.value(), base.toInt())

        attribute.addAdditiveMod(10000.0)

        assertEquals(attribute.value(), max)
        assertTrue(attribute.value() > base)

        attribute.addMultiplicativeMod(-2.0)

        assertEquals(attribute.value(), min)
        assertTrue(attribute.value() < base)
    }

    @Test
    fun `consolidateModifiers reduces all permanent, non-named status effects to a single effect object and doesn't change overall value`() {
        val attribute = Attribute("", 10.0)
        repeat(10) {
            attribute.addAdditiveMod(10.0)
        }
        attribute.addAdditiveMod(15.0, name = "fart")

        val firstValue = attribute.value()
        assertTrue(attribute.additiveModifiers.size == 11)

        attribute.consolidateModifiers()

        val secondValue = attribute.value()
        assertTrue(attribute.additiveModifiers.size == 2)

        assertEquals(firstValue, secondValue)
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