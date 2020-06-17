package tech.stephenlowery.rpgbot.models

class AttributeModifier(val value: Double, val duration: Int = 1) {

    var turns: Int = 0

    fun cycle() {
        turns += 1
    }

    fun isExpired(): Boolean = turns >= duration && !isPermanent()

    fun isPermanent(): Boolean = duration == -1

    operator fun plus(other: AttributeModifier): Double {
        return value + other.value
    }

}

fun MutableList<AttributeModifier>.sum(): Double = this.filter { !it.isExpired() }.map { it.value }.sum()