package tech.stephenlowery.rpgbot.models.action

import org.junit.Test
import tech.stephenlowery.rpgbot.models.action.action_effect.ActionEffect
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActionEffectTest {

    @Test
    fun `isExpired() returns true if the cycle given is greater than or equal to its duration`() {
        val testEffect = ActionEffect(duration = 5)
        (0..4).forEach { assertFalse { testEffect.isExpired(it) } }
        assertTrue(testEffect.isExpired(5))
    }

    @Test
    fun `isExpired() always returns false if the duration is -1`() {
        val testEffect = ActionEffect(duration = -1)
        (0..10).forEach { assertFalse { testEffect.isExpired(it) } }
    }
}