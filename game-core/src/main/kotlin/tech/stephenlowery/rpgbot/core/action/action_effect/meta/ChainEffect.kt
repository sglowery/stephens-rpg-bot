package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class ChainEffect(
    vararg effects: ActionEffect,
) : MultiEffect(
    effects = effects,
    duration = effects.sumOf { it.duration }
) {

    private val effectByCycle: List<Int> = mutableListOf<Int>().apply {
        effects.forEachIndexed { index, effect -> repeat(effect.duration) { this += index } }
    }

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        val currentEffect = effects[effectByCycle[cycle]]
        val expiredEffectDurations = effects.filterIndexed { index, it ->
            val durationUpToEffect = effects.filterIndexed { otherIndex, _ -> otherIndex < index }.sumOf { it.duration }
            it.isExpired(cycle - durationUpToEffect)
        }.sumOf { it.duration }
        return currentEffect.applyEffect(from, to, cycle - expiredEffectDurations).apply {
            first().continued = effects.first().isExpired(cycle)
            first().expired = isExpired(cycle + 1)
        }
    }

}