package tech.stephenlowery.telegram.handlers.startgame

import tech.stephenlowery.telegram.handlers.TelegramCommandResult

sealed class StartGameResult(message: String) : TelegramCommandResult(message)

data object PrivateChat : StartGameResult(
    """**RPGBot Pre-pre-pre-alpha**
Source code available on GitHub: https://github.com/sglowery/stephens-rpg-bot

/newgame -- Creates a new game for others to join.
/join -- Joins a game that hasn't started yet.
/stats -- Display your character's stats.
/start -- In a private chat, display this text. In a group chat, creates a new game or starts a pending one.
/calltoarms -- Call out and tag players who haven't finished picking their action for the round.
/cancelgame -- Cancels a game (will ask you to confirm).""".trimIndent()
)

data object UserIdNull : StartGameResult("No anonymous admins allowed. Show yourself!")

data object CreateGame : StartGameResult("")

data object UserNotInGame : StartGameResult("You need to join the game with /join.")

data object IllegalNumberOfUsers : StartGameResult("Number of players in game is not valid for the game type.")

data object UserNotInitiator : StartGameResult("Only the initiator can start the game.")

class GameStarting(numPlayers: Int, val messages: Collection<Pair<Long, String>>) : StartGameResult("Game is starting with $numPlayers player(s).")
