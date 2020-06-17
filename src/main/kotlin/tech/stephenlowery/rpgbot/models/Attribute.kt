package tech.stephenlowery.rpgbot.models

class Attribute(val name: String, var base: Int) {

    val additiveModifiers = mutableListOf<AttributeModifier>()
    val multiplyModifiers = mutableListOf(AttributeModifier(1.0, -1))

    fun value(): Int = Math.round(multiplyModifiers.sum() * (base + additiveModifiers.sum())).toInt()
}