package tech.stephenlowery.rpgbot.core.game.impl

import tech.stephenlowery.rpgbot.core.action.*
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DamageHealthEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.HealEffect
import tech.stephenlowery.rpgbot.core.character.NonPlayerCharacter
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.game.Game
import kotlin.random.Random

private const val DUMMY_ID = 1L

private const val GAME_STARTED_MESSAGE = "You're in a Debug Dummy game. Have fun testing this."

private const val CHANCE_TO_BONK = 20

private const val DUMMY_HEALTH = 1000

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

private val dummyBonkStrings = CharacterActionStrings(
    queuedText = "",
    actionText = "The dummy wobbles menacingly at {target}...",
    successText = "It gives a solid bonk for {value} damage!",
    critText = "The gods channel their energy and help the dummy deliver a particularly brutal bonk, for {value} damage!",
    missedText = "The dummy falls over harmlessly."
)

private val dummyBonk = CharacterAction(
    effect = DamageHealthEffect(min = 12, max = 30),
    displayName = "Debug Dummy Bonk",
    description = "Sometimes the dummy strikes back.",
    identifier = "dummybonk",
    actionType = CharacterActionType.DAMAGE,
    targetingType = TargetingType.SINGLE_TARGET,
    strings = dummyBonkStrings,
)

class FightingDummyGame(id: Long, initiatorId: Long, initiatorName: String) : Game(id, initiatorId, initiatorName) {

    private val dummy = NonPlayerCharacter("Debug Dummy", 1, healthValue = DUMMY_HEALTH) {
        if(shouldBonk()) {
            QueuedCharacterAction(dummyBonk, this, livingPlayers<PlayerCharacter>().random())
        } else {
            QueuedCharacterAction(dummyHeal, this, this)
        }
    }

    private fun shouldBonk(): Boolean = dummy.getActualHealth() == DUMMY_HEALTH || Random.nextInt(100) < CHANCE_TO_BONK

    override fun numberOfPlayersIsValid(): Boolean = players.isNotEmpty()

    override fun isOver(): Boolean {
        return livingPlayers<PlayerCharacter>().isEmpty() || dummy.isDead()
    }

    override fun getGameEndedText(): String = when {
        livingPlayers<PlayerCharacter>().isEmpty() -> "The dummy wins lol. SAD!!"
        else                                       -> "Congrats on killing a (mostly) helpless, inanimate object. You win."
    }

    override fun startGame(): Collection<Pair<Long, String>> {
        players[DUMMY_ID] = dummy
        startGameStateAndPrepCharacters()
        return players.keys
            .filter { it != DUMMY_ID }
            .map { it to GAME_STARTED_MESSAGE }
    }

}