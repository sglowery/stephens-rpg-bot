package tech.stephenlowery.rpgbot.models

import tech.stephenlowery.rpgbot.models.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.models.character.RPGCharacter
import tech.stephenlowery.rpgbot.models.character.UserState

class Game(val id: Long, initiator: RPGCharacter) {

    val playerList = mutableListOf<RPGCharacter>()
    var gameStarted = false
    var turnCounter = 0

    val actionQueue = mutableListOf<QueuedCharacterAction>()

    init {
        playerList.add(initiator)
    }

    fun queueActionFromCharacter(callbackData: String, userID: Long): UserState {
        val character = playerList.find { it.userID == userID }!!
        val queuedAction = character.chooseAction(callbackData)
        actionQueue.add(queuedAction)
        return character.characterState
    }

    fun addTargetToQueuedCharacterAction(from: Long, to: Long) {
        val fromCharacter = getCharacterFromUserID(from)
        val toCharacter = getCharacterFromUserID(to)
        fromCharacter.addTargetToAction(toCharacter)
    }

    fun playerInGame(userID: Long): Boolean = playerList.any { it.userID == userID }

    fun allPlayersWaiting(): Boolean = waitingOn().isEmpty()

    fun waitingOn(): List<RPGCharacter> = livingPlayers().filter { it.characterState == UserState.CHOOSING_ACTION || it.characterState == UserState.CHOOSING_TARGETS }

    fun livingPlayers(): List<RPGCharacter> = playerList.filter { it.isAlive() }

    fun deadPlayers(): List<RPGCharacter> = playerList.filter { !it.isAlive() }

    fun resolveActions(): String {
        val results = actionQueue.shuffled().mapNotNull { it.cycleAndResolve() }.toMutableList()
        actionQueue.removeIf { it.isExpired() }
        playerList.forEach { player ->
            if (player.getActualHealth() <= 0 && player.characterState != UserState.DEAD) {
                results.add("${player.name} died! They will be removed from the game.")
                player.characterState = UserState.DEAD
            } else {
                player.characterState = UserState.CHOOSING_ACTION
                player.clearQueuedAction()
                player.cycleAttributeModifiers()
                player.cycleCooldowns()
            }
        }
        turnCounter += 1
        return listOf("*----Turn $turnCounter results----*", results.joinToString("\n\n")).joinToString("\n\n")
    }

    fun startGame() {
        if (playerList.size > 1) {
            gameStarted = true
            playerList.forEach {
                it.characterState = UserState.CHOOSING_ACTION
            }
        }
    }

    fun getGameEndedText(): String = when (livingPlayers().size) {
        1 -> "${livingPlayers().first().name} wins!"
        0 -> "Uh, well, I guess the remaining people died at the same time or something. Ok."
        else -> "Uh oh, this shouldn't happen."
    }

    fun cancel() {
        playerList.forEach(RPGCharacter::resetCharacter)
        playerList.clear()
        actionQueue.clear()
    }

    private fun getCharacterFromUserID(userID: Long): RPGCharacter = playerList.find { it.userID == userID }!!
}
