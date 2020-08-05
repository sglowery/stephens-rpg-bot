package tech.stephenlowery.rpgbot.assets

import tech.stephenlowery.rpgbot.RPGBot
import tech.stephenlowery.rpgbot.models.character.RPGCharacter

class TestCharacterFactory {

    companion object {

        private var uniqueLong = 1L
        private val names = listOf("Stephen", "Ashley", "Zach", "Cody", "April")
        fun newCharacter(): RPGCharacter = RPGCharacter(uniqueLong, names.random()).also { uniqueLong += 1L }
    }
}