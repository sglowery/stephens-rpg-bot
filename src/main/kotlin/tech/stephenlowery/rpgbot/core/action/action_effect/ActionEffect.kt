package tech.stephenlowery.rpgbot.core.action.action_effect

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

abstract class ActionEffect(val duration: Int = 1) {

    abstract fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult>

    fun isExpired(cycle: Int): Boolean = !isPermanent() && cycle >= duration

    fun isPermanent(): Boolean = duration == -1
}
