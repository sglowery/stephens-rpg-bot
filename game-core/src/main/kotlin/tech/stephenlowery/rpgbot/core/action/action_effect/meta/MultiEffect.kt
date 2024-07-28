package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class MultiEffect(private vararg val effects: ActionEffect, duration: Int = 1) : ActionEffect(duration) {

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        return effects.flatMap { it.applyEffect(from, to, cycle) }
    }
}