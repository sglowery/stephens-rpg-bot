package tech.stephenlowery.util.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.ReplyMarkup

fun Bot.sendMessage(
    chatId: Long,
    text: String,
    parseMode: ParseMode? = null,
    disableWebPagePreview: Boolean? = null,
    disableNotification: Boolean? = null,
    replyToMessageId: Long? = null,
    replyMarkup: ReplyMarkup? = null
) = sendMessage(
    ChatId.fromId(chatId),
    text,
    parseMode,
    disableWebPagePreview,
    disableNotification,
    replyToMessageId,
    replyMarkup,
)

fun Bot.editMessageText(
    chatId: Long,
    messageId: Long? = null,
    inlineMessageId: String? = null,
    text: String,
    parseMode: ParseMode? = null,
    disableWebPagePreview: Boolean? = null,
    replyMarkup: ReplyMarkup? = null
) = editMessageText(
    ChatId.fromId(chatId),
    messageId,
    inlineMessageId,
    text,
    parseMode,
    disableWebPagePreview,
    replyMarkup,
)

fun Bot.deleteMessage(chatId: Long, messageId: Long) = deleteMessage(ChatId.fromId(chatId), messageId)