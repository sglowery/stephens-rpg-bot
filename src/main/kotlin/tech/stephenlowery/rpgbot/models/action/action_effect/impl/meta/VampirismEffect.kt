package tech.stephenlowery.rpgbot.models.action.action_effect.impl.meta

import tech.stephenlowery.rpgbot.models.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.models.action.EffectResult
import tech.stephenlowery.rpgbot.models.action.MetaActionEffect
import tech.stephenlowery.rpgbot.models.character.RPGCharacter

class VampirismEffect(private val proportion: Double = 1.0, effect: ActionEffect) : MetaActionEffect(effect) {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        val wrappedEffectResult = effect.resolve(from, to, cycle)
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