package tech.stephenlowery.telegram.handlers.startgame

import com.github.kotlintelegrambot.entities.Message
import tech.stephenlowery.rpgbot.core.game.Game
import tech.stephenlowery.rpgbot.core.game.GameManager
import tech.stephenlowery.telegram.handlers.TelegramCommandHandler

object StartGameCommandHandler : TelegramCommandHandler<StartGameResult> {

    override fun execute(message: Message): StartGameResult {
        val userId = message.from?.id
        val game = GameManager.findGame(message.chat.id)
        return when {
            message.chat.type == "private"     -> PrivateChat
            userId == null                     -> UserIdNull
            game == null                       -> CreateGame
            !game.containsPlayerWithID(userId) -> UserNotInGame
            !game.numberOfPlayersIsValid()     -> IllegalNumberOfUsers
            userId != game.initiatorId         -> UserNotInitiator
            else                               -> startGame(game)
        }
    }

    private fun startGame(game: Game): GameStarting {
        val messages = game.startGame()
        return GameStarting(game.numberOfPlayers(), messages)
    }
}