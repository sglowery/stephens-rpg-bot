package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.game.GameConstants.BASE_CRIT_CHANCE
import tech.stephenlowery.rpgbot.core.game.GameConstants.CRIT_EFFECT_PRECISION_SCALAR
import tech.stephenlowery.rpgbot.core.game.GameConstants.HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR
import kotlin.random.Random

class HealEffect(
    private val min: Int,
    private val max: Int,
    private val canFail: Boolean = false,
    private val canCrit: Boolean = true,
) : ActionEffect() {
    
    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        val critChance = BASE_CRIT_CHANCE + from.precision.value()
        val critHealingMultiplier = from.criticalDamage.value() + from.precision.value() * CRIT_EFFECT_PRECISION_SCALAR
        val isCrit = Random.nextInt(100) < critChance && canCrit
        val baseHealing = Random.nextInt(min, max + 1) + (from.defense.value() + from.power.value()) / 2 * HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR
        val totalHealing = (baseHealing * (from.healingGiven.value() * to.healingTaken.value() / 1e4) * (if (isCrit) critHealingMultiplier else 1.0))
        val succeeds = !canFail || succeedsByChance(from.precision.value())
        if (succeeds) {
            to.damage.addAdditiveMod(-totalHealing)
        }
        return EffectResult.singleResult(
            source = from,
            target = to,
            miss = !succeeds,
            value = totalHealing.toInt(),
            crit = isCrit && succeeds
        )
    }
    
    private fun succeedsByChance(fromPrecision: Int) = (Math.random() * 100) > (50 - fromPrecision * 3)
}