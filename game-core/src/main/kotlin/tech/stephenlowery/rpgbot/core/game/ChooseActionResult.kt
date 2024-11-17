package tech.stephenlowery.rpgbot.core.game

import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.UserState

data class ChooseActionResult(
    val newCharacterState: UserState,
    val queuedActionText: String?,
    val character: PlayerCharacter
)
