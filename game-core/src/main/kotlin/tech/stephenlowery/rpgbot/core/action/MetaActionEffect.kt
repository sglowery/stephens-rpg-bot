package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

abstract class MetaActionEffect(open val effect: ActionEffect, duration: Int = 1) : ActionEffect(duration)