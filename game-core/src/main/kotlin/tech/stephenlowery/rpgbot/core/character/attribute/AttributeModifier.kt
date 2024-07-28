package tech.stephenlowery.rpgbot.core.character.attribute

class AttributeModifier(val value: Double,
                        val duration: Int = -1,
                        val name: String? = null,
                        val modifierType: AttributeModifierType) {

    var turnsActive: Int = 0

    fun cycle() {
        turnsActive += 1
    }

    fun isExpired(): Boolean = turnsActive >= duration && !isPermanent()

    fun isPermanent(): Boolean = duration == -1

    fun displayValue(): String = when(modifierType) {
        AttributeModifierType.ADDITIVE -> value.toInt().toString()
        AttributeModifierType.MULTIPLICATIVE -> "${value.toInt()}%"
    }
}