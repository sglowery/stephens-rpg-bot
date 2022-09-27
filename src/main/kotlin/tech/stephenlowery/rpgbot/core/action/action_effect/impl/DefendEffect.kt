package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.action_effect.meta.TemporaryStatModEffect
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class DefendEffect(
    amount: Int = 50
) : TemporaryStatModEffect(amount.toDouble(), duration = 1, attributeModifierType = AttributeModifierType.ADDITIVE, statGetter = RPGCharacter::defense)