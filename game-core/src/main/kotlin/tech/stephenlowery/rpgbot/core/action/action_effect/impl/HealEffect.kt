package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.StatModEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType
import tech.stephenlowery.rpgbot.core.game.GameConstants.CRIT_DAMAGE_PRECISION_SCALAR
import tech.stephenlowery.rpgbot.core.game.GameConstants.HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR
import kotlin.random.Random

class HealEffect(
    private val min: Int,
    private val max: Int,
    modDuration: Int = -1,
    duration: Int = 1,
    private val canFail: Boolean = false,
    private val canCrit: Boolean = true,
) : StatModEffect(
    modDuration = modDuration,
    duration = duration,
    statGetter = RPGCharacter::damage,
    attributeModifierType = AttributeModifierType.ADDITIVE
) {

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        val (totalHealing, isCrit) = calculateHealing(from, to)
        val succeeds = isSuccessful(from)
        if (succeeds) {
            super.applyEffect(from, to, cycle, -totalHealing.toInt())
        }
        return EffectResult.singleResult(
            source = from,
            target = to,
            miss = !succeeds,
            value = totalHealing.toInt(),
            crit = isCrit && succeeds
        )
    }

    private fun calculateHealing(from: RPGCharacter, to: RPGCharacter): Pair<Double, Boolean> {
        val critChance = from.criticalChance.value() + from.precision.value()
        val isCrit = Random.nextInt(100) < critChance && canCrit
        val totalHealing = baseHealing(from) * healingScalar(from, to) * critHealingMultiplierIfCrit(from, isCrit)
        return Pair(totalHealing, isCrit)
    }

    private fun critHealingMultiplierIfCrit(from: RPGCharacter, isCrit: Boolean) = if (isCrit) critHealingMultiplier(from) else 1.0

    private fun baseHealing(from: RPGCharacter): Double {
        return (Random.nextInt(min, max + 1) + (from.defense.value() + from.power.value()) / 2 * HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR).coerceAtLeast(0.0)
    }

    private fun healingScalar(from: RPGCharacter, to: RPGCharacter): Double {
        return from.healingGivenScalar.value() * to.healingTakenScalar.value() / 1e4
    }

    private fun critHealingMultiplier(from: RPGCharacter): Double {
        return from.criticalDamage.value() + from.precision.value() * CRIT_DAMAGE_PRECISION_SCALAR
    }

    private fun isSuccessful(from: RPGCharacter): Boolean = !canFail || succeedsByChance(from.precision.value())

    private fun succeedsByChance(fromPrecision: Int) = (Math.random() * 100) > (50 - fromPrecision * 3)
}