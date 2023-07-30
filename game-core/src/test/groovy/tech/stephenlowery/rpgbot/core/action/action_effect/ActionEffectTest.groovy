package tech.stephenlowery.rpgbot.core.action.action_effect


import spock.lang.Specification
import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class ActionEffectTest extends Specification {

    private static final class TestableActionEffect extends ActionEffect {
        TestableActionEffect(int duration) {
            super(duration)
        }

        @Override
        List<EffectResult> applyEffect(RPGCharacter from, RPGCharacter to, int cycle) {
            return null
        }
    }

    def "isExpired() returns correct value for ActionEffect parameters"() {
        expect:
        expected == new TestableActionEffect(duration).isExpired(cycle)

        where:
        duration | cycle || expected
        -1       | 50    || false
        1        | 0     || false
        1        | 1     || true
        1        | 2     || true
    }
}
