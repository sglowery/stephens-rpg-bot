package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class QueuedCharacterAction(
    val action: CharacterAction,
    val source: RPGCharacter
) {
    private var cycle = 0

    var target: RPGCharacter? = null

    fun cycleAndResolve(): QueuedCharacterActionResolvedResults? {
        val results = action.applyEffect(source, target!!, cycle)?.let { QueuedCharacterActionResolvedResults(action, it) }
        cycle++
        return results
    }

    fun isExpired(): Boolean = action.isExpired(cycle) || (action.lastActionResult?.miss ?: false)

    fun isUnresolved(): Boolean = cycle == 0

    fun getQueuedText(): String {
        return action.strings.getFormattedQueuedText(source, target)
    }
}

class QueuedCharacterActionResolvedResults(
    val action: CharacterAction,
    val effectResults: List<EffectResult>,
    private val effectResultSeparator: String = "\n\n"
) {
    val stringResult: String
        get() = effectResults.joinToString(effectResultSeparator) { action.strings.getFormattedEffectResultString(it) }
}