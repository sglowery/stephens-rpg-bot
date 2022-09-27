package tech.stephenlowery.rpgbot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import kotlinx.coroutines.*
import tech.stephenlowery.rpgbot.core.game.Game
import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.game.FightingDummyGame
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.UserState
import tech.stephenlowery.util.telegram.deleteMessage
import tech.stephenlowery.util.telegram.editMessageText
import tech.stephenlowery.util.telegram.sendMessage


class RPGBot(private val telegramBotToken: String) {

    private val games = mutableMapOf<Long, Game>()

    private val characters = mutableMapOf<Long, PlayerCharacter>()

    fun start() {
        val rpgBot = bot {
            token = telegramBotToken
            dispatch {
                command("newcharacter") { newCharacterCommand(bot, update, message) }
                command("newgame") { newGameCommand(bot, update, message) }
                command("join") { joinGameCommand(bot, update, message) }
                command("start") { startGameCommand(bot, update, message) }
                command("stats") { characterStatsCommand(bot, update, message) }
                command("calltoarms") { waitingOnCommand(bot, update, message) }
                command("cancelgame") { cancelGameCommandConfirm(bot, update, message) }
                callbackQuery {
                    val callbackDataSplit = update.callbackQuery!!.data.split("|")
                    when (callbackDataSplit[0]) {
                        "action" -> actionChosen(bot, update)
                        "target" -> targetChosen(bot, update)
                        "cancel" -> cancelGameChoiceHandler(callbackDataSplit.subList(1, callbackDataSplit.size), bot, update)
                    }
                }
            }
        }
        println("bot running")
        rpgBot.startPolling()
    }

    private fun newCharacterCommand(bot: Bot, update: Update, message: Message) {
        val from = message.from!!
        val userID = from.id
        var replyToMessageId: Long? = null
        val newCharacter = PlayerCharacter(userID, from.firstName)
        val characterCreatedResponse = "Here's your new character, take it or leave it lol:\n${newCharacter.getCharacterSummaryText()}"
        val chatID = message.chat.id
        val response: String
        if (characterFromUserExists(userID)) {
            response = "You already have a character. Deal with it."
        } else {
            replyToMessageId = message.messageId
            bot.sendMessage(userID, characterCreatedResponse).run {
                response = when (this.first?.isSuccessful) {
                    true -> {
                        characters[userID] = newCharacter
                        when (message.chat.type) {
                            "private" -> "Your character has been made"
                            else      -> "Your character has been made -- see our private chat for details."
                        }
                    }
                    else -> "You need to open a private chat with me first before using /newcharacter in this group."
                }
            }
        }
        bot.sendMessage(chatID, response, replyToMessageId = replyToMessageId)
    }

    private fun newGameCommand(bot: Bot, update: Update, message: Message) {
        val chatID = message.chat.id
        val userID = message.from!!.id
        val game = games[chatID]
        val userCharacter = characters[userID]
        var replyToMessageId: Long? = null
        val response = when {
            message.chat.type == "private" -> "You can't start a game in here silly goose!"
            game != null                   -> if (game.gameStarted) "The game's already started. You're too late." else "There's already a game created. Chill."
            userCharacter == null          -> "You need to make a character first. Use the /newcharacter command.".also { replyToMessageId = message.messageId }
            else                           -> "Game created. /join to join, /start to start.".also { createNewGameAndAddToGamesMap(chatID, userCharacter) }
        }
        bot.sendMessage(chatID, response, replyToMessageId = replyToMessageId)
    }

    private fun createNewGameAndAddToGamesMap(chatID: Long, initiator: PlayerCharacter) = games.apply { put(chatID, FightingDummyGame(chatID, initiator)) }

    private fun joinGameCommand(bot: Bot, update: Update, message: Message) {
        val chatID = message.chat.id
        val userID = message.from!!.id
        val game = games[chatID]
        var replyToMessageId: Long? = message.messageId
        val response = when {
            message.chat.type == "private"   -> "Ain't no game in here, kid."
            !characterFromUserExists(userID) -> "You need to make a character before you can join a game. Use the /newcharacter command."
            game == null                     -> "There isn't a game started. Type /newgame to make one."
            game.containsPlayerWithID(userID) -> "You're already in the game, silly."
            game.gameStarted                 -> "Game's already started. Too late bub."
            else                             -> addCharacterToGameAndGetReplyText(userID, game).also { replyToMessageId = null }
        }
        bot.sendMessage(chatID, response, replyToMessageId = replyToMessageId)
    }

    private fun addCharacterToGameAndGetReplyText(userID: Long, game: Game): String {
        val userCharacter = characters[userID]!!
        userCharacter.characterState = UserState.IN_LOBBY
        game.addPlayer(userCharacter)
        val numPlayersInGame = game.playerList.size
        return "${userCharacter.name} has joined! There are now $numPlayersInGame players in the game."
    }

    private fun startGameCommand(bot: Bot, update: Update, message: Message) {
        val userID = message.from!!.id
        val chatID = message.chat.id
        val game: Game? = games[chatID]
        var replyToMessageID: Long? = message.messageId
        var parseMode: ParseMode? = null
        val response: String
        if (message.chat.type == "private") {
            response = """
                **RPGBot Pre-pre-pre-alpha**
                Source code available on GitHub: https://github.com/sglowery/stephens-rpg-bot
                
                /newcharacter -- Generates a new character
                /newgame -- Creates a new game for others to join
                /join -- Joins a game that hasn't started yet
                /stats -- Display your character's stats
                /start -- In a private chat, display this text. In a group chat, initiates a game as long as it has at least two players
                /calltoarms -- Call out and tag players who haven't finished picking their action for the round
                /cancelgame -- Cancels a game (will ask you to confirm)
            """.trimIndent()
            parseMode = ParseMode.MARKDOWN
            replyToMessageID = null
        } else if (!characterFromUserExists(userID)) {
            response = "You need to make a character first. Talk to me in a private chat and use /newcharacter."
        } else if (game == null) {
            response = "No one has started a game yet. Start one with /newgame."
        } else if (!game.containsPlayerWithID(userID)) {
            response = "You need to join the game with /join."
        } else if (game.playerList.size == 1) {
            response = "Do you really want to play with yourself in front of all your friends? Didn't think so. Unless...?"
        } else {
            response = "Game is starting with ${game.playerList.size} player(s)."
            game.startGame()
        }
        bot.sendMessage(chatID, response, parseMode = parseMode, replyToMessageId = replyToMessageID)
        if (game?.gameStarted == true) {
            sendPlayersInGameActions(bot, chatID)
        }
    }

    private fun characterFromUserExists(userID: Long): Boolean = characters.containsKey(userID)

    private fun characterStatsCommand(bot: Bot, update: Update, message: Message) {
        bot.sendMessage(
            chatId = message.chat.id,
            replyToMessageId = message.messageId,
            text = characters[message.from!!.id]?.getCharacterStatusText() ?: "You don't have a character, silly goose. Open a private chat with me and use the /newcharacter command."
        )
    }

    private fun waitingOnCommand(bot: Bot, update: Update, message: Message) {
        val chatID = message.chat.id
        val game = games[chatID]
        if (message.chat.type == "group" && game != null) {
            val waitingOn = game.waitingOn()
            if (waitingOn.isNotEmpty()) {
                val names = waitingOn.joinToString(", ") { "[${it.name}](tg://user?id=${it.id})" }
                bot.sendMessage(chatID, "Waiting on the following player(s): $names.", parseMode = ParseMode.MARKDOWN)
            }
        }
    }

    private fun cancelGameCommandConfirm(bot: Bot, update: Update, message: Message) {
        val chatID = message.chat.id
        val userID = message.from!!.id
        val choices = listOf(InlineKeyboardButton.CallbackData("Yes", callbackData = "cancel|yes"), InlineKeyboardButton.CallbackData("No", "cancel|no"))
        val markup = InlineKeyboardMarkup.create(listOf(choices))
        val game = games[chatID]
        if (game == null) {
            bot.sendMessage(chatID, "There isn't a game running in this chat, yo.", replyToMessageId = userID)
        } else if (userID != game.initiator.id) {
            bot.sendMessage(chatID, "Only the initiator of the game can cancel it.")
        } else {
            bot.sendMessage(chatID, "Are you sure you want to cancel the game?", replyToMessageId = userID, replyMarkup = markup)
        }
    }

    private fun cancelGameChoiceHandler(callbackDataSplit: List<String>, bot: Bot, update: Update) = when (callbackDataSplit[1].lowercase()) {
        "yes" -> ::cancelGameYes
        else  -> ::cancelGameNo
    }.also { it(bot, update.message!!.messageId) }

    private fun cancelGameYes(bot: Bot, chatID: Long) {
        games[chatID]?.cancel()
        games.remove(chatID)
        bot.sendMessage(chatID, "Game cancelled. Hope you're happy. Your characters are fine.")
    }

    private fun cancelGameNo(bot: Bot, chatID: Long) {
        bot.sendMessage(chatID, "K")
    }

    private fun sendPlayersInGameActions(bot: Bot, chatID: Long) = runBlocking {
        val game = games[chatID]!!
        launch {
            delay((1000.0 * (game.playerList.size.toDouble())).toLong())
            game.livingPlayers().filterIsInstance<PlayerCharacter>().forEach {
                val keyboard = makeKeyboardFromPlayerActions(it.getAvailableActions())
                val replyMarkup = InlineKeyboardMarkup.create(keyboard)
                bot.sendMessage(it.id, it.getPreActionText() + "\n\nPick an action.", replyMarkup = replyMarkup)
            }
        }
    }

    private fun actionChosen(bot: Bot, update: Update) {
        val callbackQuery = update.callbackQuery!!
        val userID = callbackQuery.from.id
        val userCharacter = characters[userID]
        if (userCharacter != null) {
            if (userCharacter.characterState == UserState.CHOOSING_ACTION) {
                val game = games.findGameWithPlayer(userCharacter.id)!!
                val callbackData = callbackQuery.data
                val newCharacterState = game.queueActionFromCharacter(callbackData, userID)
                if (newCharacterState == UserState.WAITING) {
                    bot.editMessageText(
                        chatId = userID,
                        messageId = callbackQuery.message!!.messageId,
                        inlineMessageId = null,
                        text = userCharacter.queuedAction!!.getQueuedText()
                    )
                } else if (newCharacterState == UserState.CHOOSING_TARGETS) {
                    sendTargetsToPlayer(bot, game, userCharacter, callbackQuery.message!!.messageId)
                }

                if (game.allPlayersAreWaiting()) {
                    resolveActionsInGame(bot, game)
                }
            } else {
                bot.sendMessage(userID, "Can you stop trying to break the game? Just press the button once. Thanks.")
                bot.editMessageReplyMarkup(ChatId.fromId(userID), update.message!!.messageId, replyMarkup = ReplyKeyboardRemove())
            }
        } else {
            bot.deleteMessage(userID, callbackQuery.message!!.messageId)
        }
    }

    private fun sendTargetsToPlayer(bot: Bot, game: Game, character: PlayerCharacter, previousMessageID: Long) {
        bot.editMessageText(
            chatId = character.id,
            messageId = previousMessageID,
            inlineMessageId = null,
            text = character.getPreActionText() + "\n\nChoose a target.",
            replyMarkup = InlineKeyboardMarkup.create(makeKeyboardFromPlayerNamesExcludingSelf(game.livingPlayers().filter { it.id != character.id }))
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
            bot.editMessageText(chatId = userID, messageId = callbackQuery.message!!.messageId, inlineMessageId = null, text = fromCharacter.queuedAction!!.getQueuedText())
            if (game.allPlayersAreWaiting()) {
                resolveActionsInGame(bot, game)
            }
        }
    }

    private fun resolveActionsInGame(bot: Bot, game: Game) {
        val resolvedActionsText = game.resolveActionsAndGetResults()
        bot.sendMessage(game.id, resolvedActionsText, replyMarkup = ReplyKeyboardRemove(), parseMode = ParseMode.MARKDOWN)
        val deadPlayers = game.humanCharacters().dead()
        deadPlayers.forEach { player ->
            bot.sendMessage(player.id, "You died in the previous round and have been removed from the game.")
        }
        game.removePlayers(deadPlayers)
        val livingPlayers = game.livingPlayers()
        if (livingPlayers.size > 1) {
            sendPlayersInGameActions(bot, game.id)
        } else {
            bot.sendMessage(game.id, game.getGameEndedText())
            characters -= deadPlayers.map { it.id }
            games.remove(game.id)
        }
    }

    private fun Map<Long, Game>.findGameWithPlayer(userID: Long): Game? {
        return this.values.find { game -> game.containsPlayerWithID(userID) }
    }

    private fun makeKeyboardFromPlayerNamesExcludingSelf(characters: List<RPGCharacter>): List<List<InlineKeyboardButton>> {
        return characters.map { InlineKeyboardButton.CallbackData(text = it.getNameAndHealthPercentLabel(), callbackData = "target|${it.id}") }.chunked(2)
    }

    private fun makeKeyboardFromPlayerActions(actions: List<CharacterAction>): List<List<InlineKeyboardButton>> {
        return actions.map { InlineKeyboardButton.CallbackData(text = it.displayName, callbackData = it.identifier) }.chunked(2)
    }

    private fun Game.humanCharacters() = this.playerList.filterIsInstance<PlayerCharacter>()

    private fun <T : RPGCharacter> List<T>.living() = this.filter { it.isAlive() }
    private fun <T : RPGCharacter> List<T>.dead() = this.filterNot { it.isAlive() }
}