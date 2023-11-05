package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.action_effect.meta.StatModEffect
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class DefendEffect(
    value: Int = 50,
    modDuration: Int = 1
) : StatModEffect(
    value = value,
    duration = 1,
    modDuration = modDuration,
    attributeModifierType = AttributeModifierType.ADDITIVE,
    statGetter = RPGCharacter::defense
)