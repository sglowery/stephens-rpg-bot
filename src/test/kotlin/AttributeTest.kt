import org.junit.Test
import tech.stephenlowery.rpgbot.models.Attribute
import tech.stephenlowery.rpgbot.models.AttributeModifier
import kotlin.test.assertEquals

class AttributeTest {

    data class AttributeTestCase(
        val base: Int,
        val addMod: Double,
        val expected: Int
    )

    @Test
    fun `fresh attribute has calculated value equal to its base`() {
        val attribute = Attribute("", 1)
        assertEquals(expected = attribute.base, actual = attribute.value())
    }

    @Test
    fun `attribute modifier modifies attribute value`() {

        val numberTestCases = listOf(
            AttributeTestCase(1, 1.0, 2),
            AttributeTestCase(1, 0.0, 1),
            AttributeTestCase(1, 3.0, 4),
            AttributeTestCase(1, -1.0, 0)
        )

        numberTestCases.forEach {
            val attribute = Attribute("", it.base)
            val attributeModifier = AttributeModifier(it.addMod, -1)
            attribute.additiveModifiers.add(attributeModifier)

            assertEquals(expected = it.expected, actual = attribute.value())
        }
    }
}