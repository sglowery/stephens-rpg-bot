package tech.stephenlowery.telegram.formatters

import tech.stephenlowery.rpgbot.core.character.PlayerCharacter

fun formatTelegramUserLink(name: String, userId: Long): String = "[${name}](tg://user?id=${userId})"

fun formatTelegramUserLink(player: PlayerCharacter): String = formatTelegramUserLink(player.name, player.id)
