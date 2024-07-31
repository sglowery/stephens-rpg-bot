package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.CharacterActionType
import tech.stephenlowery.rpgbot.core.game.StatGetterFn
import tech.stephenlowery.rpgbot.core.game.ValueFromRPGCharacterFn
import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class SwapStatsEffect(
    duration: Int,
    private val statGetterFn: StatGetterFn,
    private val swapValueFn: ValueFromRPGCharacterFn<Int>? = null,
) : ActionEffect(duration) {
    
    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        val fromValue = swapValueFn?.invoke(from) ?: statGetterFn(from).value()
        val toValue = swapValueFn?.invoke(to) ?: statGetterFn(to).value()
        val difference = fromValue - toValue
        val fromStat = statGetterFn(from)
        fromStat.addAdditiveMod(difference.toDouble(), duration)
        statGetterFn(to).addAdditiveMod(-difference.toDouble(), duration)
        return EffectResult.singleResult(
            source = from,
            target = to,
            value = difference,
            actionType = CharacterActionType.OTHER,
            other = fromStat.name
        )
    }

}