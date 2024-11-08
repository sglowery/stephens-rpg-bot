package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.CharacterActionType
import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.StatModEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType
import tech.stephenlowery.rpgbot.core.game.GameConstants.CRIT_CHANCE_PRECISION_SCALAR
import tech.stephenlowery.rpgbot.core.game.GameConstants.CRIT_DAMAGE_PRECISION_SCALAR
import tech.stephenlowery.rpgbot.core.game.GameConstants.HEALING_BASE_HIT_CHANCE
import tech.stephenlowery.rpgbot.core.game.GameConstants.HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR
import tech.stephenlowery.rpgbot.core.game.GameConstants.HIT_CHANCE_PRECISION_SCALAR
import kotlin.random.Random

class HealEffect(
    private val min: Int? = null,
    private val max: Int? = null,
    modDuration: Int = -1,
    duration: Int = 1,
    private val canMiss: Boolean = false,
    private val canCrit: Boolean = true,
    private val alwaysCrits: Boolean = false,
) : StatModEffect(
    modDuration = modDuration,
    duration = duration,
    statGetter = RPGCharacter::damage,
    attributeModifierType = AttributeModifierType.ADDITIVE,
    beforeAfterValueComparator = RPGCharacter::getActualHealth
) {

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        val isCrit = isCrit(from, to)
        val totalHealing = calculateHealing(from, to, isCrit).toInt()
        val succeeds = isSuccessful(from)
        var results: List<EffectResult>? = null
        if (succeeds) {
            results = super.applyEffect(from, to, cycle, -totalHealing)
        }
        return EffectResult.singleResult(
            source = from,
            target = to,
            value = results?.first()?.value ?: totalHealing ,
            actionType = CharacterActionType.HEALING,
            miss = !succeeds,
            crit = isCrit,
            expired = results?.first()?.expired ?: false,
            continued = results?.first()?.continued ?: false,
        )
    }

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int, value: Int): List<EffectResult> {
        val healing = value * healingScalar(from, to)
        val results = super.applyEffect(from, to, cycle, -healing.toInt())
        return EffectResult.singleResult(
            source = from,
            target = to,
            value = results.first().value,
            actionType = CharacterActionType.HEALING,
            miss = false,
            crit = results.first().crit,
            expired = results.first().expired,
            continued = results.first().continued,
        )
    }

    private fun calculateHealing(from: RPGCharacter, to: RPGCharacter, isCrit: Boolean): Double {
        return baseHealing(from) * healingScalar(from, to) * critHealingMultiplierIfCrit(from, isCrit)
    }

    private fun critHealingMultiplierIfCrit(from: RPGCharacter, isCrit: Boolean) = when {
        isCrit -> critHealingMultiplier(from) / 100
        else   -> 1.0
    }

    private fun baseHealing(from: RPGCharacter): Double {
        return (Random.nextInt(min!!, max!! + 1) + (from.defense.value() + from.power.value()) / 2 * HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR).coerceAtLeast(0.0)
    }

    private fun healingScalar(from: RPGCharacter, to: RPGCharacter): Double {
        return (from.healingGivenScalar.value() * to.healingTakenScalar.value() / 1e4).coerceAtLeast(0.0)
    }

    private fun isCrit(from: RPGCharacter, to: RPGCharacter): Boolean {
        return alwaysCrits || (canCrit && Random.nextInt(100) < critChance(from))
    }

    private fun critChance(from: RPGCharacter): Double {
        return sourceCritChance(from).coerceAtLeast(0.0)
    }

    private fun sourceCritChance(from: RPGCharacter): Double {
        return from.criticalChance.value() + from.precision.value() * CRIT_CHANCE_PRECISION_SCALAR
    }

    private fun critHealingMultiplier(from: RPGCharacter): Double {
        return (from.criticalEffectScalar.value() + from.precision.value() * CRIT_DAMAGE_PRECISION_SCALAR).coerceAtLeast(0.0)
    }

    private fun isSuccessful(from: RPGCharacter): Boolean = !canMiss || succeedsByChance(from)

    private fun succeedsByChance(from: RPGCharacter) = Random.nextInt(100) < sourceHitChance(from)

    private fun sourceHitChance(from: RPGCharacter): Double {
        return HEALING_BASE_HIT_CHANCE + from.precision.value() * HIT_CHANCE_PRECISION_SCALAR
    }
}