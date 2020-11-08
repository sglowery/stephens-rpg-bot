package tech.stephenlowery.rpgbot.models.action.action_effect.impl

import tech.stephenlowery.rpgbot.models.GameConstants
import tech.stephenlowery.rpgbot.models.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.models.action.EffectResult
import tech.stephenlowery.rpgbot.models.character.RPGCharacter
import kotlin.random.Random

class HealEffect(private val min: Int, private val max: Int) : ActionEffect() {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        val critChance = GameConstants.BASE_CRIT_CHANCE + from.precision.value()
        val critHealingMultiplier = GameConstants.BASE_CRIT_DAMAGE_MULTIPLIER + from.precision.value() * GameConstants.CRIT_DAMAGE_PRECISION_SCALAR
        val isCrit = Random.nextInt(100) < critChance
        val baseHealing = Random.nextInt(min, max + 1) + (from.defense.value() + from.power.value()) / 2 * GameConstants.HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR
        val totalHealing = (baseHealing * (if (isCrit) critHealingMultiplier else 1.0)).toInt().also { healing -> to.damage.addAdditiveMod(-healing.toDouble()) }
        return EffectResult(
            source = from,
            target = to,
            value = totalHealing,
            crit = isCrit
        )
    }
}