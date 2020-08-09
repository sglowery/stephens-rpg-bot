package tech.stephenlowery.rpgbot.models.character

import org.junit.Test
import tech.stephenlowery.rpgbot.models.action.*
import kotlin.test.assertEquals

class RPGCharacterTest {

    class TestEffectNeverMisses : ActionEffect() {
        override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): EffectResult {
            to.damage.additiveModifiers.add(AttributeModifier(value = 2.0, duration = -1))
            return EffectResult(source = from, target = to, value = 2, miss = false)
        }
    }

    val character1 = RPGCharacter(1L, "Stephen").apply { power.base = 10.0 }

    val testActionWithCooldown = CharacterAction(
        TestEffectNeverMisses(),
        "Always Crit",
        "action|alwayscrit",
        "",
        TargetingType.SINGLE,
        CharacterActionStrings(
            actionText = "{source} is going to crit so hard on {target}",
            critText = "{source} crits so hard on {target}"
        ),
        cooldown = 5
    )

    @Test
    fun `chooseAction() changes character state based on chosen action's targeting type`() {

        class TestCase(val callbackText: String, val expectedState: UserState)

        listOf(
            TestCase("action|attack", UserState.CHOOSING_TARGETS),
            TestCase("action|defend", UserState.WAITING)
        ).forEach {
            character1.chooseAction(it.callbackText)
            assertEquals(expected = character1.characterState, actual = it.expectedState)
        }
    }
}