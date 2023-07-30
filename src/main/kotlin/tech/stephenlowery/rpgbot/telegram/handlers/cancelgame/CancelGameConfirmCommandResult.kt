package tech.stephenlowery.rpgbot.telegram.handlers.cancelgame

import tech.stephenlowery.rpgbot.telegram.handlers.TelegramCommandResult

sealed class CancelGameConfirmCommandResult(message: String) : TelegramCommandResult(message)

object PrivateChat : CancelGameConfirmCommandResult("You can't start games here, why are you trying to cancel one?")

object UserIdNull : CancelGameConfirmCommandResult("Nice try, anonymous admin.")

object NoGameExists : CancelGameConfirmCommandResult("There isn't a game running in this chat.")

object UserNotInGame : CancelGameConfirmCommandResult("You're not even in the game.")

object UserNotInitiator : CancelGameConfirmCommandResult("Only the initiator can cancel the game.")

object GameCanBeCanceled : CancelGameConfirmCommandResult("Are you sure you want to cancel the game?")