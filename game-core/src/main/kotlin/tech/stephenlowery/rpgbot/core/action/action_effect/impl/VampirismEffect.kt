package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.ComposeEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.StatModEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType

class VampirismEffect(
    private val proportion: Double = 1.0,
    min: Int,
    max: Int,
    canMiss: Boolean = true,
    canCrit: Boolean = true,
) : ComposeEffect<StatModEffect>(
    outer = HealEffect(min = min, max = max, duration = 1),
    inner = DamageHealthEffect(min, max, canMiss = canMiss, canCrit = canCrit),
    compose = { from, to, cycle, outer, effects ->
        val damageDone = effects.filter { !it.miss && it.target == to }.sumOf { it.value }
        val healing = damageDone.toDouble() * proportion
        val healingResults = outer.applyEffect(from, from, cycle, healing.toInt())
        EffectResult.singleResult(
            source = from,
            target = to,
            value = damageDone,
            miss = damageDone == 0,
            other = healingResults.first().value.toString()
        )
    }
)