package tech.stephenlowery.rpgbot.models

data class EffectResult(
    var text: String? = null,
    var source: RPGCharacter? = null,
    var target: RPGCharacter? = null,
    var value: Int = 0,
    var miss: Boolean = false,
    var crit: Boolean = false,
    var continued: Boolean = false,
    var expired: Boolean = false
)