package tech.stephenlowery.rpgbot.models

import tech.stephenlowery.rpgbot.models.BaseGameConstants.Companion.BASE_CRIT_CHANCE
import tech.stephenlowery.rpgbot.models.BaseGameConstants.Companion.BASE_HIT_CHANCE
import tech.stephenlowery.rpgbot.models.BaseGameConstants.Companion.HIT_CHANCE_PRECISION_SCALING
import kotlin.random.Random

open class ActionEffect(open val min: Int = 0, open val max: Int = 0, open val duration: Int = 1) {

    open fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        return emptyList()
    }

    fun isExpired(cycle: Int): Boolean = cycle >= duration && duration != -1
}

class DamageHealthEffect(override val min: Int, override val max: Int, override val duration: Int = 1) : ActionEffect() {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        val results = mutableListOf<EffectResult>()
        val critChance = Math.max(BASE_CRIT_CHANCE + from.precision.value() - to.precision.value(), 0.0)
        val critDamageMultiplier = Math.max(1.5 + from.precision.value() / 25 - to.precision.value() / 25, 1.0)
        val baseDamage = Math.max(0, (Random.nextInt(min, max + 1) + from.power.value()))
        val doesHit =
            Random.nextInt(100) < (BASE_HIT_CHANCE + from.precision.value() * HIT_CHANCE_PRECISION_SCALING) - to.defense.value()
        val isCrit = doesHit && Random.nextInt(100) < critChance
        val totalDamage =
            (if (doesHit) (baseDamage * (if (isCrit) critDamageMultiplier else 1.0) - to.defense.value()) else 0.0).clampToPositive().also { damage ->
                to.damage.additiveModifiers.add(AttributeModifier(damage, -1))
            }
        results.add(
            EffectResult(
                source = from,
                target = to,
                value = totalDamage.toInt(),
                miss = !doesHit,
                crit = isCrit
            )
        )
        return results
    }
}

class DefendSelfEffect(override val min: Int = 10, override val duration: Int = 1) : ActionEffect() {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        val defense = min.toDouble()
        from.defense.additiveModifiers.add(AttributeModifier(defense, duration))
        return listOf(EffectResult(value = defense.toInt(), source = from))
    }
}

class HealSelfEffect(override val min: Int, override val max: Int, override val duration: Int = 1) : ActionEffect() {

    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        val results = mutableListOf<EffectResult>()
        val critChance = BASE_CRIT_CHANCE + from.precision.value()
        val critHealingMultiplier = 1.5 + from.precision.value() / 25
        val isCrit = Random.nextInt(100) < critChance
        val baseHealing = Random.nextInt(min, max + 1) + from.power.value() / 3
        val totalHealing = (baseHealing * (if (isCrit) critHealingMultiplier else 1.0)).also { healing ->
            from.damage.additiveModifiers.add(
                AttributeModifier(-healing, -1)
            )
        }
        results.add(EffectResult(source = from, value = totalHealing.toInt(), crit = isCrit))
        return results
    }
}

fun Double.clampToPositive(): Double = if (this < 0) 0.0 else this