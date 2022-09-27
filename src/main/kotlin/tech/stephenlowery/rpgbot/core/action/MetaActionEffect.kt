package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

open class MetaActionEffect(open val effect: ActionEffect, duration: Int = 1) : ActionEffect(duration) {
    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        return EffectResult.merge(effect.applyEffect(from, to, cycle), super.applyEffect(from, to, cycle))
    }
}