package tech.stephenlowery.rpgbot.models

class Game(val id: Long, initiator: RPGCharacter) {

    val playerList = mutableListOf<RPGCharacter>()
    var gameStarted = false
    var turnCounter = 0

    val actionQueue = mutableListOf<QueuedCharacterAction>()

    init {
        playerList.add(initiator)
    }

    fun queueActionFromCharacter(callbackData: String, userID: Long): UserState {
        val character = getPlayerWithID(userID)
        val queuedAction = character.chooseAction(callbackData)
        actionQueue.add(queuedAction)
        return character.characterState
    }

    fun addTargetToQueuedCharacterAction(from: Long, to: Long): UserState {
        val fromCharacter = getPlayerWithID(from)
        val toCharacter = getPlayerWithID(to)
        val newCharacterState = fromCharacter.addTargetToAction(toCharacter)
        return newCharacterState
    }

    fun getPlayerWithID(userID: Long): RPGCharacter = playerList.find { it.userID == userID }!!

    fun playerInGame(userID: Long): Boolean = playerList.any { it.userID == userID }

    fun livingPlayers(): List<RPGCharacter> = playerList.filter { it.isAlive() }

    fun waitingOn(): List<RPGCharacter> =
        playerList.filter { it.characterState == UserState.CHOOSING_ACTION || it.characterState == UserState.CHOOSING_TARGETS }

    fun allPlayersWaiting(): Boolean = waitingOn().isEmpty()

    fun resolveActions(): String {
        val results = actionQueue.map { it.cycleAndResolve() }.toMutableList()
        actionQueue.removeIf { it.isExpired() }
        playerList.forEach {
            if (it.getHealth() <= 0) {
                results.add("${it.name} died! They will be removed from the game")
                it.characterState = UserState.DEAD
                actionQueue.forEach { action -> action.targets.removeIf { target -> target.userID == it.userID } }
            } else {
                it.characterState = UserState.CHOOSING_ACTION
                it.clearQueuedAction()
                it.cycleAttributeModifiers()
                it.cycleCooldowns()
            }
        }
//        playerList.removeIf { it.characterState == UserState.DEAD }
        turnCounter += 1
        return (listOf("*----Turn ${turnCounter} results----*", results.joinToString("\n\n"))).joinToString("\n\n")
    }

    fun startGame() {
        if (playerList.size > 1) {
            gameStarted = true
            playerList.forEach {
                it.characterState = UserState.CHOOSING_ACTION
            }
        }
    }

}