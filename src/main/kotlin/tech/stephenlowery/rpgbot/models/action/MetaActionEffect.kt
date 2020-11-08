package tech.stephenlowery.rpgbot.models.action

import tech.stephenlowery.rpgbot.models.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.models.character.RPGCharacter

open class MetaActionEffect(open val effect: ActionEffect, duration: Int = 1) : ActionEffect(duration) {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        return EffectResult.merge(effect.resolve(from, to, cycle), super.resolve(from, to, cycle))
    }
}