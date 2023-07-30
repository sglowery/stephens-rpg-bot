package tech.stephenlowery.telegram.handlers.joingame

import com.github.kotlintelegrambot.entities.Message
import tech.stephenlowery.rpgbot.core.game.Game
import tech.stephenlowery.rpgbot.core.game.GameManager
import tech.stephenlowery.telegram.handlers.TelegramCommandHandler

object JoinGameCommandHandler : TelegramCommandHandler<JoinGameCommandResult> {

    override fun execute(message: Message): JoinGameCommandResult {
        val userId = message.from?.id
        val chatId = message.chat.id
        val game = GameManager.findGame(chatId)
        return when {
            message.chat.type == "private"    -> PrivateChat
            userId == null                    -> UserIdNull
            game == null                      -> NoGameExists
            game.containsPlayerWithID(userId) -> PlayerAlreadyInGame
            game.hasStarted                   -> GameAlreadyStarted
            else                              -> addPlayerToGame(game, userId, message)
        }
    }

    private fun addPlayerToGame(game: Game, userId: Long, message: Message): JoinedGame {
        val name = message.from!!.firstName
        GameManager.addPlayerToGame(game.id, userId, name)
        return JoinedGame(name, game.numberOfPlayers())
    }
}