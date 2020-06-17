package tech.stephenlowery.rpgbot

import me.ivmg.telegram.Bot
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.callbackQuery
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.entities.*
import tech.stephenlowery.rpgbot.models.CharacterAction
import tech.stephenlowery.rpgbot.models.Game
import tech.stephenlowery.rpgbot.models.RPGCharacter
import tech.stephenlowery.rpgbot.models.UserState


class RPGBot(val telegramBotToken: String) {

    private val games = mutableMapOf<Long, Game>()

    fun MutableMap<Long, Game>.findGameWithPlayer(userID: Long): Game? {
        return this.values.find { game -> game.playerInGame(userID) }
    }

    private val characters = mutableMapOf<Long, RPGCharacter>()

    fun start() {
        val rpgBot = bot {
            token = telegramBotToken
            dispatch {
                command("newcharacter", ::newCharacterCommand)
                command("newgame", ::newGameCommand)
                command("join", ::joinGameCommand)
                command("start", ::startGameCommand)
                command("stats", ::characterStatsCommand)
                callbackQuery { bot: Bot, update: Update ->
                    val callbackType = update.callbackQuery!!.data.split("|")
                    when (callbackType[0]) {
                        "action" -> actionChosen(bot, update)
                        "target" -> targetChosen(bot, update)
                    }
                }
            }
        }
        rpgBot.startPolling()
    }

    private fun newCharacterCommand(bot: Bot, update: Update) {
        val message = update.message!!
        val chatID = message.chat.id
        val finalResponse: String
        val from = message.from!!
        val userID = from.id
        var replyToMessageId: Long? = null
        if (characters.containsKey(userID)) {
            finalResponse = "You already have a character. Deal with it"
        } else if (message.chat.type == "private") {
            val newCharacter = RPGCharacter(userID, from.firstName)
            characters[userID] = newCharacter
            finalResponse = "Here's your new character, take it or leave it lol:\n${newCharacter}"
        } else {
            finalResponse = "Let's take this somewhere more private, sweatie (;"
            replyToMessageId = message.messageId
        }
        bot.sendMessage(chatID, finalResponse, replyToMessageId = replyToMessageId)
    }

    private fun newGameCommand(bot: Bot, update: Update) {
        val message = update.message!!
        val chatID = message.chat.id
        val userID = message.from!!.id
        val game = games[chatID]
        val userCharacter = characters[userID]
        val response: String
        var replyToMessageId: Long? = null
        if (message.chat.type == "private") {
            response = "You can't start a game in here silly goose"
        } else if (game != null) {
            response = if (game.gameStarted) "The game's already started. You're too late" else "There's already a game created. Fucking chill"
        } else if (userCharacter == null) {
            replyToMessageId = message.messageId
            response = "You need to make a character first. Talk to me in a private chat and use /newcharacter"
        } else {
            val newGame = Game(chatID, userCharacter)
            games[chatID] = newGame
            response = "Game created. /join to join, /start to start"
        }
        bot.sendMessage(chatID, response, replyToMessageId = replyToMessageId)
    }

    private fun joinGameCommand(bot: Bot, update: Update) {
        val message = update.message!!
        val chatID = message.chat.id
        val userID = message.from!!.id
        val replyToMessageId = message.messageId
        val game = games[chatID]
        if (message.chat.type == "private") {
            bot.sendMessage(chatID, "Ain't no game in here, kid", replyToMessageId = replyToMessageId)
        } else if (!characters.containsKey(userID)) {
            bot.sendMessage(chatID,
                "You need to make a character before you can join a game.\n\n" +
                        "Open a private chat with me and type /newcharacter",
                replyToMessageId = replyToMessageId
            )
        } else if (game == null) {
            bot.sendMessage(chatID, "There isn't a game started. Type /newgame to make one", replyToMessageId = replyToMessageId)
        } else if (game.playerInGame(userID)) {
            bot.sendMessage(chatID, "You're already in the game, silly", replyToMessageId = message.messageId)
        } else if (game.gameStarted) {
            bot.sendMessage(chatID, "Game's already started. Too late bub", replyToMessageId = replyToMessageId)
        } else {
            val userCharacter = characters[userID]!!
            userCharacter.characterState = UserState.IN_LOBBY
            game.playerList.add(userCharacter)
            val numPlayersInGame = game.playerList.size
            bot.sendMessage(chatID, "${userCharacter.name} has joined! There are now $numPlayersInGame players in the game.")
        }
    }

    private fun startGameCommand(bot: Bot, update: Update) {
        val message = update.message!!
        val userID = message.from!!.id
        val chatID = message.chat.id
        val game: Game? = games[chatID]
        val messageId = message.messageId
        if (message.chat.type == "private") {
            bot.sendMessage(chatID, "You can't start a game in here. Really. You can't")
        } else if (!characters.containsKey(userID)) {
            bot.sendMessage(chatID,
                "You need to make a character first. Talk to me in a private chat and use /newcharacter",
                replyToMessageId = messageId
            )
        } else if (game == null) {
            bot.sendMessage(chatID,
                "No one has started a game yet. Start one with /newgame",
                replyToMessageId = messageId
            )
        } else if (!game.playerInGame(userID)) {
            bot.sendMessage(chatID,
                "You need to join the game with /join",
                replyToMessageId = messageId
            )
        } else if (game.playerList.size == 1) {
            bot.sendMessage(chatID,
                "Do you really want to play with yourself in front of all your friends? Didn't think so. Unless...?",
                replyToMessageId = messageId
            )
        } else {
            bot.sendMessage(chatID, "Game is starting with ${game.playerList.size} player(s)")
            game.startGame()
            sendPlayersInGameActions(bot, chatID)
        }
    }

    private fun characterStatsCommand(bot: Bot, update: Update) {
        val message = update.message!!
        bot.sendMessage(
            chatId = message.chat.id,
            replyToMessageId = message.messageId,
            text = characters.get(message.from!!.id)?.getCharacterStatusText() ?: "You don't have a character, silly goose. Open a private chat with me and use the /newcharacter command"
        )
    }

    private fun sendPlayersInGameActions(bot: Bot, chatID: Long) {
        games[chatID]?.livingPlayers()?.forEach {
            val keyboard = makeKeyboardFromPlayerActions(it.getAvailableActions())
            val replyMarkup = InlineKeyboardMarkup(keyboard)
            val characterStatus = it.getCharacterStatusText()
            bot.sendMessage(it.userID, characterStatus + "\n\nPick an action", replyMarkup = replyMarkup)
        }
    }

    private fun actionChosen(bot: Bot, update: Update) {
        val callbackQuery = update.callbackQuery!!
        val userID = callbackQuery.from.id
        val userCharacter = characters[userID]
        if (userCharacter != null) {
            if (userCharacter.characterState == UserState.CHOOSING_ACTION) {
                val game = games.findGameWithPlayer(userCharacter.userID)!!
                val callbackData = callbackQuery.data
                val newCharacterState = game.queueActionFromCharacter(callbackData, userID)
                if (newCharacterState == UserState.WAITING) {
                    bot.editMessageText(userID,
                        callbackQuery.message!!.messageId,
                        null,
                        userCharacter.queuedAction!!.getQueuedText()
                    )
                } else if (newCharacterState == UserState.CHOOSING_TARGETS) {
                    sendTargetsToPlayer(bot, game, userCharacter, callbackQuery.message!!.messageId)
                }

                if (game.allPlayersWaiting()) {
                    resolveActionsInGame(bot, game)
                }
            } else {
                bot.sendMessage(userID, "Can you stop trying to break the game? Just press the button once. Thanks", replyMarkup = ReplyKeyboardRemove())
            }
        }
    }

    private fun sendTargetsToPlayer(bot: Bot, game: Game, character: RPGCharacter, previousMessageID: Long) {
        bot.editMessageText(character.userID,
            previousMessageID,
            null,
            "Choose a target",
            replyMarkup = InlineKeyboardMarkup(makeKeyboardFromPlayerNames(game.playerList.filter { it.userID != character.userID }))
        )
    }

    private fun targetChosen(bot: Bot, update: Update) {
        val callbackQuery = update.callbackQuery!!
        val userID = callbackQuery.from.id
        val game = games.findGameWithPlayer(userID)
        val fromCharacter = characters[userID]
        if (game != null && fromCharacter != null) {
            if (fromCharacter.characterState == UserState.CHOOSING_TARGETS) {
                val target = callbackQuery.data.split("|")[1].toLong()
                val resolvedCharacterState = game.addTargetToQueuedCharacterAction(userID, target)
                if (resolvedCharacterState == UserState.WAITING) {
                    bot.editMessageText(userID,
                        callbackQuery.message!!.messageId,
                        null,
                        "When everyone is done, you'll use ${fromCharacter.queuedAction!!.action.displayName} " +
                                "on ${fromCharacter.queuedAction!!.targets.map { it.name }.joinToString(", ")}"
                    )
                } else {
                    sendTargetsToPlayer(bot, game, characters[userID]!!, callbackQuery.message!!.messageId)
                }

                if (game.allPlayersWaiting()) {
                    resolveActionsInGame(bot, game)
                }
            } else {
                bot.sendMessage(fromCharacter.userID, "Yes, you're very clever trying to mash the button. Stop.")
            }
        }
    }

    private fun resolveActionsInGame(bot: Bot, game: Game) {
        val resolvedActionsText = game.resolveActions()
        game.playerList.forEach { player ->
            bot.sendMessage(player.userID, resolvedActionsText, replyMarkup = ReplyKeyboardRemove(), parseMode = ParseMode.MARKDOWN)
            if (!player.isAlive()) {
                bot.sendMessage(player.userID, "You died in the previous round and have been removed from the game. You must wait for it to finish to join")
            }
        }
        val livingPlayers = game.livingPlayers()
        if (livingPlayers.size == 1) {
            bot.sendMessage(game.id, "${livingPlayers.first().name} wins!")
            games.remove(game.id)
        } else if (livingPlayers.size > 1) {
            sendPlayersInGameActions(bot, game.id)
        } else {
            bot.sendMessage(game.id, "Uh, well, I guess the remaining people died at the same time or something. Ok")
            games.remove(game.id)
        }
    }

    private fun makeKeyboardFromPlayerActions(actions: List<CharacterAction>): List<List<InlineKeyboardButton>> {
        return actions.map { InlineKeyboardButton(it.displayName, callbackData = it.callbackText) }.chunked(2)
    }

    private fun makeKeyboardFromPlayerNames(characters: List<RPGCharacter>): List<List<InlineKeyboardButton>> {
        return characters.map { InlineKeyboardButton(it.getNameAndHealthPercentLabel(), callbackData = "target|${it.userID}") }.chunked(2)
    }
}