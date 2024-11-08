package tech.stephenlowery.rpgbot.core.game

import tech.stephenlowery.rpgbot.core.action.CharacterActionType
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
    var turnCounter = 0

    protected val resultsHistory = mutableListOf<Collection<QueuedCharacterActionResolvedResults>>()

    init {
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

    fun addTargetToQueuedCharacterAction(from: Long, to: Long) {
        val fromCharacter = findPlayerCharacterFromID(from)
        val toCharacter = getCharacterFromId(to)
        if (fromCharacter != null && toCharacter != null) {
            fromCharacter.addTargetToAction(toCharacter)
            actionQueue.add(fromCharacter.queuedAction!!)
        } else {
            println("target, character or action missing for addTargetToCharacterAction")
        }
    }

    fun getCharacterFromId(playerId: Long): RPGCharacter? = players[playerId]

    fun findPlayerCharacterFromID(playerId: Long): PlayerCharacter? = getHumanPlayers()[playerId]

    @Suppress("UNCHECKED_CAST")
    fun getHumanPlayers(): Map<Long, PlayerCharacter> = players.filterValues { it is PlayerCharacter } as Map<Long, PlayerCharacter>

    fun containsPlayerWithID(userID: Long): Boolean = players.containsKey(userID)

    fun allPlayersReadyForTurnToResolve(): Boolean = waitingOn().isEmpty()

    fun waitingOn(): Collection<PlayerCharacter> = livingPlayers<PlayerCharacter>().filter {
        it.characterState in PLAYER_NOT_READY_STATES
    }

    @JvmName("livingPlayers1")
    fun livingPlayers(): Collection<RPGCharacter> = players.values.filter { it.isAlive() }

    fun deadPlayers(): Collection<RPGCharacter> = players.values.filterNot { it.isAlive() }

    open fun resolveActionsAndGetResults(): String {
        val npcActions = livingPlayers<NonPlayerCharacter>()
            .filter { it.characterState != UserState.OCCUPIED }
            .mapNotNull { it.queueAction(this) }
        val queuedActions = listOf(actionQueue, npcActions)
            .flatMap(::partitionAndShuffleActionQueue)
            .toMutableList()
        queuedActions.forEach {
            val action = it.action
            if (action.cooldown > 0 && !it.source.isActionOnCooldown(action.identifier) && !it.cooldownApplied) {
                it.source.setCooldownForAction(action)
                it.cooldownApplied = true
            }
        }
        val results: MutableCollection<QueuedCharacterActionResolvedResults> = resolveActions(queuedActions)
        val stringResults: MutableList<String> = results.map { it.stringResult }.toMutableList()
        queuedActions.removeIf { it.isExpired() }
        actionQueue = queuedActions
        players.values.forEach { player ->
            if (player.getActualHealth() <= 0 && player.characterState != UserState.DEAD) {
                stringResults.add("${player.name} died! They will be removed from the game.")
                removeEffectsTargetingCharacter(player)
            } else {
                player.resetForNextTurnAfterAction()
            }
        }
        resultsHistory.add(results)
        return listOf("*----Turn ${++turnCounter} results----*", stringResults.joinToString("\n\n")).joinToString("\n\n")
    }

    private fun removeEffectsTargetingCharacter(player: RPGCharacter) {
        actionQueue.removeIf { it.target == player }
    }

    open fun startGame(): Collection<Pair<Long, String>> {
        startGameStateAndPrepCharacters()
        return players
            .filterValues { it is PlayerCharacter }
            .map { it.key to getGameStartedMessageForPlayer(it.value as PlayerCharacter) }
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

    fun getPostGameStatsString(): String {
        val idToPlayerGameStatsMap: Map<Long, PlayerGameStats> =
            players.values.associate { it.id to PlayerGameStats(it.name, it is NonPlayerCharacter, it.getActualHealth()) }
        var totalDamageDone = 0
        var totalHealingDone = 0
        resultsHistory.forEachIndexed { round, queuedActionResultsForRound ->
            queuedActionResultsForRound.forEach { queuedResult ->
                queuedResult.effectResults.forEach { actionEffectResult ->
                    val source = actionEffectResult.source
                    val target = actionEffectResult.target
                    val value = actionEffectResult.value
                    val fromStats = idToPlayerGameStatsMap[source.id]!!
                    val targetStats = idToPlayerGameStatsMap[target.id]!!
                    when (actionEffectResult.actionType) {
                        CharacterActionType.DAMAGE      -> {
                            fromStats.damageDone += value
                            targetStats.damageTaken += value
                            totalDamageDone += value
                            if (queuedResult.actionResultedInDeath) {
                                targetStats.diedOnRound = round + 1
                                fromStats.playersKilled.add(target.name)
                            }
                        }
                        CharacterActionType.HEALING     -> {
                            fromStats.healingDone += value
                            targetStats.healingTaken += value
                            totalHealingDone += value
                        }
                        CharacterActionType.DAMAGE_HEAL -> {
                            val healing = actionEffectResult.other?.toInt() ?: 0
                            fromStats.damageDone += value
                            fromStats.healingDone += healing
                            fromStats.healingTaken += healing
                            targetStats.damageTaken += value
                            totalDamageDone += value
                            totalHealingDone += healing
                            if (queuedResult.actionResultedInDeath) {
                                targetStats.diedOnRound = round + 1
                                fromStats.playersKilled.add(target.name)
                            }
                        }
                        else                            -> {}
                    }
                }
            }
        }
        return "Post-Game Stats:\n\n" +
                "Total damage done: $totalDamageDone\n" +
                "Total healing done: $totalHealingDone\n\n" +
                idToPlayerGameStatsMap.values.joinToString(
                    separator = "\n\n",
                    transform = PlayerGameStats::toString,
                )
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

    open fun numberOfPlayersIsValid() = numberOfPlayers() >= 2

    protected fun startGameStateAndPrepCharacters() {
        hasStarted = true
        players.values.forEach {
            it.characterState = UserState.CHOOSING_ACTION
        }
    }

    protected inline fun <reified T : RPGCharacter> livingPlayers(): Collection<T> = players.values.filterIsInstance<T>().filter { it.isAlive() }

    private fun getGameStartedMessageForPlayer(player: PlayerCharacter): String {
        return listOf(
            description,
            player.getFirstTimeGameStartedCharacterText()
        ).joinToString("\n\n")
    }

    private fun removeCharactersByUserIDs(userIDs: Collection<Long>) {
        userIDs.forEach(players::remove)
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
