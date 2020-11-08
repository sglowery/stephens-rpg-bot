package tech.stephenlowery.rpgbot.models.action.action_effect.impl

import tech.stephenlowery.rpgbot.StatGetterFn
import tech.stephenlowery.rpgbot.ValueFromCharacterFn
import tech.stephenlowery.rpgbot.models.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.models.action.EffectResult
import tech.stephenlowery.rpgbot.models.character.RPGCharacter

class SwapStatsEffect(duration: Int, val statGetterFn: StatGetterFn, val swapValueFn: ValueFromCharacterFn<Int>? = null) : ActionEffect(duration) {
    override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
        val fromValue = swapValueFn?.let { it(from) } ?: statGetterFn(from).value()
        val toValue = swapValueFn?.let { it(to) } ?: statGetterFn(to).value()
        val difference = fromValue - toValue
        val fromStat = statGetterFn(from)
        fromStat.addAdditiveMod(difference.toDouble(), duration)
        statGetterFn(to).addAdditiveMod(-difference.toDouble(), duration)
        return EffectResult(
            source = from,
            target = to,
            value = difference,
            other = fromStat.name
        )
    }
}