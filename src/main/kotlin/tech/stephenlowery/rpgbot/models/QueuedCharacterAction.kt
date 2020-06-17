package tech.stephenlowery.rpgbot.models;

class QueuedCharacterAction(
    val action: CharacterAction,
    val from: RPGCharacter,
    var targets: MutableList<RPGCharacter> = mutableListOf(),
    var cycle: Int = 0
) {
    fun cycleAndResolve(): String {
        val results = action.resolveEffects(from, targets, cycle)
        cycle += 1
        return results
    }

    fun isExpired(): Boolean = action.allEffectsExpired(cycle)

    fun getQueuedText(): String {
        return action.strings.queuedText.formatFromEffectResult(EffectResult(source = from))
    }
 }
