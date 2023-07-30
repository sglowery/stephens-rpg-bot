package tech.stephenlowery.rpgbot.telegram.handlers.newgame

import com.github.kotlintelegrambot.entities.Message
import tech.stephenlowery.rpgbot.core.game.GameManager
import tech.stephenlowery.rpgbot.telegram.handlers.TelegramCommandHandler

object NewGameCommandHandler : TelegramCommandHandler<NewGameCommandResult> {

    override fun execute(message: Message): NewGameCommandResult {
        val chatId = message.chat.id
        val userId = message.from?.id
        val game = GameManager.findGame(chatId)
        val name = message.from?.firstName
        return when {
            message.chat.type == "private" -> PrivateChat
            userId == null                 -> UserIdNull
            game == null                   -> createGame(chatId, userId, name!!)
            game.hasStarted                -> GameAlreadyStarted
            else                           -> GameAlreadyExists
        }
    }

    private fun createGame(chatId: Long, userId: Long, name: String): GameCreated {
        GameManager.createGame(chatId, userId, name)
        return GameCreated
    }
}