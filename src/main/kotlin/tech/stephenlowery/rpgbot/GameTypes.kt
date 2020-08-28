package tech.stephenlowery.rpgbot

import tech.stephenlowery.rpgbot.models.character.Attribute
import tech.stephenlowery.rpgbot.models.character.RPGCharacter

typealias StatGetterFn = (RPGCharacter) -> Attribute

typealias ValueFromCharacterFn<T> = (RPGCharacter) -> T