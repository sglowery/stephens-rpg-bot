package tech.stephenlowery.rpgbot.core.action.action_effect

import spock.lang.Specification

class ActionEffectTest extends Specification {

    def "isExpired() returns correct value for ActionEffect parameters"() {
        expect:
        expected == new ActionEffect(duration).isExpired(cycle)

        where:
        duration | cycle || expected
        -1       | 50    || false
        1        | 0     || false
        1        | 1     || true
        1        | 2     || true
    }
}
