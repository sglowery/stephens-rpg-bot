package tech.stephenlowery.rpgbot.core.character.attribute

class AttributeModifier(val value: Double, val duration: Int = -1, val name: String? = null) {

    private var turnsActive: Int = 0

    fun cycle() {
        turnsActive += 1
    }

    fun isExpired(): Boolean = turnsActive >= duration && !isPermanent()

    fun isPermanent(): Boolean = duration == -1
}