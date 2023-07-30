package tech.stephenlowery.telegram.handlers.cancelgame

import com.github.kotlintelegrambot.entities.Message
import tech.stephenlowery.rpgbot.core.game.GameManager
import tech.stephenlowery.telegram.handlers.TelegramCommandHandler

object CancelGameConfirmCommandHandler : TelegramCommandHandler<CancelGameConfirmCommandResult> {
    
    override fun execute(message: Message): CancelGameConfirmCommandResult {
        val userId = message.from?.id
        val chatId = message.chat.id
        val game = GameManager.findGame(chatId)
        return when {
            message.chat.type == "private"     -> PrivateChat
            userId == null                     -> UserIdNull
            game == null                       -> NoGameExists
            !game.containsPlayerWithID(userId) -> UserNotInGame
            userId != game.initiatorId         -> UserNotInitiator
            else                               -> GameCanBeCanceled
        }
    }

    fun getGameCanceledMessage(name: String) = "$name canceled the game."
}