package tech.stephenlowery.rpgbot.core.game

import tech.stephenlowery.rpgbot.core.action.CharacterActionType
import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.core.action.QueuedCharacterActionResolvedResults
import tech.stephenlowery.rpgbot.core.character.NonPlayerCharacter
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.UserState

private val PLAYER_NOT_READY_STATES = listOf(UserState.CHOOSING_ACTION, UserState.CHOOSING_TARGETS)

open class Game(val id: Long, val initiatorId: Long, initiatorName: String) {

    val players = mutableMapOf<Long, RPGCharacter>()

    var hasStarted = false
    private var turnCounter = 0

    private val resultsHistory = mutableListOf<Collection<QueuedCharacterActionResolvedResults>>()

    init {
        println("adding initiator")
        addPlayerToGame(initiatorId, initiatorName)
    }

    var actionQueue = mutableListOf<QueuedCharacterAction>()

    fun addPlayerToGame(playerId: Long, name: String) {
        players[playerId] = PlayerCharacter(playerId, name)
    }

    fun numberOfPlayers() = players.keys.size

    fun queueActionFromCharacter(callbackData: String, userID: Long): UserState {
        return findPlayerCharacterFromID(userID)!!.apply {
            actionQueue.add(this.chooseAction(callbackData))
        }.characterState
    }

    fun addCharacter(character: PlayerCharacter) {
        players[character.id] = character
    }

    fun removeCharacters(characters: Collection<PlayerCharacter>) = removeCharactersByUserIDs(characters.map { it.id })

    private fun removeCharactersByUserIDs(userIDs: Collection<Long>) {
        userIDs.forEach(players::remove)
    }

    fun addTargetToQueuedCharacterAction(from: Long, to: Long) {
        val fromCharacter = findPlayerCharacterFromID(from)
        val toCharacter = getCharacterFromId(to)
        if (fromCharacter != null && toCharacter != null) {
            fromCharacter.addTargetToAction(toCharacter)
        } else {
            println("character missing for addTargetToCharacterAction")
        }
    }

    fun getCharacterFromId(playerId: Long): RPGCharacter? = players[playerId]

    fun findPlayerCharacterFromID(playerId: Long): PlayerCharacter? = getHumanPlayers()[playerId]

    @Suppress("UNCHECKED_CAST")
    fun getHumanPlayers(): Map<Long, PlayerCharacter> = players.filterValues { it is PlayerCharacter } as Map<Long, PlayerCharacter>

    fun containsPlayerWithID(userID: Long): Boolean = players.containsKey(userID)

    fun allPlayersAreWaiting(): Boolean = waitingOn().isEmpty()

    fun waitingOn(): Collection<PlayerCharacter> = livingPlayers<PlayerCharacter>().filter {
        it.characterState in PLAYER_NOT_READY_STATES
    }

    protected inline fun <reified T : RPGCharacter> livingPlayers(): Collection<T> = players.values.filterIsInstance<T>().filter { it.isAlive() }

    @JvmName("livingPlayers1")
    fun livingPlayers(): Collection<RPGCharacter> = players.values.filter { it.isAlive() }

    fun deadPlayers(): Collection<RPGCharacter> = players.values.filterNot { it.isAlive() }

    open fun resolveActionsAndGetResults(): String {
        val npcActions = livingPlayers<NonPlayerCharacter>().mapNotNull { it.getQueuedAction(this) }
        val queuedActions = listOf(actionQueue, npcActions).flatMap(::partitionAndShuffleActionQueue).toMutableList()
        val results = resolveActions(queuedActions)
        val stringResults = results.map(QueuedCharacterActionResolvedResults::stringResult).toMutableList()
        queuedActions.removeIf { it.isExpired() }
        actionQueue = queuedActions
        players.values.forEach { player ->
            if (player.getActualHealth() <= 0 && player.characterState != UserState.DEAD) {
                stringResults.add("${player.name} died! They will be removed from the game.")
                player.characterState = UserState.DEAD
            } else {
                player.resetForNextTurnAfterAction()
            }
        }
        resultsHistory.add(results)
        turnCounter += 1
        return listOf("*----Turn $turnCounter results----*", stringResults.joinToString("\n\n")).joinToString("\n\n")
    }

    open fun startGame(): Collection<Pair<Long, String>> {
        startGameStateAndPrepCharacters()
        return players.map { it.key to "You've entered a free-for-all game. The last one alive wins. Good luck ${it.value.name}." }
    }

    open fun isOver() = livingPlayers().size > 1

    open fun getGameEndedText(): String = when (livingPlayers().size) {
        1    -> "${livingPlayers().first().name} wins!"
        0    -> "Uh, well, I guess the remaining people died at the same time or something. Ok."
        else -> "Uh oh, this shouldn't happen."
    }

    open fun getTargetsForCharacter(character: PlayerCharacter): Collection<RPGCharacter> {
        return charactersBesidesSelf(character)
    }

    fun cancel() {
        actionQueue.clear()
        players.clear()
        turnCounter = 0
        players.values.forEach(RPGCharacter::resetCharacter)
        players.clear()
        actionQueue.clear()
        System.gc()
    }

    open fun numberOfPlayersIsInvalid() = numberOfPlayers() < 2

    protected fun startGameStateAndPrepCharacters() {
        hasStarted = true
        players.values.forEach {
            it.characterState = UserState.CHOOSING_ACTION
        }
    }

    private fun charactersBesidesSelf(character: PlayerCharacter): Collection<RPGCharacter> {
        return livingPlayers().filter { it.id != character.id }
    }

    private fun partitionAndShuffleActionQueue(actionQueue: Collection<QueuedCharacterAction>): Collection<QueuedCharacterAction> {
        return actionQueue
            .partition { it.action.actionType == CharacterActionType.DEFENSIVE }
            .let { it.first.shuffled() + it.second.shuffled() }
    }

    private fun resolveActions(sortedActionQueue: MutableCollection<QueuedCharacterAction>): MutableCollection<QueuedCharacterActionResolvedResults> {
        val actionResults = mutableListOf<QueuedCharacterActionResolvedResults>()
        val deadPlayersThisTurn = mutableSetOf<Long>()
        val actionQueueIterator = sortedActionQueue.iterator()
        while (actionQueueIterator.hasNext()) {
            val currentAction = actionQueueIterator.next()
            if (currentAction.target?.id in deadPlayersThisTurn || actionIsUnresolvedAndFromADeadPlayer(currentAction, deadPlayersThisTurn)) {
                actionQueueIterator.remove()
                continue
            }
            val currentActionResults = currentAction.cycleAndResolve()
            currentAction.target?.apply {
                if (!this.isAlive() && this.id !in deadPlayersThisTurn) {
                    deadPlayersThisTurn.add(this.id)
                    currentActionResults.actionResultedInDeath = true
                }
            }
            actionResults.add(currentActionResults)
        }
        return actionResults
    }

    private fun actionIsUnresolvedAndFromADeadPlayer(action: QueuedCharacterAction, deadPlayers: Set<Long>): Boolean {
        return action.isUnresolved() && action.source.id in deadPlayers
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Game) return false

        if (id != other.id) return false
        return initiatorId == other.initiatorId
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + initiatorId.hashCode()
        return result
    }

}
