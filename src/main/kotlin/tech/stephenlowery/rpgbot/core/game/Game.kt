package tech.stephenlowery.rpgbot.core.game

import tech.stephenlowery.rpgbot.core.action.CharacterActionType
import tech.stephenlowery.rpgbot.core.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.core.action.QueuedCharacterActionResolvedResults
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.UserState

open class Game(val id: Long, val initiator: PlayerCharacter) {

    val playerList = mutableListOf<RPGCharacter>()
    var gameStarted = false
    var turnCounter = 0

    var actionQueue = mutableListOf<QueuedCharacterAction>()

    init {
        playerList.add(initiator)
    }

    fun queueActionFromCharacter(callbackData: String, userID: Long): UserState {
        return findPlayerCharacterFromID(userID)!!.apply {
            actionQueue.add(this.chooseAction(callbackData))
        }.characterState
    }

    fun addPlayer(character: PlayerCharacter) = playerList.add(character)

    fun addPlayers(characters: Collection<PlayerCharacter>) = playerList.addAll(characters)

    fun removePlayersByUserIDs(userIDs: Collection<Long>) = playerList.removeIf { it is PlayerCharacter && userIDs.contains(it.id) }

    fun removePlayers(characters: Collection<PlayerCharacter>) = removePlayersByUserIDs(characters.map { it.id })

    fun addTargetToQueuedCharacterAction(from: Long, to: Long) {
        val fromCharacter = findPlayerCharacterFromID(from)
        val toCharacter = getCharacterFromID(to)
        if (fromCharacter != null && toCharacter != null) {
            fromCharacter.addTargetToAction(toCharacter)
        } else {
            println("character missing for addTargetToCharacterAction")
        }
    }

    fun containsPlayerWithID(userID: Long): Boolean = playerList.filterIsInstance<PlayerCharacter>().any { it.id == userID }

    fun allPlayersAreWaiting(): Boolean = waitingOn().isEmpty()

    fun waitingOn(): Collection<PlayerCharacter> = livingPlayers<PlayerCharacter>().filter { it.characterState in listOf(UserState.CHOOSING_ACTION, UserState.CHOOSING_TARGETS) }

    private inline fun <reified T: RPGCharacter> livingPlayers(): Collection<T> = playerList.filterIsInstance<T>().filter { it.isAlive() }

    @JvmName("livingPlayers1")
    fun livingPlayers(): Collection<RPGCharacter> = playerList.filter { it.isAlive() }

    fun deadPlayers(): Collection<RPGCharacter> = playerList.filterNot { it.isAlive() }

    open fun resolveActionsAndGetResults(): String {
        val queuedActions = getSortedActionsToResolve().toMutableList()
        val results = resolveActions(queuedActions).map { it.stringResult }.toMutableList()
        queuedActions.removeIf { it.isExpired() }
        actionQueue = queuedActions
        playerList.forEach { player ->
            if (player.getActualHealth() <= 0 && player.characterState != UserState.DEAD) {
                results.add("${player.name} died! They will be removed from the game.")
                player.characterState = UserState.DEAD
            } else {
                player.resetForNextTurnAfterAction()
            }
        }
        turnCounter += 1
        return listOf("*----Turn $turnCounter results----*", results.joinToString("\n\n")).joinToString("\n\n")
    }

    open fun startGame() {
        if (playerList.size > 1) {
            startGameStateAndPrepCharacters()
        }
    }

    fun getGameEndedText(): String = when (livingPlayers().size) {
        1    -> "${livingPlayers().first().name} wins!"
        0    -> "Uh, well, I guess the remaining people died at the same time or something. Ok."
        else -> "Uh oh, this shouldn't happen."
    }

    fun cancel() {
        playerList.forEach(RPGCharacter::resetCharacter)
        playerList.clear()
        actionQueue.clear()
    }

    protected fun startGameStateAndPrepCharacters() {
        gameStarted = true
        playerList.forEach {
            it.characterState = UserState.CHOOSING_ACTION
        }
    }

    private fun getCharacterFromID(id: Long): RPGCharacter? = playerList.find { it.id == id }

    private fun findPlayerCharacterFromID(id: Long): PlayerCharacter? = playerList.filterIsInstance<PlayerCharacter>().find { it.id == id }

    private fun getSortedActionsToResolve(): Collection<QueuedCharacterAction> {
        return actionQueue.partition { it.action.actionType == CharacterActionType.DEFENSIVE }.let { it.first.shuffled() + it.second.shuffled() }
    }

    private fun resolveActions(sortedActionQueue: MutableCollection<QueuedCharacterAction>): MutableCollection<QueuedCharacterActionResolvedResults> {
        val actionResults = mutableListOf<QueuedCharacterActionResolvedResults>()
        val deadPlayersThisTurn = mutableListOf<RPGCharacter>()
        val actionQueueIterator = sortedActionQueue.iterator()
        while (actionQueueIterator.hasNext()) {
            val currentAction = actionQueueIterator.next()
            if (currentAction.target in deadPlayersThisTurn || actionIsUnresolvedAndFromADeadPlayer(currentAction, deadPlayersThisTurn)) {
                actionQueueIterator.remove()
            } else {
                currentAction.cycleAndResolve()?.let { actionResults.add(it) }
                currentAction.target?.run {
                    if (!this.isAlive() && this !in deadPlayersThisTurn) {
                        deadPlayersThisTurn.add(this)
                    }
                }
            }
        }
        return actionResults
    }

    private fun actionIsUnresolvedAndFromADeadPlayer(action: QueuedCharacterAction, deadPlayers: List<RPGCharacter>): Boolean {
        return action.isUnresolved() && action.source in deadPlayers
    }
}
