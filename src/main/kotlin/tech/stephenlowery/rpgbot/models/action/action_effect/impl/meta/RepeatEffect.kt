package tech.stephenlowery.rpgbot.models.action.action_effect.impl.meta

import tech.stephenlowery.rpgbot.models.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.models.action.EffectResult
import tech.stephenlowery.rpgbot.models.action.MetaActionEffect
import tech.stephenlowery.rpgbot.models.character.RPGCharacter

class RepeatEffect(val times: Int, effect: ActionEffect) : MetaActionEffect(effect) {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        return EffectResult.merge((0..times).map { effect.resolve(from, to, cycle) })
    }
}