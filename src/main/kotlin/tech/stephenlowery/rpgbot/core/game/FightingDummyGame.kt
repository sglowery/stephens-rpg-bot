package tech.stephenlowery.rpgbot.core.game

import tech.stephenlowery.rpgbot.core.character.NonPlayerCharacter
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter

class FightingDummyGame(id: Long, initiator: PlayerCharacter) : Game(id, initiator) {

    private val dummy = NonPlayerCharacter("Debug Dummy", 1)

    init {
        playerList += dummy
    }

    override fun startGame() {
        startGameStateAndPrepCharacters()
    }

    override fun resolveActionsAndGetResults(): String {
        val results = super.resolveActionsAndGetResults()
        dummy.resetCharacter()
        return results
    }

}