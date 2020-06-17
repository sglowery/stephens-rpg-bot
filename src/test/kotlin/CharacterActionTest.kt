import org.junit.Test
import tech.stephenlowery.rpgbot.models.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CharacterActionTest {

    class TestEffectAlwaysCrits: ActionEffect() {
        override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
            to.damage.additiveModifiers.add(AttributeModifier(value = 2.0, duration = -1))
            return listOf(EffectResult(source = from, target = to, value = 2, miss = false, crit = true))
        }
    }

    class TestEffectAlwaysMisses: ActionEffect() {
        override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
            return listOf(EffectResult(source = from, target = to, miss = true))
        }
    }

    class TestEffectAlwaysSucceeds: ActionEffect() {
        override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
            to.damage.additiveModifiers.add(AttributeModifier(value = 1.0, duration = 1))
            return listOf(EffectResult(source = from, target = to, value = 1))
        }
    }

    val character1 = RPGCharacter(1L, "Stephen").apply { health.base = 10 }
    val character2 = RPGCharacter(2L, "Ashley").apply { health.base = 10 }

    val testActionWithCritEffect = CharacterAction(listOf(TestEffectAlwaysCrits()),
        "Always Crit",
        "action|alwayscrit",
        TargetingType.SINGLE,
        CharacterActionStrings(actionText = "{source} is going to crit so hard on {target}",
        critText = "{source} crits so hard on {target}")
    )

    @Test
    fun `getUnexpiredEffects() correctly filters out effects that have expired based on how long the QueuedCharacterAction has been in play`() {
        assertTrue {
            testActionWithCritEffect.getUnexpiredEffects(1).size == 0
            testActionWithCritEffect.getUnexpiredEffects(0).size > 0
        }
    }

    @Test
    fun `resolveEffects() correctly applies unexpired effects to targets and returns a string formatted to show the action's actionText followed by the result`() {
        val results = testActionWithCritEffect.resolveEffects(from = character1, targets = listOf(character2), cycle = 0)
        assertTrue {
            character2.damage.additiveModifiers.size == 1
            character2.getHealth() == 8
            testActionWithCritEffect.getUnexpiredEffects(1).size == 0
            results == "Stephen is going to crit so hard on Ashley\nStephen crits so hard on Ashley"
        }
    }

}