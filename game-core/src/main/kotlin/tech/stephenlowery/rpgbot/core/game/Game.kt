package tech.stephenlowery.rpgbot.core.game

import tech.stephenlowery.rpgbot.core.action.*
import tech.stephenlowery.rpgbot.core.character.CharacterState
import tech.stephenlowery.rpgbot.core.character.NonPlayerCharacter
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.equipment.EquipmentAction

private val PLAYER_NOT_READY_STATES = listOf(CharacterState.CHOOSING_ACTION, CharacterState.CHOOSING_TARGETS)

open class Game(
    val id: Long,
    val initiatorId: Long,
    initiatorName: String,
    val description: String,
) {

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

    fun queueActionFromCharacter(callbackData: String, userID: Long): CharacterState {
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

    @Suppress("UNCHECKED_CAST")
    fun getLiveHumanPlayers(): Map<Long, PlayerCharacter> = getHumanPlayers().filterValues { it.isAlive() }

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
            .filter { it.characterState != CharacterState.OCCUPIED }
            .mapNotNull { it.queueAction(this) }
        val queuedActions = listOf(actionQueue, npcActions)
            .flatMap(::partitionAndShuffleActionQueue)
            .toMutableList()
        queuedActions.forEach { queuedAction ->
            val equipmentAction = queuedAction.equipmentAction
            val action = equipmentAction.characterAction
            if (action.cooldown > 0 && !queuedAction.source.isActionOnCooldown(equipmentAction.identifier) && !queuedAction.cooldownApplied) {
                queuedAction.source.setCooldownForAction(action)
                queuedAction.cooldownApplied = true
            }
        }
        val results: MutableList<QueuedCharacterActionResolvedResults> = resolveActions(queuedActions)
        val stringResults: MutableList<String> = mutableListOf()
        for (result in results) {
            if (result.stringResult.trim().isNotEmpty()) {
                stringResults.add(result.stringResult)
            }
            if (result.actionResultedInDeath) {
                val killedTargetName = result.effectResults.first().target.name
                stringResults.add("$killedTargetName died! They will be removed from the game.")
            }
        }
        queuedActions.removeIf { it.isExpired() }
        actionQueue = queuedActions
        players.values.forEach { player ->
            if (player.getHealthMinusDamage() <= 0 && player.characterState != CharacterState.DEAD) {
                removeEffectsTargetingCharacter(player)
                removePendingActionsFromCharacter(player)
            } else {
                player.resetForNextTurnAfterAction()
            }
        }
        resultsHistory.add(results)
        return listOf("*----Turn ${++turnCounter} results----*", stringResults.joinToString("\n\n")).joinToString("\n\n")
    }

    private fun removePendingActionsFromCharacter(character: RPGCharacter) {
        actionQueue.removeIf { it.source == character }
    }

    private fun removeEffectsTargetingCharacter(character: RPGCharacter) {
        actionQueue.removeIf { it.target == character }
    }

    open fun startGame(): Collection<Pair<Long, String>> {
        startGameStateAndPrepCharacters()
        return players
            .filterValues { it is PlayerCharacter }
            .map { it.key to getGameStartedMessageForPlayer(it.value as PlayerCharacter) }
    }

    open fun isOver() = livingPlayers().size <= 1

    open fun getGameEndedText(): String = when (livingPlayers().size) {
        1    -> "${livingPlayers().first().name} wins!"
        0    -> "Uh, well, I guess the remaining people died at the same time or something. Ok."
        else -> "Uh oh, this shouldn't happen."
    }

    fun getTargetsForCharacter(character: PlayerCharacter): Collection<RPGCharacter> {
        val equipmentAction = character.queuedAction!!.equipmentAction
        val action = equipmentAction.characterAction
        val targetingType = action.targetingType
        val targetIntent = action.targetIntent
        val selfList = if (targetingType == TargetingType.SINGLE_TARGET_INCLUDING_SELF) listOf(character) else emptyList()
        return selfList + when (targetIntent) {
            TargetIntent.FRIENDLY -> getFriendliesForCharacter(character, targetingType)
            TargetIntent.HOSTILE  -> getEnemiesForCharacter(character, targetingType)
            TargetIntent.ANY      -> getAllTargetsForCharacter(character, targetingType)
        }
    }

    protected open fun getFriendliesForCharacter(character: PlayerCharacter, targetingType: TargetingType): Collection<RPGCharacter> {
        return getAllLivingHumanPlayersBesidesSelf(character)
    }

    protected open fun getEnemiesForCharacter(character: PlayerCharacter, targetingType: TargetingType): Collection<RPGCharacter> {
        return getAllLivingHumanPlayersBesidesSelf(character)
    }

    protected open fun getAllTargetsForCharacter(character: PlayerCharacter, targetingType: TargetingType): Collection<RPGCharacter> {
        return getAllLivingHumanPlayersBesidesSelf(character)
    }

    private fun getAllLivingHumanPlayersBesidesSelf(character: PlayerCharacter): Collection<RPGCharacter> {
        return getLiveHumanPlayers().values.filter { it.id != character.id }
    }

    fun getPostGameStatsString(): String {
        val idToPlayerGameStatsMap: Map<Long, PlayerGameStats> =
            players.values.associate { it.id to PlayerGameStats(it.name, it is NonPlayerCharacter, it.getHealthMinusDamage()) }
        var totalDamageDone = 0
        var totalHealingDone = 0
        resultsHistory.forEachIndexed { roundIndex, queuedActionResultsForRound ->
            queuedActionResultsForRound.forEach { queuedResult ->
                queuedResult.effectResults.forEach { actionEffectResult ->
                    val source = actionEffectResult.source
                    val target = actionEffectResult.target
                    val value = actionEffectResult.value
                    val fromStats = idToPlayerGameStatsMap[source.id]!!
                    val targetStats = idToPlayerGameStatsMap[target.id]!!
                    val round = roundIndex + 1
                    when (actionEffectResult.actionType) {
                        CharacterActionType.DAMAGE      -> {
                            fromStats.damageDone += value
                            targetStats.damageTaken += value
                            totalDamageDone += value
                            if (queuedResult.actionResultedInDeath) {
                                targetStats.diedOnRound = round
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
                                targetStats.diedOnRound = round
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
                idToPlayerGameStatsMap.values.filter(::characterShouldBeIncludedInPostGameStats).joinToString(separator = "\n\n") { it.toString(turnCounter) }
    }

    open fun characterShouldBeIncludedInPostGameStats(character: PlayerGameStats): Boolean = true

    fun cancel() {
        actionQueue.clear()
        players.values.forEach(RPGCharacter::resetCharacter)
        players.clear()
        turnCounter = 0
        System.gc()
    }

    open fun getGameGrantedActions(): Collection<EquipmentAction> {
        return emptyList()
    }

    open fun numberOfPlayersIsValid() = numberOfPlayers() >= 2

    protected fun startGameStateAndPrepCharacters() {
        hasStarted = true
        players.values.forEach {
            it.characterState = CharacterState.CHOOSING_ACTION
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
            .partition { it.equipmentAction.characterAction.actionType == CharacterActionType.DEFENSIVE }
            .let { it.first.shuffled() + it.second.shuffled() }
    }

    private fun resolveActions(sortedActionQueue: MutableCollection<QueuedCharacterAction>): MutableList<QueuedCharacterActionResolvedResults> {
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
            val target = currentAction.target
            if (target?.isAlive() == false && target.id !in deadPlayersThisTurn) {
                deadPlayersThisTurn.add(target.id)
                currentActionResults.actionResultedInDeath = true
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
