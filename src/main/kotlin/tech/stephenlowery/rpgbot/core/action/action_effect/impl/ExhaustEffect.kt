package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class ExhaustEffect(val amount: Int, duration: Int) : ActionEffect(duration) {
    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        to.cooldowns.keys.forEach { ability ->
            to.cooldowns[ability] = to.cooldowns[ability]!! + amount
        }
        return EffectResult(
            source = from,
            target = to,
            continued = cycle > 0,
            expired = cycle >= duration
        )
    }
}