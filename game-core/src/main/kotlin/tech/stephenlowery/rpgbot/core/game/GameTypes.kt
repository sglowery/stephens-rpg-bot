package tech.stephenlowery.rpgbot.core.game

import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.Attribute

typealias ValueFromRPGCharacterFn<T> = (RPGCharacter) -> T

typealias StatGetterFn = ValueFromRPGCharacterFn<Attribute>
