package tech.stephenlowery.telegram.handlers.startgame

import tech.stephenlowery.telegram.handlers.TelegramCommandResult

sealed class StartGameResult(message: String) : TelegramCommandResult(message)

object PrivateChat : StartGameResult(
    """**RPGBot Pre-pre-pre-alpha**
Source code available on GitHub: https://github.com/sglowery/stephens-rpg-bot

/newgame -- Creates a new game for others to join.
/join -- Joins a game that hasn't started yet.
/stats -- Display your character's stats.
/start -- In a private chat, display this text. In a group chat, initiates a new game.
/calltoarms -- Call out and tag players who haven't finished picking their action for the round.
/cancelgame -- Cancels a game (will ask you to confirm).""".trimIndent()
)

object UserIdNull : StartGameResult("No anonymous admins allowed. Show yourself!")

object NoGameExists : StartGameResult("No one has started a game yet. Start one with /newgame.")

object UserNotInGame : StartGameResult("You need to join the game with /join.")

object IllegalNumberOfUsers : StartGameResult("Number of players in game is not valid for the game type.")

object UserNotInitiator : StartGameResult("Only the initiator can start the game.")

class GameStarting(numPlayers: Int, val messages: Collection<Pair<Long, String>>) : StartGameResult("Game is starting with $numPlayers player(s).")
