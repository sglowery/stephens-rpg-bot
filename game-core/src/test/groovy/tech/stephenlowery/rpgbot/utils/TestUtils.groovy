package tech.stephenlowery.rpgbot.utils

import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.action.CharacterActionStrings
import tech.stephenlowery.rpgbot.core.action.CharacterActionType
import tech.stephenlowery.rpgbot.core.action.TargetingType
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.Attribute

import java.time.LocalDateTime


final class TestUtils {

    private TestUtils() {}

    private static long uniqueCharacterID = 0L

    static Attribute getTestAttribute(String name, Double base) {
        return getTestAttribute(name, base, null, null)
    }

    static Attribute getTestAttribute(String name, Double base, Integer min, Integer max) {
        return new Attribute(name, base, min, max, Integer.&toString)
    }

    static PlayerCharacter getTestCharacter() {
        return new PlayerCharacter(++uniqueCharacterID, LocalDateTime.now().toString() + "_$uniqueCharacterID")
    }

    static PlayerCharacter getTestCharacter(Long id, String name = "") {
        uniqueCharacterID = Math.max(uniqueCharacterID, id) + 1L
        return new PlayerCharacter(id, name)
    }

    static ActionEffect getTestActionEffect(int duration) {
        return [duration: duration] as ActionEffect
    }

    static CharacterAction getTestCharacterAction(CharacterActionType characterActionType) {
        return new CharacterAction(
                getTestActionEffect(1),
                '',
                '',
                '',
                0,
                1,
                characterActionType,
                TargetingType.SELF,
                new CharacterActionStrings(
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        ''),
                {}
        )
    }
}
