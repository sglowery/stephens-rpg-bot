package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.game.StatGetterFn
import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import kotlin.random.Random

open class StatModEffect(
    val min: Double,
    val max: Double? = null,
    duration: Int,
    val statGetter: StatGetterFn,
    val attributeModifierType: AttributeModifierType
) : ActionEffect(duration) {

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        val value = Random.nextDouble(min, max?.plus(1) ?: min)
        when (attributeModifierType) {
            AttributeModifierType.ADDITIVE       -> statGetter(to).addAdditiveMod(value, duration)
            AttributeModifierType.MULTIPLICATIVE -> statGetter(to).addMultiplicativeMod(value, duration)
        }
        return EffectResult.singleResult(source = from, target = to, value = value.toInt())
    }

}