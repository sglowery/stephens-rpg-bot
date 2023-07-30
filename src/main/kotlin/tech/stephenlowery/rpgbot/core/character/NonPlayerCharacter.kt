package tech.stephenlowery.rpgbot.core.character

import tech.stephenlowery.rpgbot.core.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.core.character.attribute.Attribute
import tech.stephenlowery.rpgbot.core.game.Game

class NonPlayerCharacter(
    name: String,
    id: Long,
    healthValue: Int? = null,
    powerValue: Int? = null,
    defenseValue: Int? = null,
    precisionValue: Int? = null,
    private val actionDecidingBehavior: (NonPlayerCharacter.(Game) -> QueuedCharacterAction)? = null,
) : RPGCharacter(name, id) {

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

    fun getQueuedAction(game: Game): QueuedCharacterAction? = actionDecidingBehavior?.invoke(this, game)

    private fun initAttributes(vararg valueAttributePairs: Pair<Int?, Attribute>) {
        valueAttributePairs.filter { it.first != null }.forEach { it.second.base = it.first!!.toDouble() }
    }
}