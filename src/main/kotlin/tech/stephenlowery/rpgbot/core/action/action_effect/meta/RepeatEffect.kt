package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.MetaActionEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class RepeatEffect(val times: Int, effect: ActionEffect) : MetaActionEffect(effect) {
    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        return EffectResult.merge((0 until times).map { effect.applyEffect(from, to, cycle) })
    }
}