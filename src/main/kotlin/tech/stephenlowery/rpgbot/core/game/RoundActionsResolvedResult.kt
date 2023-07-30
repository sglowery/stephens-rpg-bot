package tech.stephenlowery.rpgbot.core.game

data class RoundActionsResolvedResult(
    val messages: Collection<String>,
    val killedPlayersThisRound: Collection<String>,
)
