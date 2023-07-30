package tech.stephenlowery.rpgbot.core.character

import spock.lang.Specification

class PlayerCharacterTest extends Specification {
    def "chooseAction() changes character state based on chosen action's targeting type"() {
        given:
        def character = new PlayerCharacter(1L, "")

        when:
        character.chooseAction(actionCallbackData)

        then:
        expectedCharacterState == character.characterState

        where:
        actionCallbackData || expectedCharacterState
        "action|attack"    || UserState.CHOOSING_TARGETS
        "action|defend"    || UserState.WAITING
    }
}
