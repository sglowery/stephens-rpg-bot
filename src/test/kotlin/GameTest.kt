import org.junit.Test
import tech.stephenlowery.rpgbot.models.Game
import tech.stephenlowery.rpgbot.models.RPGCharacter
import tech.stephenlowery.rpgbot.models.UserState
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameTest {

    val character1 = RPGCharacter(1L, "Stephen").apply { power.base = 50 }
    val character2 = RPGCharacter(2L, "Ashley")

    val game = Game(1L, character1)

    @Test
    fun `when new game is created, initiator is automatically added to list of players`() {
        assertEquals(expected = 1, actual = game.playerList.size)
    }

    @Test
    fun `a new player can be added`() {
        game.playerList.add(character2)
        assertEquals(expected = 2, actual = game.playerList.size)
    }

    @Test
    fun `when game starts, the state of all players is set to "choosing action"`() {
        game.playerList.add(character2)
        game.startGame()
        assertTrue(game.gameStarted)
        assertEquals(expected = UserState.CHOOSING_ACTION, actual = character1.characterState)
        assertEquals(expected = UserState.CHOOSING_ACTION, actual = character2.characterState)
    }

    @Test
    fun `waitingOn() correctly returns players who are still choosing an action or targets`() {
        character1.characterState = UserState.CHOOSING_ACTION
        character2.characterState = UserState.WAITING
        game.playerList.addAll(
            listOf(
                character2,
                RPGCharacter(3L, "").apply { this.characterState = UserState.CHOOSING_TARGETS }
            )
        )
        assertEquals(expected = 2, actual = game.waitingOn().size)
    }

    @Test
    fun `queueActionFromCharacter adds chosen action to actionQueue`() {
        assertEquals(expected = 0, actual = game.actionQueue.size)
        game.queueActionFromCharacter("action|attack", 1L)
        assertEquals(expected = 1, actual = game.actionQueue.size)
    }

    @Test
    fun `addTargetToQueuedCharacterAction correctly adds target to queued action`() {
        game.playerList.add(character2)
        game.queueActionFromCharacter("action|attack", 1L)
        game.addTargetToQueuedCharacterAction(1L, 2L)
        assertEquals(expected = character2, actual = game.actionQueue.first().targets.first())
    }
}