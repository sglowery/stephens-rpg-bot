package tech.stephenlowery.telegram.handlers.cancelgame

import tech.stephenlowery.telegram.handlers.TelegramCommandResult

sealed class CancelGameConfirmCommandResult(message: String) : TelegramCommandResult(message)

data object PrivateChat : CancelGameConfirmCommandResult("You can't start games here, why are you trying to cancel one?")

data object UserIdNull : CancelGameConfirmCommandResult("Nice try, anonymous admin.")

data object NoGameExists : CancelGameConfirmCommandResult("There isn't a game running in this chat.")

data object UserNotInGame : CancelGameConfirmCommandResult("You're not even in the game.")

data object UserNotInitiator : CancelGameConfirmCommandResult("Only the initiator can cancel the game.")

data object GameCanBeCanceled : CancelGameConfirmCommandResult("Are you sure you want to cancel the game?")