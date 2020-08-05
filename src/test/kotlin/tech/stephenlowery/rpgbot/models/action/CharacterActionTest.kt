package tech.stephenlowery.rpgbot.models.action

import org.junit.Test
import tech.stephenlowery.rpgbot.models.character.AttributeModifier
import tech.stephenlowery.rpgbot.models.character.RPGCharacter
import kotlin.test.assertEquals

class CharacterActionTest {

    class TestEffectAlwaysCrits : ActionEffect() {
        override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
            to.damage.additiveModifiers.add(AttributeModifier(value = 2.0, duration = -1))
            return listOf(EffectResult(source = from, target = to, value = 2, miss = false, crit = true))
        }
    }

    val character1 = RPGCharacter(1L, "Stephen").apply { health.base = 10.0 }
    val character2 = RPGCharacter(2L, "Ashley").apply { health.base = 10.0 }

    val testActionWithCritEffect = CharacterAction(
        listOf(TestEffectAlwaysCrits()),
        "Always Crit",
        "action|alwayscrit",
        "",
        TargetingType.SINGLE,
        CharacterActionStrings(
            actionText = "{source} is going to crit so hard on {target}",
            critText = "{source} crits so hard on {target}"
        )
    )



    @Test
    fun `getUnexpiredEffects() correctly filters out effects that have expired based on how long the QueuedCharacterAction has been in play`() {
        assertEquals(1, testActionWithCritEffect.getUnexpiredEffects(0).size)
        assertEquals(0, testActionWithCritEffect.getUnexpiredEffects(1).size)
    }

    @Test
    fun `resolveEffects() correctly applies unexpired effects to targets and returns a string formatted to show the action's actionText followed by the result`() {
        val results = testActionWithCritEffect.resolveEffects(from = character1, target = character2, cycle = 0)
        assertEquals(1, character2.damage.additiveModifiers.size)
        assertEquals(8, character2.getActualHealth())
        assertEquals(0, testActionWithCritEffect.getUnexpiredEffects(1).size)
        assertEquals("Stephen is going to crit so hard on Ashley\nStephen crits so hard on Ashley", results.trim())
    }

    @Test
    fun `ability with cooldown will be unavailable for a number of rounds equal to its cooldown after being used`() {

    }
}