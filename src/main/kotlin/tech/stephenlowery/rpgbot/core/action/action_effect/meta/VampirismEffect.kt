package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.MetaActionEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class VampirismEffect(private val proportion: Double = 1.0, effect: ActionEffect) : MetaActionEffect(effect) {
    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        val wrappedEffectResult = effect.applyEffect(from, to, cycle)
        val damageDone = wrappedEffectResult.takeUnless { it.miss }?.value ?: 0
        val healing = damageDone.toDouble() * proportion
        from.damage.addAdditiveMod(-healing)
        return EffectResult(
            source = from,
            target = to,
            value = damageDone,
            miss = wrappedEffectResult.miss,
            crit = wrappedEffectResult.crit,
            other = healing.toInt().toString()
        )
    }
}