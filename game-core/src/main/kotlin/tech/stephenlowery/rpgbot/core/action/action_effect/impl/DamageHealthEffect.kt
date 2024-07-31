package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.StatModEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType
import tech.stephenlowery.rpgbot.core.game.GameConstants.BASE_HIT_CHANCE
import tech.stephenlowery.rpgbot.core.game.GameConstants.CRIT_CHANCE_PRECISION_SCALAR
import tech.stephenlowery.rpgbot.core.game.GameConstants.CRIT_DAMAGE_POWER_SCALAR
import tech.stephenlowery.rpgbot.core.game.GameConstants.CRIT_DAMAGE_PRECISION_SCALAR
import tech.stephenlowery.rpgbot.core.game.GameConstants.DEFENSE_CRIT_CHANCE_REDUCTION_FACTOR
import tech.stephenlowery.rpgbot.core.game.GameConstants.DEFENSE_DAMAGE_REDUCTION_FACTOR
import tech.stephenlowery.rpgbot.core.game.GameConstants.DEFENSE_HIT_CHANCE_REDUCTION_FACTOR
import tech.stephenlowery.rpgbot.core.game.GameConstants.HIT_CHANCE_PRECISION_SCALING
import tech.stephenlowery.rpgbot.core.game.GameConstants.POWER_DAMAGE_SCALAR
import kotlin.random.Random

class DamageHealthEffect(
    private val min: Int,
    private val max: Int,
    duration: Int = 1,
    modDuration: Int = -1,
    private val canMiss: Boolean = true,
    private val canCrit: Boolean = true,
    private val alwaysCrits: Boolean = false,
) : StatModEffect(
    modDuration = modDuration,
    duration = duration,
    statGetter = RPGCharacter::damage,
    attributeModifierType = AttributeModifierType.ADDITIVE
) {

    init {
        if (!canCrit && alwaysCrits) {
            throw IllegalArgumentException("DamageHealthEffect initialized as always critting but being unable to.")
        }
    }

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        val isCrit = isCrit(from, to)
        val doesHit = isSuccessful(from, to)
        var totalDamage = 0
        var result: List<EffectResult>? = null
        if (doesHit) {
            totalDamage = calculateDamage(from, to, isCrit).toInt()
            result = super.applyEffect(from, to, cycle, totalDamage)
        }
        return EffectResult.singleResult(
            source = from,
            target = to,
            value = totalDamage,
            miss = !doesHit,
            crit = isCrit,
            expired = result?.first()?.expired ?: false,
            continued = result?.first()?.continued ?: false,
        )
    }

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int, value: Int): List<EffectResult> {
        val damage = value * damageScalar(from, to)
        return super.applyEffect(from, to, cycle, damage.toInt())
    }

    private fun isCrit(from: RPGCharacter, to: RPGCharacter): Boolean {
        return alwaysCrits || (canCrit && Random.nextInt(100) < critChance(from, to))
    }

    private fun calculateDamage(from: RPGCharacter, to: RPGCharacter, isCrit: Boolean): Double {
        val totalDamage = totalBaseDamage(from, to) * damageScalar(from, to) * critDamageMultiplierIfCrit(from, isCrit)
        return totalDamage.coerceAtLeast(0.0)
    }

    private fun totalBaseDamage(from: RPGCharacter, to: RPGCharacter): Double {
        return (baseDamage(from) - targetDamageReduction(to)).coerceAtLeast(0.0)
    }

    private fun baseDamage(from: RPGCharacter): Double {
        return Random.nextInt(min, max + 1) + from.power.value() * POWER_DAMAGE_SCALAR
    }

    private fun targetDamageReduction(to: RPGCharacter): Double {
        return to.defense.value().toDouble() * DEFENSE_DAMAGE_REDUCTION_FACTOR
    }

    private fun damageScalar(from: RPGCharacter, to: RPGCharacter): Double {
        return from.damageGivenScalar.value() * to.damageTakenScalar.value() / 1e4
    }

    private fun critDamageMultiplierIfCrit(from: RPGCharacter, isCrit: Boolean): Double {
        return if (isCrit) critDamageMultiplier(from) / 100.0 else 1.0
    }

    private fun critDamageMultiplier(from: RPGCharacter): Double {
        return from.criticalEffectScalar.value() +
                (from.precision.value() * CRIT_DAMAGE_PRECISION_SCALAR) +
                (from.power.value() * CRIT_DAMAGE_POWER_SCALAR)
    }

    private fun critChance(from: RPGCharacter, to: RPGCharacter): Double {
        return (sourceCritChance(from) - targetCritChanceReduction(to)).coerceAtLeast(0.0)
    }

    private fun sourceCritChance(from: RPGCharacter): Double {
        return from.criticalChance.value() + from.precision.value() * CRIT_CHANCE_PRECISION_SCALAR
    }

    private fun targetCritChanceReduction(to: RPGCharacter): Double {
        return to.defense.value() * DEFENSE_CRIT_CHANCE_REDUCTION_FACTOR
    }

    private fun isSuccessful(from: RPGCharacter, to: RPGCharacter): Boolean {
        return !canMiss || succeedsByChance(from, to)
    }

    private fun succeedsByChance(from: RPGCharacter, to: RPGCharacter): Boolean {
        return Random.nextInt(100) < (sourceHitChance(from) - targetHitChanceReduction(to))
    }

    private fun sourceHitChance(from: RPGCharacter): Double {
        return BASE_HIT_CHANCE + from.precision.value() * HIT_CHANCE_PRECISION_SCALING
    }

    private fun targetHitChanceReduction(to: RPGCharacter): Double {
        return to.defense.value() * DEFENSE_HIT_CHANCE_REDUCTION_FACTOR
    }

}