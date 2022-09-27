package tech.stephenlowery.util.rpgbot.game

import tech.stephenlowery.rpgbot.core.character.NonPlayerCharacter
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

fun List<RPGCharacter>.humanCharacters() = this.filterIsInstance<PlayerCharacter>()
fun List<RPGCharacter>.nonPlayerCharacters() = this.filterIsInstance<NonPlayerCharacter>()
