import org.junit.Test
import tech.stephenlowery.rpgbot.models.*
import kotlin.test.assertEquals

class CharacterActionStringsTest {

    @Test
    fun `formatFromEffectResult() replaces text markers with information in an EffectResult`() {
        val source = RPGCharacter(1L, "Stephen")
        val target = RPGCharacter(2L, "Ashley")
        val effectResult = EffectResult(source = source, target = target, value = 5, text = "an effect")
        assertEquals(
            expected = "Stephen uses an effect on Ashley 5 times",
            actual = "{source} uses {other} on {target} {value} times".formatFromEffectResult(effectResult)
        )
    }

    @Test
    fun `getFormattedEffectResultString() correctly returns formatted string based on EffectResult's fields`() {

        class TestCase(val effectResult: EffectResult, val expectedString: String)

        val source = RPGCharacter(1L, "Stephen")
        val target = RPGCharacter(2L, "Ashley")
        val characterActionStrings = CharacterActionStrings(
            successText = "{source} succeeds against {target}",
            missedText = "{source} fails against {target}",
            critText = "{source} crits against {target}"
        )
        listOf(
            TestCase(EffectResult(source = source, target = target), "Stephen succeeds against Ashley"),
            TestCase(EffectResult(source = source, target = target, miss = true), "Stephen fails against Ashley"),
            TestCase(EffectResult(source = source, target = target, crit = true), "Stephen crits against Ashley")
        ).forEach { case ->
            val expected = case.expectedString
            val actual = characterActionStrings.getFormattedEffectResultString(case.effectResult).trim()
            val message = "\"${actual}\" should be equal to \"${expected}\""
            assertEquals(
                expected = expected,
                actual = actual,
                message = message
            )
        }
    }
}