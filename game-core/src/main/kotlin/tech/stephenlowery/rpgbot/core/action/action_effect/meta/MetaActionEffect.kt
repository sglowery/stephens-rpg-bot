package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect

abstract class MetaActionEffect(open val effect: ActionEffect, duration: Int = 1) : ActionEffect(duration)