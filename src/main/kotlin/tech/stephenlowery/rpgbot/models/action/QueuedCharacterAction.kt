package tech.stephenlowery.rpgbot.models.action

import tech.stephenlowery.rpgbot.models.character.RPGCharacter

class QueuedCharacterAction(
    val action: CharacterAction,
    val from: RPGCharacter
) {
    private var cycle = 0

    var target: RPGCharacter? = null

    fun cycleAndResolve(): String = action.resolveEffects(from, target!!, cycle).also { cycle += 1 }

    fun isExpired(): Boolean = action.isExpired(cycle) || (action.lastActionResults?.get(0)?.miss ?: false)

    fun getQueuedText(): String {
        return action.strings.queuedText.formatFromEffectResult(EffectResult(source = from, target = target))
    }
}
