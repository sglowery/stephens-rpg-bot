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
import tech.stephenlowery.rpgbot.core.character.CharacterState
import tech.stephenlowery.rpgbot.core.equipment.EquipmentAction
import tech.stephenlowery.rpgbot.core.game.ChooseActionResult
import tech.stephenlowery.rpgbot.core.game.Game
import tech.stephenlowery.rpgbot.core.game.GameManager
import tech.stephenlowery.telegram.formatters.formatTelegramUserLink
import tech.stephenlowery.telegram.handlers.cancelgame.CancelGameConfirmCommandHandler
import tech.stephenlowery.telegram.handlers.cancelgame.GameCanBeCanceled
import tech.stephenlowery.telegram.handlers.joingame.JoinGameCommandHandler
import tech.stephenlowery.telegram.handlers.joingame.JoinedGame
import tech.stephenlowery.telegram.handlers.newgame.NewGameCommandHandler
import tech.stephenlowery.telegram.handlers.startgame.*

object TelegramRpgBot {

    fun start(telegramBotToken: String) {
        val rpgBot = bot {
            logLevel = LogLevel.All()
            token = telegramBotToken

            dispatch {
                command("newgame") { handleNewGameCommand(bot, update, message) }
                command("join") { joinGameCommand(bot, update, message) }
                command("start") { startGameCommand(bot, update, message) }
                command("stats") { characterStatsCommand(bot, update, message) }
                command("calltoarms") { waitingOnCommand(bot, update, message) }
                command("cancelgame") { cancelGameCommandConfirm(bot, update, message) }
                callbackQuery {
                    val callbackDataSplit = update.callbackQuery!!.data.split("|", limit = 2)
                    val message = update.callbackQuery!!.message!!
                    when (callbackDataSplit[0]) {
                        "action" -> confirmAction(bot, update, message)
                        "confirmaction" -> actionChosen(bot, update, message, callbackDataSplit[1])
                        "target" -> targetChosen(bot, update, message)
                        "cancel" -> cancelGameChoiceHandler(callbackDataSplit.drop(1), bot, update)
                        "goback" -> goBackToChoosingAction(bot, message)
                    }
                }
            }
        }
        println("bot running")
        rpgBot.startPolling()
    }

    private fun goBackToChoosingAction(bot: Bot, message: Message) {
        val userCharacter = GameManager.findCharacter(message.chat.id) ?: return
        if (userCharacter.characterState != CharacterState.CHOOSING_TARGETS && userCharacter.characterState != CharacterState.CHOOSING_ACTION) {
            return
        }
        userCharacter.queuedAction = null
        userCharacter.characterState = CharacterState.CHOOSING_ACTION
        sendSinglePlayerActions(bot, userCharacter, message.messageId)
    }

    private fun handleNewGameCommand(bot: Bot, update: Update, message: Message) {
        bot.sendMessage(ChatId.fromId(message.from!!.id), "You are starting an RPGBot game; this message is to test that I can message you.")
            .fold(
                ifSuccess = { createNewGame(bot, message, it) },
                ifError = { notifyUserHasNotStartedBot(bot, message.chat.id, message.messageId) }
            )

    }

    private fun createNewGame(bot: Bot, messageFromUser: Message, messageToUser: Message) {
        val result = NewGameCommandHandler.execute(messageFromUser)
        bot.sendMessage(ChatId.fromId(messageFromUser.chat.id), result.message)
        bot.deleteMessage(ChatId.fromId(messageToUser.chat.id), messageToUser.messageId)
    }

    private fun joinGameCommand(bot: Bot, update: Update, message: Message) {
        bot.sendMessage(ChatId.fromId(message.from!!.id), "You are joining an RPGBot game; this message is to test that I can message you.")
            .fold(
                ifSuccess = { attemptToJoinGame(bot, message, it) },
                ifError = { notifyUserHasNotStartedBot(bot, message.chat.id, message.messageId) }
            )
    }

    private fun attemptToJoinGame(bot: Bot, messageFromUser: Message, botMessageToUser: Message) {
        val result = JoinGameCommandHandler.execute(messageFromUser)
        val replyToMessageId = if (result is JoinedGame) null else messageFromUser.messageId
        bot.sendMessage(ChatId.fromId(messageFromUser.chat.id), result.message, replyToMessageId = replyToMessageId)
        if (replyToMessageId != null) {
            bot.editMessageText(
                ChatId.fromId(botMessageToUser.chat.id),
                botMessageToUser.messageId,
                text = "You have joined an RPGBot game. Wait for the initiator to start it."
            )
        }
    }

    private fun notifyUserHasNotStartedBot(bot: Bot, chatId: Long, messageId: Long) {
        bot.sendMessage(ChatId.fromId(chatId), "You need to open a private chat with me and use the /start command.", replyToMessageId = messageId)
    }

    private fun startGameCommand(bot: Bot, update: Update, message: Message) {
        val result = StartGameCommandHandler.execute(message)
        when (result) {
            is CreateGame -> handleNewGameCommand(bot, update, message)
            else          -> {
                handleStartGameCommand(bot, message, result)
            }
        }

    }

    private fun handleStartGameCommand(bot: Bot, message: Message, result: StartGameResult) {
        val chatID = message.chat.id
        val game = GameManager.findGame(chatID)
        var replyToMessageID: Long? = message.messageId
        var parseMode: ParseMode? = null
        if (result is PrivateChat) {
            parseMode = ParseMode.MARKDOWN
            replyToMessageID = null
        }
        bot.sendMessage(ChatId.fromId(chatID), result.message, parseMode = parseMode, replyToMessageId = replyToMessageID)
        if (game?.hasStarted == true && result is GameStarting) {
            result.messages.forEach { (playerId, message) ->
                bot.sendMessage(ChatId.fromId(playerId), message)
            }
            sendPlayersInGameActions(bot, chatID)
        }
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

    private fun cancelGameChoiceHandler(callbackDataSplit: List<String>, bot: Bot, update: Update) = when (callbackDataSplit.first().lowercase()) {
        "yes" -> ::cancelGameYes
        else  -> ::cancelGameNo
    }.invoke(bot, update.callbackQuery!!.message!!)

    private fun cancelGameYes(bot: Bot, message: Message) {
        bot.sendMessage(ChatId.fromId(message.chat.id), CancelGameConfirmCommandHandler.getGameCanceledMessage(message.from!!.firstName))
        GameManager.cancelGame(message.chat.id)
    }

    private fun cancelGameNo(bot: Bot, message: Message) {
        bot.sendMessage(ChatId.fromId(message.chat.id), "K")
    }

    private fun sendPlayersInGameActions(bot: Bot, chatID: Long) {
        val game = GameManager.findGame(chatID)!!
        val livingHumanPlayers = game.getHumanPlayers().values.living()
        livingHumanPlayers.filter { it.characterState == CharacterState.CHOOSING_ACTION }.forEach {
            val keyboard = makeKeyboardFromPlayerActions(it.getAvailableActions())
            val replyMarkup = InlineKeyboardMarkup.create(keyboard)
            bot.sendMessage(ChatId.fromId(it.id), it.getPreActionText() + "\n\nPick an action.", replyMarkup = replyMarkup, parseMode = ParseMode.MARKDOWN)
        }
        notifyOccupiedUsers(bot, game)
    }

    private fun sendSinglePlayerActions(bot: Bot, player: PlayerCharacter, editMessageId: Long) {
        val keyboard = makeKeyboardFromPlayerActions(player.getAvailableActions())
        val replyMarkup = InlineKeyboardMarkup.create(keyboard)
        bot.editMessageText(
            ChatId.fromId(player.id),
            editMessageId,
            text = player.getPreActionText() + "\n\nPick an action.",
            replyMarkup = replyMarkup,
            parseMode = ParseMode.MARKDOWN
        )
    }

    private fun notifyOccupiedUsers(bot: Bot, game: Game) {
        game.livingPlayers().filter { it.characterState == CharacterState.OCCUPIED }
            .forEach {
                bot.sendMessage(ChatId.fromId(it.id), "You are occupied this turn and can't choose an action.")
            }
    }

    private fun confirmAction(bot: Bot, update: Update, message: Message) {
        val callbackQuery = update.callbackQuery!!
        val action = GameManager.findCharacterAction(callbackQuery.data)!!
        if (action.characterAction.targetingType.requiresChoosingTarget()) {
            actionChosen(bot, update, callbackQuery.message!!, action.identifier)
        } else {
            sendConfirmActionButtons(action.characterAction, callbackQuery, bot)
        }
    }

    private fun sendConfirmActionButtons(
        action: CharacterAction,
        callbackQuery: CallbackQuery,
        bot: Bot,
    ) {
        val actionName = action.displayName
        val actionIdentifier = action.identifier
        val buttons = listOf(
            listOf(InlineKeyboardButton.CallbackData("<-- Go Back", "goback")),
            listOf(InlineKeyboardButton.CallbackData("Confirm", "confirmaction|$actionIdentifier"))
        )
        val editMessageId = callbackQuery.message!!.messageId
        bot.editMessageText(
            chatId = ChatId.fromId(callbackQuery.from.id),
            messageId = editMessageId,
            text = "Confirm using $actionName?",
            replyMarkup = InlineKeyboardMarkup.create(buttons)
        )
    }

    // TODO move this code to a handler
    private fun actionChosen(bot: Bot, update: Update, message: Message, actionIdentifier: String) {
        val userID = when (message.from?.isBot) {
            true -> message.chat.id
            else -> message.from!!.id
        }
        val callbackQueryMessageId = message.messageId
        val chooseActionResult: ChooseActionResult
        try {
            chooseActionResult = GameManager.chooseActionForCharacter(userID, actionIdentifier)
        } catch (exception: RuntimeException) {
            exception.printStackTrace()
            // TODO
            //  if deleting the message do i need to remove the keyboard?
            //  bot.editMessageReplyMarkup(ChatId.fromId(userID), update.message!!.messageId, replyMarkup = ReplyKeyboardRemove())
            bot.deleteMessage(ChatId.fromId(userID), callbackQueryMessageId)
            return
        }
        val (newCharacterState, queuedActionText, character) = chooseActionResult
        if (newCharacterState == CharacterState.WAITING) {
            val queuedText = character.getPreActionText() + "\n\n" + queuedActionText
            bot.editMessageText(
                chatId = ChatId.fromId(userID),
                messageId = callbackQueryMessageId,
                inlineMessageId = null,
                text = queuedText,
                parseMode = ParseMode.MARKDOWN
            )
        } else if (newCharacterState == CharacterState.CHOOSING_TARGETS) {
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
            replyMarkup = InlineKeyboardMarkup.create(makeKeyboardForChoosingTarget(targets)),
            parseMode = ParseMode.MARKDOWN
        )
    }

    private fun makeKeyboardForChoosingTarget(targets: Collection<RPGCharacter>): List<List<InlineKeyboardButton>> {
        return listOf(listOf(InlineKeyboardButton.CallbackData("<-- Go Back", "goback"))) + makeKeyboardFromPlayerNames(targets)
    }

    // TODO move this code to a handler
    private fun targetChosen(bot: Bot, update: Update, message: Message) {
        val callbackQuery = update.callbackQuery!!
        val userId = callbackQuery.from.id
        val game = GameManager.findGameContainingCharacter(userId)
        val fromCharacter = GameManager.findCharacter(userId)
        if (game != null && fromCharacter != null && fromCharacter.characterState == CharacterState.CHOOSING_TARGETS) {
            val target = callbackQuery.data.split("|")[1].toLong()
            game.addTargetToQueuedCharacterAction(userId, target)
            bot.editMessageText(
                chatId = ChatId.fromId(userId),
                messageId = callbackQuery.message!!.messageId,
                inlineMessageId = null,
                text = fromCharacter.getPreActionText() + "\n\n" + fromCharacter.queuedAction!!.getQueuedText(),
                parseMode = ParseMode.MARKDOWN
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
        val deadPlayers = game.getHumanPlayers().values.dead().filter { it.characterState != CharacterState.DEAD }
        deadPlayers.forEach { player ->
            bot.sendMessage(ChatId.fromId(player.id), "You died in the previous round and have been removed from the game.")
            player.characterState = CharacterState.DEAD
        }
        if (game.isOver()) {
            bot.sendMessage(ChatId.fromId(game.id), game.getGameEndedText() + "\n\n" + game.getPostGameStatsString())
            GameManager.cancelGame(game.id)
        } else {
            if (game.allPlayersReadyForTurnToResolve()) {
                notifyOccupiedUsers(bot, game)
                resolveActionsInGame(bot, game)
            } else {
                sendPlayersInGameActions(bot, game.id)
            }
        }
    }

    private fun makeKeyboardFromPlayerNames(characters: Collection<RPGCharacter>): List<List<InlineKeyboardButton>> {
        return characters.map { InlineKeyboardButton.CallbackData(text = it.getNameAndHealthPercentLabel(), callbackData = "target|${it.id}") }.chunked(2)
    }

    private fun makeKeyboardFromPlayerActions(actions: Collection<EquipmentAction>): List<List<InlineKeyboardButton>> {
        return actions.map { InlineKeyboardButton.CallbackData(text = it.getName(), callbackData = it.characterAction.identifier) }.chunked(2)
    }

    private fun <T : RPGCharacter> Collection<T>.living() = this.filter { it.isAlive() }
    private fun <T : RPGCharacter> Collection<T>.dead() = this.filter { it.isDead() }
}