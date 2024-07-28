package tech.stephenlowery.telegram.handlers.joingame

import tech.stephenlowery.telegram.handlers.TelegramCommandResult

sealed class JoinGameCommandResult(message: String) : TelegramCommandResult(message)

data object PrivateChat : JoinGameCommandResult("Ain't no game in here, kid.")

data object UserIdNull : JoinGameCommandResult("Get out of here, anonymous admin.")

data object NoGameExists : JoinGameCommandResult("There isn't a game started. Type /newgame to make one.")

data object PlayerAlreadyInGame : JoinGameCommandResult("You're already in the game, silly.")

data object GameAlreadyStarted : JoinGameCommandResult("Game's already started. Too late bub.")

class JoinedGame(name: String, numPlayersInGame: Int) : JoinGameCommandResult("$name has joined! There are now $numPlayersInGame players in the game.")