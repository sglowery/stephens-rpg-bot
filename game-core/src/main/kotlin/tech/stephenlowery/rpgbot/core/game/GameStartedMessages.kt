package tech.stephenlowery.rpgbot.core.game

@JvmInline
value class GameStartedMessages(val messagesTo: Collection<Pair<Long, String>>)