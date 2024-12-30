package tech.stephenlowery.rpgbot.core.action

import spock.lang.Specification
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.utils.TestUtils

class CharacterActionStringsTest extends Specification {

    static def charOne = TestUtils.getTestCharacter(1L, "Stephen")
    static def charTwo = TestUtils.getTestCharacter(2L, "Ashley")

    def "getFormattedEffectResultString returns correctly formatted string based on EffectResult's fields"() {
        given:
        def characterActionStrings = new CharacterActionStrings(
                actionText,
                "success",
                "missed",
                "crit",
                "continued",
                "effectOver",
                "chained",
                "triggered",
        )

        expect:
        text == characterActionStrings.getFormattedEffectResultString(result)

        where:
        result                                                                                 | actionText || text
        effectResult(charOne, charTwo, 1, false, false, false, false, false, false, false, "") | ""         || "success"
        effectResult(charOne, charTwo, 1, true, false, false, false, false, false, false, "")  | ""         || "missed"
        effectResult(charOne, charTwo, 1, false, true, false, false, false, false, false, "")  | ""         || "crit"
        effectResult(charOne, charTwo, 1, false, false, true, false, false, false, false, "")  | ""         || "continued"
        effectResult(charOne, charTwo, 1, false, false, true, true, false, false, false, "")   | ""         || "continued\neffectOver"
        effectResult(charOne, charTwo, 1, false, false, false, false, true, false, false, "")  | ""         || "chained"
        effectResult(charOne, charTwo, 1, false, false, false, false, false, false, true, "")  | ""         || "success\ntriggered"

        effectResult(charOne, charTwo, 1, false, false, false, false, false, false, false, "") | "action"   || "action\nsuccess"
        effectResult(charOne, charTwo, 1, true, false, false, false, false, false, false, "")  | "action"   || "action\nmissed"
        effectResult(charOne, charTwo, 1, false, true, false, false, false, false, false, "")  | "action"   || "action\ncrit"
        effectResult(charOne, charTwo, 1, false, false, true, false, false, false, false, "")  | "action"   || "continued"
        effectResult(charOne, charTwo, 1, false, false, true, true, false, false, false, "")   | "action"   || "continued\neffectOver"
        effectResult(charOne, charTwo, 1, false, false, false, false, true, false, false, "")  | "action"   || "chained"
        effectResult(charOne, charTwo, 1, false, false, false, false, false, true, false, "")  | "action"   || "action"
        effectResult(charOne, charTwo, 1, false, false, false, false, false, false, true, "")  | "action"   || "action\nsuccess\ntriggered"
    }

    def "formatFromEffectResult replaces text markers with information in EffectResult"() {
        given:
        def characterActionStrings = new CharacterActionStrings(
                "",
                "{source} {target} {value} {other}",
                "",
                "",
                "",
                "",
                "",
                "",
        )

        expect:
        "Stephen Ashley 1 swag" == characterActionStrings.getFormattedEffectResultString(
                effectResult(
                        charOne,
                        charTwo,
                        1,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        "swag"
                )
        )
    }

    def effectResult(
            RPGCharacter from,
            RPGCharacter to,
            int value,
            boolean miss,
            boolean crit,
            boolean continued,
            boolean expired,
            boolean chained,
            boolean occupied,
            boolean triggered,
            String other
    ) {
        return new EffectResult(
                from,
                to,
                value,
                CharacterActionType.OTHER,
                miss,
                crit,
                continued,
                expired,
                chained,
                occupied,
                triggered,
                other
        )
    }
}
