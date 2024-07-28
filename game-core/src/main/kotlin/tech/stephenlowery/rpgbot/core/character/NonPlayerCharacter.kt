package tech.stephenlowery.rpgbot.core.character

import tech.stephenlowery.rpgbot.core.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.core.action.TargetingType
import tech.stephenlowery.rpgbot.core.character.attribute.Attribute
import tech.stephenlowery.rpgbot.core.game.Game

class NonPlayerCharacter(
    name: String,
    id: Long,
    healthValue: Int? = null,
    powerValue: Int? = null,
    defenseValue: Int? = null,
    precisionValue: Int? = null,
    private val actionDecidingBehavior: (NonPlayerCharacter.(Game) -> QueuedCharacterAction?)? = null,
) : RPGCharacter(id, name) {

    init {
        giveRandomStats()
        initAttributes(
            healthValue to health,
            powerValue to power,
            defenseValue to defense,
            precisionValue to precision
        )
        setHealthBounds()
    }

    fun queueAction(game: Game): QueuedCharacterAction? {
        return getQueuedAction(game).also { setTargetForAction(it) }
    }

    private fun getQueuedAction(game: Game): QueuedCharacterAction? = actionDecidingBehavior?.invoke(this, game)

    private fun setTargetForAction(queuedAction: QueuedCharacterAction?) {
        if (queuedAction?.target != null) {
            return
        }
        queuedAction?.target = when(queuedAction?.action?.targetingType) {
            null -> null
            TargetingType.SELF -> this
            else -> queuedAction.target
        }
    }

    private fun initAttributes(vararg valueAttributePairs: Pair<Int?, Attribute>) {
        valueAttributePairs.forEach { it.second.base = it.first?.toDouble() ?: 1.0 }
    }

    override fun toString(): String {
        return "NonPlayerCharacter(id=$id, name=$name)"
    }
}