package tech.stephenlowery.rpgbot.models

class CharacterAction(
    val effects: List<ActionEffect>,
    val displayName: String,
    val callbackText: String,
    val targetingType: TargetingType,
    val strings: CharacterActionStrings,
    val maxTargets: Int = 1,
    val cooldown: Int = 0
) {

    fun resolveEffects(from: RPGCharacter, targets: List<RPGCharacter>, cycle: Int): String {
        return targets.map { target ->
            return getUnexpiredEffects(cycle).map { effect ->
                val results = effect.resolve(from, target, cycle)
                return results.map { result ->
                    return strings.getFormattedEffectResultString(result)
                }.joinToString("\n")
            }.joinToString("\n")
        }.joinToString("\n")
    }

    fun getUnexpiredEffects(cycle: Int): List<ActionEffect> = effects.filter { !it.isExpired(cycle) }

    fun allEffectsExpired(cycle: Int): Boolean = getUnexpiredEffects(cycle).isEmpty()

}