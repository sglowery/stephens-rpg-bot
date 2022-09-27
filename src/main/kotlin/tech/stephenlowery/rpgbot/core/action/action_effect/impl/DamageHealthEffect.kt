package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.game.GameConstants
import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import kotlin.random.Random

class DamageHealthEffect(private val min: Int, private val max: Int, duration: Int = 1) : ActionEffect(duration) {
    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        val critChance = (from.criticalChance.value() + from.precision.value() - to.defense.value().toDouble() * GameConstants.DEFENSE_CRIT_CHANCE_REDUCTION_FACTOR).coerceAtLeast(0.0)
        val critDamageMultiplier = ((from.criticalDamage.value() / 100.0) + GameConstants.CRIT_DAMAGE_PRECISION_SCALAR * (from.precision.value() - to.precision.value())).coerceAtLeast(1.0)
        val baseDamage = (Random.nextInt(min, max + 1) + from.power.value() * GameConstants.POWER_SCALING).coerceAtLeast(0.0)
        val doesHit = Random.nextInt(100) < ((GameConstants.BASE_HIT_CHANCE + from.precision.value() * GameConstants.HIT_CHANCE_PRECISION_SCALING) - to.defense.value() * GameConstants.DEFENSE_HIT_CHANCE_REDUCTION_FACTOR)
        val isCrit = doesHit && Random.nextInt(100) < critChance
        val totalDamage = (baseDamage * (from.damageGiven.value() * to.damageTaken.value() / 1e4) * (if (isCrit) critDamageMultiplier else 1.0) - to.defense.value()).coerceAtLeast(0.0)
        if (doesHit) to.damage.addAdditiveMod(totalDamage, -1)
        return EffectResult(
            source = from,
            target = to,
            value = totalDamage.toInt(),
            miss = !doesHit,
            crit = isCrit
        )
    }
}