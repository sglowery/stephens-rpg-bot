package tech.stephenlowery.rpgbot.sample

import tech.stephenlowery.telegram.TelegramRpgBot

fun main(args: Array<String>) {
    TelegramRpgBot.start(System.getenv("token"))
}