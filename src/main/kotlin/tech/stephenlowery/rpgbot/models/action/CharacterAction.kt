package tech.stephenlowery.rpgbot.models.action

import tech.stephenlowery.rpgbot.models.character.RPGCharacter

class CharacterAction(
    val effects: List<ActionEffect>,
    val displayName: String,
    val callbackText: String,
    val description: String,
    val targetingType: TargetingType,
    val strings: CharacterActionStrings,
    val maxTargets: Int = 1,
    val cooldown: Int = 0,
    val duration: Int = 0
) {

    var lastActionResults: List<EffectResult>? = null

    fun resolveEffects(from: RPGCharacter, target: RPGCharacter, cycle: Int): String {
        return getUnexpiredEffects(cycle).map { effect ->
            val results = effect.resolve(from, target, cycle)
            lastActionResults = results
            return results.map { result ->
                return strings.getFormattedEffectResultString(result)
            }.joinToString("\n")
        }.joinToString("\n")
    }

    fun isExpired(cycle: Int): Boolean = allEffectsExpired(cycle) && cycle >= duration

    fun getUnexpiredEffects(cycle: Int): List<ActionEffect> = effects.filter { !it.isExpired(cycle) }

    fun allEffectsExpired(cycle: Int): Boolean = getUnexpiredEffects(cycle).isEmpty()
}