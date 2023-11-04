package tech.stephenlowery.rpgbot.core.character.attribute

import spock.lang.Specification
import tech.stephenlowery.rpgbot.utils.TestUtils

class AttributeTest extends Specification {
    def "fresh attribute has calculated value equal to its base"() {
        given:
        def attribute = TestUtils.getTestAttribute("", base)

        expect:
        attribute.base.toInteger() == attribute.value()

        where:
        base << [1.0, 5.0, 10.5]
    }

    def "attribute modifier modifies attribute value"() {
        given:
        def attribute = TestUtils.getTestAttribute("", base)
        attribute.addAdditiveMod(addMod, -1, "")
        attribute.addMultiplicativeMod(multMod, -1, "")

        expect:
        expectedValue == attribute.value()

        where:
        base | addMod | multMod || expectedValue
        1.0  | 0.0    | 0.0     || 1
        1.0  | 5.0    | 0.0     || 6
        1.0  | 9.0    | 1.0     || 20
    }

    def "bringPermanentModifiersWithinBounds will cap 'overflow' modifiers and allow new modifiers to have an effect"() {
        given:
        def min = 10
        def max = 100
        def base = 50.0
        def attribute = TestUtils.getTestAttribute("", base, min, max)

        when:
        attribute.addAdditiveMod(10000.0, -1, null)

        then:
        attribute.value() == max

        when:
        attribute.consolidateModifiers()

        then:
        attribute.value() == max

        when:
        attribute.addAdditiveMod(-10.0, -1, "")

        then:
        attribute.value() == max - 10

        when:
        (1..10).forEach {
            attribute.addAdditiveMod(-50.0, -1, "")
        }

        then:
        attribute.value() == min

        when:
        attribute.consolidateModifiers()

        then:
        attribute.value() == min

        when:
        attribute.addAdditiveMod(10.0, -1, "")

        then:
        attribute.value() == min + 10
    }
}
