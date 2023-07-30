package tech.stephenlowery.telegram.handlers.newgame

import tech.stephenlowery.telegram.handlers.TelegramCommandResult

sealed class NewGameCommandResult(message: String) : TelegramCommandResult(message)

object PrivateChat : NewGameCommandResult("You can't start a game in here silly goose!")

object UserIdNull : NewGameCommandResult("No anonymous admins allowed")

object GameAlreadyExists : NewGameCommandResult("There's already a game created. Chill.")

object GameAlreadyStarted : NewGameCommandResult("The game's already started. You're too late.")

object GameCreated : NewGameCommandResult("Game created. /join to join, /start to start.")
