package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class QueuedCharacterAction(
    val action: CharacterAction,
    val source: RPGCharacter
) {

    var target: RPGCharacter? = null

    private var cycle = 0

    private var lastActionResult: EffectResult? = null

    fun cycleAndResolve(): QueuedCharacterActionResolvedResults {
        val results = action.applyEffect(source, target!!, cycle)
        lastActionResult = results.first()
        cycle++
        return QueuedCharacterActionResolvedResults(action, results)
    }

    fun isExpired(): Boolean = action.isExpired(cycle) || lastActionResult?.miss == true

    fun isUnresolved(): Boolean = cycle == 0

    fun getQueuedText(): String {
        return action.strings.getFormattedQueuedText(source, target)
    }
}

class QueuedCharacterActionResolvedResults(
    private val action: CharacterAction,
    private val effectResults: List<EffectResult>,
    private val effectResultSeparator: String = "\n\n",
    private val stringResultOverride: String? = null
) {

    var actionResultedInDeath = false

    val stringResult: String
        get() = stringResultOverride ?: effectResults.joinToString(effectResultSeparator, transform = action.strings::getFormattedEffectResultString)
}