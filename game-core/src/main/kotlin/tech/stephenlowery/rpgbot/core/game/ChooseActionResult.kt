package tech.stephenlowery.rpgbot.core.game

import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.CharacterState

data class ChooseActionResult(
    val newCharacterState: CharacterState,
    val queuedActionText: String?,
    val character: PlayerCharacter
)
