package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.game.GameConstants
import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import kotlin.random.Random

class HealEffect(
    private val min: Int,
    private val max: Int,
    private val canFail: Boolean = false,
    private val canCrit: Boolean = true
) : ActionEffect() {
    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        val critChance = GameConstants.BASE_CRIT_CHANCE + from.precision.value()
        val critHealingMultiplier = GameConstants.BASE_CRIT_DAMAGE_MULTIPLIER + from.precision.value() * GameConstants.CRIT_DAMAGE_PRECISION_SCALAR
        val isCrit = Random.nextInt(100) < critChance && canCrit
        val baseHealing = Random.nextInt(min, max + 1) + (from.defense.value() + from.power.value()) / 2 * GameConstants.HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR
        val totalHealing = (baseHealing * (from.healingGiven.value() * to.healingTaken.value() / 1e4) * (if (isCrit) critHealingMultiplier else 1.0)).also { healing -> to.damage.addAdditiveMod(-healing) }.toInt()
        val doesSucceed = when (canFail) {
            true  -> (Math.random() * 100) > (50 + from.precision.value() * 3)
            false -> true
        }
        return EffectResult(
            source = from,
            target = to,
            miss = !doesSucceed,
            value = if (doesSucceed) totalHealing else 0,
            crit = isCrit && doesSucceed
        )
    }
}