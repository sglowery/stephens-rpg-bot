package tech.stephenlowery.rpgbot.utils

import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.action.CharacterActionStrings
import tech.stephenlowery.rpgbot.core.action.CharacterActionType
import tech.stephenlowery.rpgbot.core.action.TargetingType
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.Attribute


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
        uniqueCharacterID += 1
        return new PlayerCharacter(uniqueCharacterID, "")
    }

    static PlayerCharacter getTestCharacter(Long id, String name = "") {
        return new PlayerCharacter(id, name)
    }

    static CharacterAction getTestCharacterAction(CharacterActionType characterActionType) {
        return new CharacterAction(new ActionEffect(),
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
                        '',
                        ''),
                null
        )
    }
}
