package tech.stephenlowery.rpgbot.core.game

import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.game.impl.FightingDummyGame

object GameManager {
    
    private val games = mutableMapOf<Long, Game>()
    
    private val userToGameMap = mutableMapOf<Long, Long>()
    
    fun findGame(id: Long?): Game? = games[id]
    
    fun findGameContainingCharacter(playerId: Long): Game? = games[userToGameMap[playerId]]
    
    fun findCharacterInGame(gameId: Long, playerId: Long): PlayerCharacter? = findGame(gameId)?.findPlayerCharacterFromID(playerId)
    
    fun createGame(gameId: Long, initiatorId: Long, initiatorName: String) {
        games[gameId] = FightingDummyGame(gameId, initiatorId, initiatorName)
        userToGameMap[initiatorId] = gameId
    }
    
    fun addPlayerToGame(gameId: Long, playerId: Long, name: String) {
        val game = findGame(gameId)
        userToGameMap[playerId] = gameId
        game?.addPlayerToGame(playerId, name)
    }

    fun startGame(gameId: Long): Collection<Pair<Long, String>> {
        val game = findGame(gameId)!!
        return game.startGame()
    }
    
    fun cancelGame(chatId: Long): Boolean {
        val game = games.remove(chatId)
        game?.cancel()
        return game != null
    }
    
    fun chooseActionForCharacter(playerId: Long, actionName: String): ChooseActionResult {
        val game = findGame(userToGameMap[playerId])
        val character = game?.getHumanPlayers()?.get(playerId)
        if (character?.queuedAction != null) {
            throw RuntimeException("Player with id $playerId attempted to add an extra action.")
        }

        if (game == null || character == null) {
            throw RuntimeException("Game or character is null; cannot choose action")
        }

        val newCharacterState = game.queueActionFromCharacter(actionName, playerId)

        return ChooseActionResult(newCharacterState, character.queuedAction!!.getQueuedText(), character)
    }
    
    fun findCharacter(playerId: Long): PlayerCharacter? = findGameContainingCharacter(playerId)?.findPlayerCharacterFromID(playerId)

    fun resolveActionsForGame(gameId: Long): String {
        val game = games[gameId]!!
        return game.resolveActionsAndGetResults()
    }
}