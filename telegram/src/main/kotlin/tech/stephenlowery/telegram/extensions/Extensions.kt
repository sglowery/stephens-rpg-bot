package tech.stephenlowery.telegram.extensions

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
    replyMarkup: ReplyMarkup? = null,
) = sendMessage(
    chatId = ChatId.fromId(chatId),
    text = text,
    parseMode = parseMode,
    disableWebPagePreview = disableWebPagePreview,
    disableNotification = disableNotification,
    replyToMessageId = replyToMessageId,
    replyMarkup = replyMarkup,
)

fun Bot.editMessageText(
    chatId: Long,
    messageId: Long? = null,
    inlineMessageId: String? = null,
    text: String,
    parseMode: ParseMode? = null,
    disableWebPagePreview: Boolean? = null,
    replyMarkup: ReplyMarkup? = null,
) = editMessageText(
    chatId = ChatId.fromId(chatId),
    messageId = messageId,
    inlineMessageId = inlineMessageId,
    text = text,
    parseMode = parseMode,
    disableWebPagePreview = disableWebPagePreview,
    replyMarkup = replyMarkup,
)

fun Bot.deleteMessage(chatId: Long, messageId: Long) = deleteMessage(chatId = ChatId.fromId(chatId), messageId = messageId)