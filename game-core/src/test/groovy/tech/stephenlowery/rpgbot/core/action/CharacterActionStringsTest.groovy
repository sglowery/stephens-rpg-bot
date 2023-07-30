package tech.stephenlowery.rpgbot.core.action

import spock.lang.Specification
import tech.stephenlowery.rpgbot.utils.TestUtils

class CharacterActionStringsTest extends Specification {

    static def charOne = TestUtils.getTestCharacter(1L, "Stephen")
    static def charTwo = TestUtils.getTestCharacter(2L, "Ashley")

    def "getFormattedEffectResultString returns correctly formatted string based on EffectResult's fields"() {
        given:
        def characterActionStrings = new CharacterActionStrings(
                "queued", actionText, "success",
                "missed", "failed", "effectOver",
                "crit", "continued", "chained"
        )

        expect:
        text == characterActionStrings.getFormattedEffectResultString(effectResult)

        where:
        effectResult                                                                 | actionText || text
        new EffectResult(charOne, charTwo, 1, false, false, false, false, false, "") | ""         || "success"
        new EffectResult(charOne, charTwo, 1, true, false, false, false, false, "")  | ""         || "missed"
        new EffectResult(charOne, charTwo, 1, false, true, false, false, false, "")  | ""         || "crit"
        new EffectResult(charOne, charTwo, 1, false, false, true, false, false, "")  | ""         || "continued"
        new EffectResult(charOne, charTwo, 1, false, false, true, true, false, "")   | ""         || "effectOver"
        new EffectResult(charOne, charTwo, 1, false, false, false, false, true, "")  | ""         || "chained"

        new EffectResult(charOne, charTwo, 1, false, false, false, false, false, "") | "action"   || "action\nsuccess"
        new EffectResult(charOne, charTwo, 1, true, false, false, false, false, "")  | "action"   || "action\nmissed"
        new EffectResult(charOne, charTwo, 1, false, true, false, false, false, "")  | "action"   || "action\ncrit"
        new EffectResult(charOne, charTwo, 1, false, false, true, false, false, "")  | "action"   || "continued"
        new EffectResult(charOne, charTwo, 1, false, false, true, true, false, "")   | "action"   || "effectOver"
        new EffectResult(charOne, charTwo, 1, false, false, false, false, true, "")  | "action"   || "chained"
    }

    def "formatFromEffectResult replaces text markers with information in EffectResult"() {
        given:
        def characterActionStrings = new CharacterActionStrings(
                "", "", "{source} {target} {value} {other}", "", "", "", "", "", ""
        )

        expect:
        "Stephen Ashley 1 swag" == characterActionStrings.getFormattedEffectResultString(
                new EffectResult(
                        source: charOne,
                        target: charTwo,
                        value:1,
                        miss: false,
                        crit: false,
                        continued: false,
                        expired: false,
                        chained: false,
                        other: "swag"
                )
        )
    }
}
