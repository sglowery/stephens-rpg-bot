package tech.stephenlowery.rpgbot.models.action.action_effect

import tech.stephenlowery.rpgbot.models.action.EffectResult
import tech.stephenlowery.rpgbot.models.character.RPGCharacter

open class ActionEffect(open val duration: Int = 1) {

    open fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        return EffectResult.EMPTY
    }

    fun isExpired(cycle: Int): Boolean = cycle >= duration && !isPermanent()

    fun isPermanent(): Boolean = duration == -1
}
