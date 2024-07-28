package tech.stephenlowery.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.UserState
import tech.stephenlowery.rpgbot.core.game.Game
import tech.stephenlowery.rpgbot.core.game.GameManager
import tech.stephenlowery.telegram.formatters.formatTelegramUserLink
import tech.stephenlowery.telegram.handlers.cancelgame.CancelGameConfirmCommandHandler
import tech.stephenlowery.telegram.handlers.cancelgame.GameCanBeCanceled
import tech.stephenlowery.telegram.handlers.joingame.JoinGameCommandHandler
import tech.stephenlowery.telegram.handlers.joingame.JoinedGame
import tech.stephenlowery.telegram.handlers.newgame.NewGameCommandHandler
import tech.stephenlowery.telegram.handlers.startgame.GameStarting
import tech.stephenlowery.telegram.handlers.startgame.PrivateChat
import tech.stephenlowery.telegram.handlers.startgame.StartGameCommandHandler

object TelegramRpgBot {

    fun start(telegramBotToken: String) {
        val rpgBot = bot {
            logLevel = LogLevel.Error
            token = telegramBotToken
            dispatch {
                command("newgame") { newGameCommand(bot, update, message) }
                command("join") { joinGameCommand(bot, update, message) }
                command("start") { startGameCommand(bot, update, message) }
                command("stats") { characterStatsCommand(bot, update, message) }
                command("calltoarms") { waitingOnCommand(bot, update, message) }
                command("cancelgame") { cancelGameCommandConfirm(bot, update, message) }
                callbackQuery {
                    val callbackDataSplit = update.callbackQuery!!.data.split("|", limit = 2)
                    val message = update.callbackQuery!!.message!!
                    when (callbackDataSplit[0]) {
                        "action" -> actionChosen(bot, update, message)
                        "target" -> targetChosen(bot, update, message)
                        "cancel" -> cancelGameChoiceHandler(callbackDataSplit.drop(1), bot, update)
                    }
                }
            }
        }
        println("bot running")
        rpgBot.startPolling()
    }

    private fun newGameCommand(bot: Bot, update: Update, message: Message) {
        val result = NewGameCommandHandler.execute(message)
        bot.sendMessage(ChatId.fromId(message.chat.id), result.message)
    }

    private fun joinGameCommand(bot: Bot, update: Update, message: Message) {
        val result = JoinGameCommandHandler.execute(message)
        val replyToMessageId = if (result is JoinedGame) null else message.messageId
        bot.sendMessage(ChatId.fromId(message.chat.id), result.message, replyToMessageId = replyToMessageId)
    }

    private fun startGameCommand(bot: Bot, update: Update, message: Message) {
        val chatID = message.chat.id
        val game = GameManager.findGame(chatID)
        var replyToMessageID: Long? = message.messageId
        var parseMode: ParseMode? = null
        val result = StartGameCommandHandler.execute(message)
        if (result is PrivateChat) {
            parseMode = ParseMode.MARKDOWN
            replyToMessageID = null
        }
        bot.sendMessage(ChatId.fromId(chatID), result.message, parseMode = parseMode, replyToMessageId = replyToMessageID)
        if (game?.hasStarted == true && result is GameStarting) {
            result.messages.forEach { (playerId, message) ->
                bot.sendMessage(ChatId.fromId(playerId), message)
            }
            // determine roles, let players pick archetypes and skills
            // should probably just happen in the game class
            sendPlayersInGameActions(bot, chatID)
        }
    }

    private fun sendPlayerSkillList(bot: Bot, player: PlayerCharacter, skills: Collection<CharacterAction>) {
        val userId = player.id
        val keyboard = skills.map { InlineKeyboardButton.CallbackData(it.displayName, "pickSkill|${it.identifier}") }.chunked(2)
        val replyMarkup = InlineKeyboardMarkup.create(keyboard)
        bot.sendMessage(ChatId.fromId(userId), "Pick a skill", replyMarkup = replyMarkup)
        // TODO implement players choosing skills instead of always having them randomly assigned
    }

    private fun characterStatsCommand(bot: Bot, update: Update, message: Message) {
        val response = message.from?.let { GameManager.findCharacter(it.id)?.getCharacterStatusText() } ?: return
        bot.sendMessage(
            chatId = ChatId.fromId(message.chat.id),
            replyToMessageId = message.messageId,
            text = response
        )
    }

    private fun waitingOnCommand(bot: Bot, update: Update, message: Message) {
        val chatID = message.chat.id
        val game = GameManager.findGame(chatID)
        if (message.chat.type != "group" || game == null) {
            return
        }
        val waitingOn = game.waitingOn()
        if (waitingOn.isEmpty()) {
            return
        }
        val names = waitingOn.joinToString(separator = ", ", transform = ::formatTelegramUserLink)
        bot.sendMessage(ChatId.fromId(chatID), "Waiting on the following player(s): $names.", parseMode = ParseMode.MARKDOWN)
    }

    private fun cancelGameCommandConfirm(bot: Bot, update: Update, message: Message) {
        val chatID = message.chat.id
        val result = CancelGameConfirmCommandHandler.execute(message)
        var replyMarkup: ReplyMarkup? = null
        if (result is GameCanBeCanceled) {
            val choices = listOf(
                InlineKeyboardButton.CallbackData("Yes", callbackData = "cancel|yes"),
                InlineKeyboardButton.CallbackData("No", callbackData = "cancel|no")
            )
            replyMarkup = InlineKeyboardMarkup.createSingleRowKeyboard(choices)
        }
        bot.sendMessage(ChatId.fromId(chatID), result.message, replyToMessageId = message.messageId, replyMarkup = replyMarkup)
    }

    private fun cancelGameChoiceHandler(callbackDataSplit: List<String>, bot: Bot, update: Update) = when (callbackDataSplit[1].lowercase()) {
        "yes" -> TelegramRpgBot::cancelGameYes
        else  -> TelegramRpgBot::cancelGameNo
    }.invoke(bot, update.message!!)

    private fun cancelGameYes(bot: Bot, message: Message) {
        bot.sendMessage(ChatId.fromId(message.chat.id), CancelGameConfirmCommandHandler.getGameCanceledMessage(message.from!!.firstName))
    }

    private fun cancelGameNo(bot: Bot, message: Message) {
        bot.sendMessage(ChatId.fromId(message.chat.id), "K")
    }

    private fun sendPlayersInGameActions(bot: Bot, chatID: Long) {
        val game = GameManager.findGame(chatID)!!
        val livingHumanPlayers = game.getHumanPlayers().values.living()
        livingHumanPlayers.filter { it.characterState == UserState.CHOOSING_ACTION }.forEach {
            val keyboard = makeKeyboardFromPlayerActions(it.getAvailableActions())
            val replyMarkup = InlineKeyboardMarkup.create(keyboard)
            bot.sendMessage(ChatId.fromId(it.id), it.getPreActionText() + "\n\nPick an action.", replyMarkup = replyMarkup)
        }
        game.getHumanPlayers().values.living().filter { it.characterState == UserState.OCCUPIED }.forEach {
            bot.sendMessage(ChatId.fromId(it.id), "You are occupied this turn and can't choose an action.")
        }
    }

    // TODO move this code to a handler
    private fun actionChosen(bot: Bot, update: Update, message: Message) {
        val callbackQuery = update.callbackQuery!!
        val userID = callbackQuery.from.id
        val actionName = callbackQuery.data
        val callbackQueryMessageId = message.messageId
        val newCharacterState: UserState
        val queuedText: String
        try {
            val chooseActionResult = GameManager.chooseActionForCharacter(userID, actionName)
            newCharacterState = chooseActionResult.newCharacterState
            queuedText = chooseActionResult.queuedActionText
        } catch (exception: RuntimeException) {
            exception.printStackTrace()
            // TODO
            //  if deleting the message do i need to remove the keyboard?
            //  bot.editMessageReplyMarkup(ChatId.fromId(userID), update.message!!.messageId, replyMarkup = ReplyKeyboardRemove())
            bot.deleteMessage(ChatId.fromId(userID), callbackQueryMessageId)
            return
        }
        if (newCharacterState == UserState.WAITING) {
            bot.editMessageText(
                chatId = ChatId.fromId(userID),
                messageId = callbackQueryMessageId,
                inlineMessageId = null,
                text = queuedText
            )
        } else if (newCharacterState == UserState.CHOOSING_TARGETS) {
            sendTargetsToPlayer(bot, userID, callbackQueryMessageId)
        }
        val game = GameManager.findGameContainingCharacter(userID)!!
        if (game.allPlayersReadyForTurnToResolve()) {
            resolveActionsInGame(bot, game)
        }
    }

    private fun sendTargetsToPlayer(bot: Bot, playerId: Long, messageId: Long) {
        val game = GameManager.findGameContainingCharacter(playerId)!!
        val character = GameManager.findCharacter(playerId)!!
        val targets = game.getTargetsForCharacter(character)
        bot.editMessageText(
            chatId = ChatId.fromId(character.id),
            messageId = messageId,
            inlineMessageId = null,
            text = character.getPreActionText() + "\n\nChoose a target.",
            replyMarkup = InlineKeyboardMarkup.create(makeKeyboardFromPlayerNames(targets))
        )
    }

    // TODO move this code to a handler
    private fun targetChosen(bot: Bot, update: Update, message: Message) {
        val callbackQuery = update.callbackQuery!!
        val userId = callbackQuery.from.id
        val game = GameManager.findGameContainingCharacter(userId)
        val fromCharacter = GameManager.findCharacter(userId)
        if (game != null && fromCharacter != null && fromCharacter.characterState == UserState.CHOOSING_TARGETS) {
            val target = callbackQuery.data.split("|")[1].toLong()
            game.addTargetToQueuedCharacterAction(userId, target)
            bot.editMessageText(
                chatId = ChatId.fromId(userId),
                messageId = callbackQuery.message!!.messageId,
                inlineMessageId = null,
                text = fromCharacter.queuedAction!!.getQueuedText()
            )
            if (game.allPlayersReadyForTurnToResolve()) {
                resolveActionsInGame(bot, game)
            }
        }
    }

    // TODO maybe move this code to a handler?
    private fun resolveActionsInGame(bot: Bot, game: Game) {
        val resolvedActionsText = game.resolveActionsAndGetResults()
        bot.sendMessage(ChatId.fromId(game.id), resolvedActionsText, replyMarkup = ReplyKeyboardRemove(), parseMode = ParseMode.MARKDOWN)
        val deadPlayers = game.getHumanPlayers().values.dead()
        deadPlayers.forEach { player ->
            bot.sendMessage(ChatId.fromId(player.id), "You died in the previous round and have been removed from the game.")
        }
        game.removeCharacters(deadPlayers)
        if (game.isOver()) {
            bot.sendMessage(ChatId.fromId(game.id), game.getGameEndedText())
            GameManager.cancelGame(game.id)
        } else {
            if (game.allPlayersReadyForTurnToResolve()) {
                resolveActionsInGame(bot, game)
            } else {
                sendPlayersInGameActions(bot, game.id)
            }
        }
    }

    private fun makeKeyboardFromPlayerNames(characters: Collection<RPGCharacter>): List<List<InlineKeyboardButton>> {
        return characters.map { InlineKeyboardButton.CallbackData(text = it.getNameAndHealthPercentLabel(), callbackData = "target|${it.id}") }.chunked(2)
    }

    private fun makeKeyboardFromPlayerActions(actions: Collection<CharacterAction>): List<List<InlineKeyboardButton>> {
        return actions.map { InlineKeyboardButton.CallbackData(text = it.displayName, callbackData = it.identifier) }.chunked(2)
    }

    private fun <T : RPGCharacter> Collection<T>.living() = this.filter { it.isAlive() }
    private fun <T : RPGCharacter> Collection<T>.dead() = this.filter { it.isDead() }
}