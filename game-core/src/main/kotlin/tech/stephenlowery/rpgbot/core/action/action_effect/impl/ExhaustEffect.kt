package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class ExhaustEffect(private val amount: Int, duration: Int) : ActionEffect(duration) {

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        to.cooldowns.forEach { (ability, value) ->
            to.cooldowns[ability] = value + amount
        }
        return EffectResult.singleResult(
            value = amount,
            source = from,
            target = to,
            continued = cycle > 0,
            expired = cycle >= duration
        )
    }

}