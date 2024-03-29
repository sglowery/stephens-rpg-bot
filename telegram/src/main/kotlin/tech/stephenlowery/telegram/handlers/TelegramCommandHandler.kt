package tech.stephenlowery.telegram.handlers

import com.github.kotlintelegrambot.entities.Message

interface TelegramCommandHandler<out T : TelegramCommandResult> {
    fun execute(message: Message): T
}