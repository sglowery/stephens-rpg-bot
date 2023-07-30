package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.action_effect.meta.StatModEffect
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class DefendEffect(
    amount: Int = 50
) : StatModEffect(
    min = amount.toDouble(),
    duration = 1,
    attributeModifierType = AttributeModifierType.ADDITIVE,
    statGetter = RPGCharacter::defense
)