package tech.stephenlowery.rpgbot.models.action.action_effect.impl.meta

import tech.stephenlowery.rpgbot.StatGetterFn
import tech.stephenlowery.rpgbot.models.action.EffectResult
import tech.stephenlowery.rpgbot.models.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.models.character.AttributeModifierType
import tech.stephenlowery.rpgbot.models.character.RPGCharacter
import kotlin.random.Random

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