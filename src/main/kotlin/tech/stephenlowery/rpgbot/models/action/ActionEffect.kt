package tech.stephenlowery.rpgbot.models.action

import tech.stephenlowery.rpgbot.models.character.RPGCharacter

open class ActionEffect(open val duration: Int = 1) {

    open fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        return EffectResult.EMPTY
    }

    fun isExpired(cycle: Int): Boolean = cycle >= duration && !isPermanent()

    fun isPermanent(): Boolean = duration == -1
}

fun Double.clampToPositive(): Double = this.coerceAtLeast(0.0)
fun Int.clampToPositive(): Int = this.coerceAtLeast(0)