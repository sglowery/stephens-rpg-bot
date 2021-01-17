package tech.stephenlowery.rpgbot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.*
import tech.stephenlowery.rpgbot.models.Game
import tech.stephenlowery.rpgbot.models.action.CharacterAction
import tech.stephenlowery.rpgbot.models.character.RPGCharacter
import tech.stephenlowery.rpgbot.models.character.UserState


class RPGBot(val telegramBotToken: String) {

    private val games = mutableMapOf<Long, Game>()

    private fun MutableMap<Long, Game>.findGameWithPlayer(userID: Long): Game? {
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
                command("calltoarms", ::waitingOnCommand)
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
        val from = message.from!!
        val userID = from.id
        var replyToMessageId: Long? = null
        val newCharacter = RPGCharacter(userID, from.firstName)
        val characterCreatedResponse = "Here's your new character, take it or leave it lol:\n${newCharacter.getCharacterSummaryText()}"

        val finalResponse: String
        if (characterFromUserExists(userID)) {
            finalResponse = "You already have a character. Deal with it."
        } else if (message.chat.type == "private") {
            characters[userID] = newCharacter
            finalResponse = characterCreatedResponse
        } else {
            val response = bot.sendMessage(chatID, characterCreatedResponse)
            replyToMessageId = message.messageId
            if (response.first?.isSuccessful == true) {
                characters[userID] = newCharacter
                finalResponse = "Your character has been made -- see our private chat for details."
            } else {
                finalResponse = "You need to open a private chat with me first before using /newcharacter in this group."
            }
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
            response = "You can't start a game in here silly goose!"
        } else if (game != null) {
            response = if (game.gameStarted) "The game's already started. You're too late." else "There's already a game created. Chill."
        } else if (userCharacter == null) {
            replyToMessageId = message.messageId
            response = "You need to make a character first. Talk to me in a private chat and use /newcharacter."
        } else {
            val newGame = Game(chatID, userCharacter)
            games[chatID] = newGame
            response = "Game created. /join to join, /start to start."
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
            bot.sendMessage(chatID, "Ain't no game in here, kid.", replyToMessageId = replyToMessageId)
        } else if (!characterFromUserExists(userID)) {
            bot.sendMessage(
                chatID,
                "You need to make a character before you can join a game.\n\n" +
                        "Open a private chat with me and type /newcharacter.",
                replyToMessageId = replyToMessageId
            )
        } else if (game == null) {
            bot.sendMessage(chatID, "There isn't a game started. Type /newgame to make one.", replyToMessageId = replyToMessageId)
        } else if (game.playerInGame(userID)) {
            bot.sendMessage(chatID, "You're already in the game, silly.", replyToMessageId = message.messageId)
        } else if (game.gameStarted) {
            bot.sendMessage(chatID, "Game's already started. Too late bub.", replyToMessageId = replyToMessageId)
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
            bot.sendMessage(
                chatID, """
                **RPGBot Pre-pre-pre-alpha**
                Source code available on GitHub: https://github.com/sglowery/stephens-rpg-bot
                
                /newcharacter -- Generates a new character, only usable in a private chat
                /newgame -- Creates a new game for others to join
                /join -- Joins a game that hasn't started yet
                /stats -- Display your character's stats
                /start -- In a private chat, display this text. In a group chat, initiates a game as long as it has at least two players
            """.trimIndent(), parseMode = ParseMode.MARKDOWN
            )
        } else if (!characterFromUserExists(userID)) {
            bot.sendMessage(
                chatID,
                "You need to make a character first. Talk to me in a private chat and use /newcharacter.",
                replyToMessageId = messageId
            )
        } else if (game == null) {
            bot.sendMessage(
                chatID,
                "No one has started a game yet. Start one with /newgame.",
                replyToMessageId = messageId
            )
        } else if (!game.playerInGame(userID)) {
            bot.sendMessage(
                chatID,
                "You need to join the game with /join.",
                replyToMessageId = messageId
            )
        } else if (game.playerList.size == 1) {
            bot.sendMessage(
                chatID,
                "Do you really want to play with yourself in front of all your friends? Didn't think so. Unless...?",
                replyToMessageId = messageId
            )
        } else {
            bot.sendMessage(chatID, "Game is starting with ${game.playerList.size} player(s).")
            game.startGame()
            sendPlayersInGameActions(bot, chatID)
        }
    }

    private fun characterFromUserExists(userID: Long): Boolean = characters.containsKey(userID)

    private fun characterStatsCommand(bot: Bot, update: Update) {
        val message = update.message!!
        bot.sendMessage(
            chatId = message.chat.id,
            replyToMessageId = message.messageId,
            text = characters[message.from!!.id]?.getCharacterStatusText() ?: "You don't have a character, silly goose. Open a private chat with me and use the /newcharacter command."
        )
    }

    private fun waitingOnCommand(bot: Bot, update: Update) {
        val chatID = update.message!!.chat.id
        val game = games[chatID]
        if (update.message!!.chat.type == "group" && game != null) {
            val waitingOn = game.waitingOn()
            if (waitingOn.isNotEmpty()) {
                val names = waitingOn.joinToString(", ") { "[${it.name}](tg://user?id=${it.userID})" }
                bot.sendMessage(chatID, "Waiting on the following player(s): $names.", parseMode = ParseMode.MARKDOWN)
            }
        }
    }

    private fun sendPlayersInGameActions(bot: Bot, chatID: Long) {
        games[chatID]?.livingPlayers()?.forEach {
            val keyboard = makeKeyboardFromPlayerActions(it.getAvailableActions())
            val replyMarkup = InlineKeyboardMarkup(keyboard)
            bot.sendMessage(it.userID, it.getPreActionText() + "\n\nPick an action.", replyMarkup = replyMarkup)
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
                    bot.editMessageText(
                        userID,
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
                bot.sendMessage(userID, "Can you stop trying to break the game? Just press the button once. Thanks.", replyMarkup = ReplyKeyboardRemove())
            }
        } else {
            bot.deleteMessage(
                userID,
                callbackQuery.message!!.messageId
            )
        }
    }

    private fun sendTargetsToPlayer(bot: Bot, game: Game, character: RPGCharacter, previousMessageID: Long) {
        bot.editMessageText(
            character.userID,
            previousMessageID,
            null,
            character.getPreActionText() + "\n\nChoose a target.",
            replyMarkup = InlineKeyboardMarkup(makeKeyboardFromPlayerNames(game.livingPlayers().filter { it.userID != character.userID }))
        )
    }

    private fun targetChosen(bot: Bot, update: Update) {
        val callbackQuery = update.callbackQuery!!
        val userID = callbackQuery.from.id
        val game = games.findGameWithPlayer(userID)
        val fromCharacter = characters[userID]
        if (game != null && fromCharacter != null && fromCharacter.characterState == UserState.CHOOSING_TARGETS) {
            val target = callbackQuery.data.split("|")[1].toLong()
            game.addTargetToQueuedCharacterAction(userID, target)
            bot.editMessageText(
                userID,
                callbackQuery.message!!.messageId,
                null,
                fromCharacter.queuedAction!!.getQueuedText()
            )
            if (game.allPlayersWaiting()) {
                resolveActionsInGame(bot, game)
            }
        }
    }

    private fun resolveActionsInGame(bot: Bot, game: Game) {
        val resolvedActionsText = game.resolveActions()
        bot.sendMessage(game.id, resolvedActionsText, replyMarkup = ReplyKeyboardRemove(), parseMode = ParseMode.MARKDOWN)
        game.deadPlayers().forEach { player ->
            bot.sendMessage(player.userID, "You died in the previous round and have been removed from the game.")
        }
        game.playerList.removeAll(game.deadPlayers())
        val livingPlayers = game.livingPlayers()
        if (livingPlayers.size > 1) {
            sendPlayersInGameActions(bot, game.id)
        } else {
            bot.sendMessage(
                game.id,
                game.getGameEndedText()
            )
            characters.minusAssign(game.playerList.map(RPGCharacter::userID))
            games.remove(game.id)
        }
    }

    private fun makeKeyboardFromPlayerNames(characters: List<RPGCharacter>): List<List<InlineKeyboardButton>> {
        return characters.map { InlineKeyboardButton(it.getNameAndHealthPercentLabel(), callbackData = "target|${it.userID}") }.chunked(2)
    }

    private fun makeKeyboardFromPlayerActions(actions: List<CharacterAction>): List<List<InlineKeyboardButton>> {
        return actions.map { InlineKeyboardButton(it.displayName, callbackData = it.callbackText) }.chunked(2)
    }
}