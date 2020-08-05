package tech.stephenlowery.rpgbot.models.character

class AttributeModifier(val value: Double, val duration: Int = -1, val name: String? = null) {

    var turnsActive: Int = 0

    fun cycle() {
        turnsActive += 1
    }

    fun isExpired(): Boolean = turnsActive >= duration && !isPermanent()

    fun isPermanent(): Boolean = duration == -1
}

fun MutableList<AttributeModifier>.sum(): Double = this.getValuesOfActiveModifiers().sum()

fun MutableList<AttributeModifier>.getValuesOfActiveModifiers(): List<Double> = this.filter { !it.isExpired() }.map { it.value }