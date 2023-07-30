package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.MetaActionEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class VampirismEffect(private val proportion: Double = 1.0, effect: ActionEffect) : MetaActionEffect(effect) {
    
    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        val wrappedEffectResults = effect.applyEffect(from, to, cycle)
        val damageDone = wrappedEffectResults.filter { !it.miss && it.target == to }.sumOf { it.value }
        val healing = damageDone.toDouble() * proportion
        from.damage.addAdditiveMod(-healing)
        return EffectResult.singleResult(
            source = from,
            target = to,
            value = damageDone,
            miss = damageDone == 0,
            other = healing.toInt().toString()
        )
    }

}