package tech.stephenlowery.rpgbot.models.action

import tech.stephenlowery.rpgbot.models.character.RPGCharacter

class CharacterAction(
    val effect: ActionEffect,
    val displayName: String,
    val callbackText: String,
    val description: String,
    val targetingType: TargetingType,
    val strings: CharacterActionStrings,
    val maxTargets: Int = 1,
    val cooldown: Int = 0,
    val duration: Int = 0
) {

    var lastActionResult: EffectResult? = null

    fun resolveEffects(from: RPGCharacter, target: RPGCharacter, cycle: Int): String? {
        return effect.takeIf { !it.isExpired(cycle) }
            ?.resolve(from, target, cycle)
            ?.also { lastActionResult = it }
            ?.let { strings.getFormattedEffectResultString(it) }
    }

    fun isExpired(cycle: Int): Boolean = effect.isExpired(cycle) && cycle >= duration
}