package tech.stephenlowery.rpgbot.core.equipment

import spock.lang.Shared
import spock.lang.Specification
import tech.stephenlowery.rpgbot.core.action.ActionResource

class ActionResourceTest extends Specification {

    @Shared
    ActionResource actionResource

    def setup() {
        actionResource = getResource(false)
    }

    def "cycle correctly changes value amount after defined number of cycles"() {
        when:
        actionResource.cycle()

        then:
        actionResource.value == 0

        when:
        actionResource.cycle()

        then:
        actionResource.value == actionResource.regenPerCycle

        when:
        actionResource.cycle()

        then:
        actionResource.value == actionResource.regenPerCycle

        when:
        actionResource.cycle()

        then:
        actionResource.value == actionResource.regenPerCycle * 2
    }

    def "useResource() changes value and respects max and min values"() {
        when: 'an excess amount of time to fully charge resource'
        cycleResource(actionResource, 12)

        then:
        actionResource.value == actionResource.maxValue

        when:
        actionResource.useResource()

        then:
        actionResource.value == actionResource.maxValue + actionResource.valuePerUse

        when:
        actionResource.useResource()

        then:
        actionResource.value == actionResource.maxValue + actionResource.valuePerUse * 2

        when:
        useResource(actionResource, 100)

        then:
        actionResource.value == actionResource.minValue
    }

    def "IsUsable"() {
    }

    def "unitString() incorporates value and uses singular or plural correctly"() {
        when:
        cycleResource(actionResource, 12)

        then:
        actionResource.unitString() == "100 zorches"

        when:
        actionResource.value = 1

        then:
        actionResource.unitString() == "1 zorch"

        when:
        actionResource.useResource()

        then:
        actionResource.unitString() == "0 zorches"
    }

    def "unitString() incorporates value and uses singular or plural correctly with max value"() {
        given:
        def resource = getResource(true)

        when:
        cycleResource(resource, 12)

        then:
        resource.unitString() == "100 / 100 zorches"

        when:
        resource.value = 1

        then:
        resource.unitString() == "1 / 100 zorch"

        when:
        resource.useResource()

        then:
        resource.unitString() == "0 / 100 zorches"
    }

    private static ActionResource getResource(boolean includeMaxValueInString) {
        return new ActionResource(
                0,
                100,
                20,
                2,
                true,
                false,
                false,
                "zorch",
                "zorches",
                -25,
                Integer::toString,
                includeMaxValueInString,
                0,
                { ActionResource a -> true }

        )
    }

    private static void cycleResource(ActionResource resource, int times) {
        for (int i = 0; i < times; i++) {
            resource.cycle()
        }
    }

    private static void useResource(ActionResource resource, int times) {
        for (int i = 0; i < times; i++) {
            resource.useResource()
        }
    }
}
