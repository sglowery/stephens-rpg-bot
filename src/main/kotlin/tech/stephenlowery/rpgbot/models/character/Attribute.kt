package tech.stephenlowery.rpgbot.models.character

class Attribute(val name: String, var base: Double, val displayValueFn: (Int) -> String = Int::toString) {

    val additiveModifiers = mutableListOf<AttributeModifier>()
    val multiplyModifiers = mutableListOf(AttributeModifier(1.0, -1))

    fun cycleModifiers() {
        listOf(additiveModifiers, multiplyModifiers).forEach { modifierSet ->
            modifierSet.forEach { it.cycle() }
        }
    }

    fun clearExpiredModifiers() {
        additiveModifiers.removeIf { it.isExpired() }
        multiplyModifiers.removeIf { it.isExpired() }
    }

    fun addAdditiveMod(value: Double, duration: Int = -1, name: String? = null) {
        additiveModifiers.add(AttributeModifier(value, duration, name))
    }

    fun addMultiplicativeMod(value: Double, duration: Int = -1, name: String? = null) {
        multiplyModifiers.add(AttributeModifier(value, duration, name))
    }

    fun value(): Int = (multiplyModifiers.sum() * (base + additiveModifiers.sum())).toInt()

    fun displayValue(): String = displayValueFn(value())
}