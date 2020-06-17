import org.junit.Test
import tech.stephenlowery.rpgbot.models.RPGCharacter
import tech.stephenlowery.rpgbot.models.UserState
import kotlin.test.assertEquals

class RPGCharacterTest {

    val character1 = RPGCharacter(1L, "Stephen").apply { power.base = 50 }

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