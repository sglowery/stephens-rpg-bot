package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType
import tech.stephenlowery.rpgbot.core.game.StatGetterFn
import kotlin.math.abs

open class StatModEffect(
    protected var value: Int? = null,
    protected val modDuration: Int = -1,
    duration: Int = 1,
    protected val statGetter: StatGetterFn,
    protected val attributeModifierType: AttributeModifierType,
) : ActionEffect(duration) {

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        return addModifier(from, to)
    }

    open fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int, value: Int): List<EffectResult> {
        this.value = value
        return this.addModifier(from, to)
    }

    private fun addModifier(from: RPGCharacter, to: RPGCharacter): List<EffectResult> {
        when (attributeModifierType) {
            AttributeModifierType.ADDITIVE       -> statGetter(to).addAdditiveMod(value!!.toDouble(), modDuration)
            AttributeModifierType.MULTIPLICATIVE -> statGetter(to).addMultiplicativeMod(value!!.toDouble(), modDuration)
        }
        return EffectResult.singleResult(source = from, target = to, value = abs(value!!))
    }

}