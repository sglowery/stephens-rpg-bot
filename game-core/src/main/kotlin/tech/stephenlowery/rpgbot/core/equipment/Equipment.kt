package tech.stephenlowery.rpgbot.core.equipment

import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class Equipment(
    val name: String,
    val equipmentRole: EquipmentRole,
    val actions: Collection<CharacterAction>,
    val equipEffects: RPGCharacter.() -> Unit = { },
)