package tech.stephenlowery.rpgbot.models.action.action_effect.impl

import tech.stephenlowery.rpgbot.models.action.action_effect.impl.meta.TemporaryStatModEffect
import tech.stephenlowery.rpgbot.models.character.AttributeModifierType
import tech.stephenlowery.rpgbot.models.character.RPGCharacter

class DefendEffect(
    amount: Int = 50
) : TemporaryStatModEffect(amount, duration = 1, attributeModifierType = AttributeModifierType.ADDITIVE, statGetter = RPGCharacter::defense)