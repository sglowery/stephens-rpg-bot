package tech.stephenlowery.rpgbot.assets

import tech.stephenlowery.rpgbot.models.GameConstants.Companion.BASE_CRIT_CHANCE
import tech.stephenlowery.rpgbot.models.GameConstants.Companion.BASE_CRIT_DAMAGE_MULTIPLIER
import tech.stephenlowery.rpgbot.models.GameConstants.Companion.BASE_HIT_CHANCE
import tech.stephenlowery.rpgbot.models.GameConstants.Companion.CRIT_DAMAGE_PRECISION_SCALAR
import tech.stephenlowery.rpgbot.models.GameConstants.Companion.DEFENSE_DAMAGE_REDUCTION_FACTOR
import tech.stephenlowery.rpgbot.models.GameConstants.Companion.DEFENSE_HIT_CHANCE_REDUCTION_FACTOR
import tech.stephenlowery.rpgbot.models.GameConstants.Companion.HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR
import tech.stephenlowery.rpgbot.models.GameConstants.Companion.HIT_CHANCE_PRECISION_SCALING
import tech.stephenlowery.rpgbot.models.GameConstants.Companion.POWER_SCALING
import tech.stephenlowery.rpgbot.models.action.ActionEffect
import tech.stephenlowery.rpgbot.models.action.EffectResult
import tech.stephenlowery.rpgbot.models.action.MetaActionEffect
import tech.stephenlowery.rpgbot.models.character.AttributeModifierType
import tech.stephenlowery.rpgbot.models.character.RPGCharacter
import kotlin.random.Random

class DamageHealthEffect(private val min: Int, private val max: Int, duration: Int = 1) : ActionEffect(duration) {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        val critChance = (from.criticalChance.value() + from.precision.value() - to.defense.value().toDouble() * DEFENSE_DAMAGE_REDUCTION_FACTOR).coerceAtLeast(0.0)
        val critDamageMultiplier = ((from.criticalDamage.value() / 100.0) + CRIT_DAMAGE_PRECISION_SCALAR * (from.precision.value() - to.precision.value())).coerceAtLeast(1.0)
        val baseDamage = (Random.nextInt(min, max + 1) + from.power.value() * POWER_SCALING).coerceAtLeast(0.0)
        val doesHit = Random.nextInt(100) < ((BASE_HIT_CHANCE + from.precision.value() * HIT_CHANCE_PRECISION_SCALING) - to.defense.value() * DEFENSE_HIT_CHANCE_REDUCTION_FACTOR)
        val isCrit = doesHit && Random.nextInt(100) < critChance
        val totalDamage = (baseDamage * (from.damageGiven.value() * to.damageTaken.value() / 10000.0) * (if (isCrit) critDamageMultiplier else 1.0) - to.defense.value()).coerceAtLeast(0.0)
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

class DefendEffect(
    amount: Int = 50
) : TemporaryStatModEffect(amount, duration = 1, attributeModifierType = AttributeModifierType.ADDITIVE, statGetter = RPGCharacter::defense)

class HealEffect(private val min: Int, private val max: Int) : ActionEffect() {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        val critChance = BASE_CRIT_CHANCE + from.precision.value()
        val critHealingMultiplier = BASE_CRIT_DAMAGE_MULTIPLIER + from.precision.value() * CRIT_DAMAGE_PRECISION_SCALAR
        val isCrit = Random.nextInt(100) < critChance
        val baseHealing = Random.nextInt(min, max + 1) + (from.defense.value() + from.power.value()) / 2 * HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR
        val totalHealing = (baseHealing * (if (isCrit) critHealingMultiplier else 1.0)).toInt().also { healing -> to.damage.addAdditiveMod(-healing.toDouble()) }
        return EffectResult(
            source = from,
            target = to,
            value = totalHealing,
            crit = isCrit
        )
    }
}

class ExhaustEffect(val amount: Int, duration: Int) : ActionEffect(duration) {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        to.cooldowns.keys.forEach { ability ->
            to.cooldowns[ability] = to.cooldowns[ability]!! + 1
        }
        return EffectResult(
            source = from,
            target = to,
            continued = cycle > 0,
            expired = cycle >= duration
        )
    }
}

class SwapStatsEffect(duration: Int, val statGetter: (RPGCharacter) -> Attribute) : ActionEffect(duration) {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        val fromStat = statGetter(from)
        val toStat = statGetter(to)
        val difference = (toStat.value() - fromStat.value())
        fromStat.addAdditiveMod(difference.toDouble(), duration)
        toStat.addAdditiveMod(-difference.toDouble(), duration)
        return EffectResult(
            source = from,
            target = to,
            value = difference,
            other = fromStat.name
        )
    }
}

// meta-effects

class VampirismEffect(private val proportion: Double = 1.0, effect: ActionEffect) : MetaActionEffect(effect) {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        val wrappedEffectResult = effect.resolve(from, to, cycle)
        val damageDone = wrappedEffectResult.takeIf { !it.miss }?.value ?: 0
        val healing = damageDone.toDouble() * proportion
        from.damage.addAdditiveMod(-healing)
        return EffectResult(
            source = from,
            target = to,
            value = damageDone,
            miss = wrappedEffectResult.miss,
            crit = wrappedEffectResult.crit,
            other = healing.toInt().toString()
        )
    }
}

open class TemporaryStatModEffect(
    val min: Int,
    val max: Int? = null,
    duration: Int,
    val statGetter: StatGetterFn,
    val attributeModifierType: AttributeModifierType
) : ActionEffect(duration) {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        val value = max?.let { Random.nextInt(min, max + 1) } ?: min
        when (attributeModifierType) {
            AttributeModifierType.ADDITIVE -> statGetter(to).addAdditiveMod(value.toDouble(), duration)
            AttributeModifierType.MULTIPLICATIVE -> statGetter(to).addMultiplicativeMod(value.toDouble(), duration)
        }
        return EffectResult(source = from, target = to, value = value)
    }
}

class RepeatEffect(val times: Int, effect: ActionEffect) : MetaActionEffect(effect) {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        return (0..times).map { effect.resolve(from, to, cycle) }.let { effectResultList ->
            EffectResult(
                source = from,
                target = to,
                value = effectResultList.sumBy { it.value },
                miss = effectResultList.any { it.miss },
                crit = effectResultList.any { it.crit }
            )
        }
    }
}