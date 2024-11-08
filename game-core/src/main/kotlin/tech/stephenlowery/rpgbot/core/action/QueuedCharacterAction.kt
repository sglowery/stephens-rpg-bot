package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class QueuedCharacterAction(
    val action: CharacterAction,
    val source: RPGCharacter,
    var target: RPGCharacter? = null,
) {

    var cooldownApplied = false

    private var cycle = 0

    private var previousPrimaryResult: EffectResult? = null

    fun cycleAndResolve(): QueuedCharacterActionResolvedResults {
        val results = action.applyEffect(source, target!!, cycle++)
        previousPrimaryResult = results.first()
        return QueuedCharacterActionResolvedResults(action, results)
    }

    fun isExpired(): Boolean = action.isExpired(cycle) || previousPrimaryResult?.miss == true

    fun isUnresolved(): Boolean = cycle == 0

    fun getQueuedText(): String {
        return action.strings.getFormattedQueuedText(source, target)
    }
}

class QueuedCharacterActionResolvedResults(
    val action: CharacterAction,
    val effectResults: List<EffectResult>,
    private val effectResultSeparator: String = "\n\n",
    private val stringResultOverride: String? = null,
) {

    var actionResultedInDeath = false

    val stringResult: String
        get() = stringResultOverride ?: action.strings.getFormattedEffectResultString(effectResults.first())
}