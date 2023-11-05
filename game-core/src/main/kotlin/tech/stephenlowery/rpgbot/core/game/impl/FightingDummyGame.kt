package tech.stephenlowery.rpgbot.core.game.impl

import tech.stephenlowery.rpgbot.core.action.*
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.HealEffect
import tech.stephenlowery.rpgbot.core.character.NonPlayerCharacter
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.game.Game

private const val DUMMY_ID = 1L

private const val GAME_STARTED_MESSAGE = "You're in a Debug Dummy game. Have fun testing this."

private val dummyHealStrings = CharacterActionStrings(
    queuedText = "",
    actionText = "The dummy glows softly...",
    successText = "It heals itself for {value}!"
)

private val dummyHeal = CharacterAction(
    effect = HealEffect(20, 50, canCrit = false, canFail = false),
    displayName = "Debug Dummy Heal",
    description = "Just making it hard, but not impossible, to kill the dummy",
    identifier = "dummyheal",
    actionType = CharacterActionType.HEALING,
    targetingType = TargetingType.SELF,
    strings = dummyHealStrings
)

class FightingDummyGame(id: Long, initiatorId: Long, initiatorName: String) : Game(id, initiatorId, initiatorName) {

    private val dummy = NonPlayerCharacter("Debug Dummy", 1, healthValue = 1000) {
        QueuedCharacterAction(dummyHeal, this)
    }

    override fun numberOfPlayersIsInvalid(): Boolean = players.isEmpty()

    override fun isOver(): Boolean {
        return livingPlayers<PlayerCharacter>().isEmpty() || dummy.isDead()
    }

    override fun getGameEndedText(): String = when {
        livingPlayers<PlayerCharacter>().isEmpty() -> "The dummy wins?"
        else                                       -> "Congrats on killing a helpless, inanimate object. You win."
    }

    override fun startGame(): Collection<Pair<Long, String>> {
        players[DUMMY_ID] = dummy
        startGameStateAndPrepCharacters()
        return players.keys
            .filter { it != DUMMY_ID }
            .map { it to GAME_STARTED_MESSAGE }
    }

}