package tech.stephenlowery.rpgbot.models.action.action_effect.impl

import tech.stephenlowery.rpgbot.models.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.models.action.EffectResult
import tech.stephenlowery.rpgbot.models.character.RPGCharacter

class ExhaustEffect(val amount: Int, duration: Int) : ActionEffect(duration) {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
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