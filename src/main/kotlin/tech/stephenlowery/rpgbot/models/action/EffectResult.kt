package tech.stephenlowery.rpgbot.models.action

import tech.stephenlowery.rpgbot.models.character.RPGCharacter

data class EffectResult(
    var source: RPGCharacter? = null,
    var target: RPGCharacter? = null,
    var value: Int = 0,
    var miss: Boolean = false,
    var crit: Boolean = false,
    var continued: Boolean = false,
    var expired: Boolean = false,
    var other: String? = null,
    var chained: Boolean = false
)