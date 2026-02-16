package tech.stephenlowery.rpgbot.core.character.attribute

class AttributeModifier(
    val value: Double,
    val duration: Int = -1,
    val name: String? = null,
    val modifierType: AttributeModifierType,
) {

    var turnsActive: Int = 0

    fun cycle() {
        turnsActive += 1
    }

    fun isExpired(): Boolean = turnsActive >= duration && !isPermanent()

    fun isPermanent(): Boolean = duration == -1

    fun displayValue(displayValueFn: (Int) -> String): String = sign() + when (modifierType) {
        AttributeModifierType.ADDITIVE       -> displayValueFn(value.toInt())
        AttributeModifierType.MULTIPLICATIVE -> "${value.toInt()}%"
    }

    private fun sign(): String = when (value > 0) {
        true  -> "+"
        false -> ""
    }

}