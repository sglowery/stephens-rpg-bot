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
    outer = StatModEffect(duration = 1, statGetter = RPGCharacter::health, attributeModifierType = AttributeModifierType.ADDITIVE),
    inner = DamageHealthEffect(min, max, canMiss = canMiss, canCrit = canCrit),
    compose = { from, to, cycle, outer, effects ->
        val damageDone = effects.filter { !it.miss && it.target == to }.sumOf { it.value }
        val healing = damageDone.toDouble() * proportion
        from.damage.addAdditiveMod(-healing)
        EffectResult.singleResult(
            source = from,
            target = to,
            value = damageDone,
            miss = damageDone == 0,
            other = healing.toInt().toString()
        )
    }
)