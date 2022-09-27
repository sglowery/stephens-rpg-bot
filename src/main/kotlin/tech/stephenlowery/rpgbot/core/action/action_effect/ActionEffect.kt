package tech.stephenlowery.rpgbot.core.action.action_effect

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

open class ActionEffect(val duration: Int = 1) {

    open fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        return EffectResult.EMPTY
    }

    fun isExpired(cycle: Int): Boolean = cycle >= duration && !isPermanent()

    fun isPermanent(): Boolean = duration == -1
}
