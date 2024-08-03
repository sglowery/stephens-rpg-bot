package tech.stephenlowery.rpgbot.core.game.impl

import tech.stephenlowery.rpgbot.core.action.*
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DamageHealthEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.HealEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.NoOpEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.MultiEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.StatModEffect
import tech.stephenlowery.rpgbot.core.character.NonPlayerCharacter
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType
import tech.stephenlowery.rpgbot.core.game.Game
import kotlin.random.Random

private const val DUMMY_ID = 1L
private const val BOSCO_ID = 2L

private const val GAME_STARTED_MESSAGE = "You're in a Debug Dummy game. Have fun testing this."

private const val CHANCE_TO_BONK = 75

private const val DUMMY_HEALTH = 1200
private const val DUMMY_HEALTH_PLAYER_SCALAR = 800

private val dummyHealStrings = CharacterActionStrings(
    queuedText = "",
    actionText = "The dummy glows softly...",
    successText = "It heals itself for {value}!"
)
private val dummyHeal = CharacterAction(
    effect = HealEffect(40, 50, canCrit = false, canMiss = false),
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
    effect = DamageHealthEffect(min = 22, max = 36),
    displayName = "Debug Dummy Bonk",
    description = "Sometimes the dummy strikes back.",
    identifier = "dummybonk",
    actionType = CharacterActionType.DAMAGE,
    targetingType = TargetingType.SINGLE_TARGET,
    strings = dummyBonkStrings,
)

val boscoNoOpStrings = CharacterActionStrings(
    queuedText = "",
    actionText = "{source} floats around in the air, unable to do anything."
)

private val boscoNoOp = CharacterAction(
    effect = NoOpEffect(),
    displayName = "Do Nothing",
    description = "Does nothing. How are you seeing this anyway?",
    identifier = "bosconoop",
    actionType = CharacterActionType.OTHER,
    targetingType = TargetingType.SELF,
    strings = boscoNoOpStrings
)

private const val boscoHealModifierName = "Bosco Invigoration"
private val boscoHealStrings = CharacterActionStrings(
    queuedText = "",
    actionText = "Bosco aims a healing dart at {target} and shoots...",
    successText = "The dart flies true and injects its invigorating goo!",
    missedText = "The dart misses and embeds itself in the ground. Nature is healing.",
)
private val boscoHealDartAction = CharacterAction(
    effect = MultiEffect(
        HealEffect(
            min = 20,
            max = 30,
            canCrit = false,
            canMiss = true,
        ),
        StatModEffect(
            value = -15,
            modDuration = 5,
            statGetter = RPGCharacter::damageTakenScalar,
            attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
            modifierName = boscoHealModifierName,
        )
    ),
    displayName = "Bosco Heal Dart",
    description = "This dart does some healing and reduces damage taken for a few turns",
    identifier = "boscohealdart",
    cooldown = 3,
    actionType = CharacterActionType.HEALING,
    targetingType = TargetingType.SINGLE_TARGET,
    strings = boscoHealStrings,
)

class FightingDummyGame(id: Long, initiatorId: Long, initiatorName: String) : Game(id, initiatorId, initiatorName) {

    private val dummy = NonPlayerCharacter("Debug Dummy", 1, healthValue = DUMMY_HEALTH) {
        if (shouldBonk()) {
            val target = listOf(chooseTargetFromMostDamageDone(), chooseTargetFromMostHealingDone())
                .randomOrNull()
                ?: livingPlayers<PlayerCharacter>().random()
            QueuedCharacterAction(dummyBonk, this, target)
        } else {
            QueuedCharacterAction(dummyHeal, this, this)
        }
    }

    private fun chooseTargetFromMostDamageDone(): PlayerCharacter? {
        return resultsHistory
            .flatten()
            .flatMap(QueuedCharacterActionResolvedResults::effectResults)
            .filter { it.target == dummy && it.source is PlayerCharacter }
            .fold(mutableMapOf<PlayerCharacter, Int>()) { characterDamageMap, effect ->
                return@fold characterDamageMap.apply {
                    this[effect.source as PlayerCharacter] = effect.value + (this[effect.source] ?: 0)
                }
            }
            .maxByOrNull { it.value }
            ?.key
    }

    private fun chooseTargetFromMostHealingDone(): PlayerCharacter? {
        return resultsHistory
            .flatten()
            .flatMap(QueuedCharacterActionResolvedResults::effectResults)
            .filter {
                it.source is PlayerCharacter &&
                        (it.actionType == CharacterActionType.HEALING
                                || (it.target == dummy && it.actionType == CharacterActionType.DAMAGE_HEAL))
            }
            .fold(mutableMapOf<PlayerCharacter, Int>()) { characterDamageMap, effect ->
                val effectHealingValue = when (effect.actionType) {
                    CharacterActionType.HEALING -> effect.value
                    else                        -> effect.other?.toInt() ?: 0
                }
                return@fold characterDamageMap.apply {
                    this[effect.source as PlayerCharacter] = effectHealingValue + (this[effect.source] ?: 0)
                }
            }
            .maxByOrNull { it.value }
            ?.key
    }

    private val bosco = NonPlayerCharacter("Bosco", 2) {
        val target = livingPlayers<PlayerCharacter>().filter { !characterHasActiveInvigoration(it) }.randomOrNull()
        val isOffCooldown = !this.isActionOnCooldown(boscoHealDartAction.identifier)
        when (target != null && isOffCooldown) {
            true  -> QueuedCharacterAction(boscoHealDartAction, this, target)
            false -> QueuedCharacterAction(boscoNoOp, this, this)
        }
    }

    override fun getTargetsForCharacter(character: PlayerCharacter): Collection<RPGCharacter> {
        return when (character.queuedAction?.action?.actionType) {
            CharacterActionType.DAMAGE -> listOf(dummy)
            else                       -> getTargetsForPlayerAction(character)
        }
    }

    override fun isOver(): Boolean {
        return livingPlayers<PlayerCharacter>().isEmpty() || dummy.isDead()
    }

    override fun numberOfPlayersIsValid(): Boolean = players.isNotEmpty()

    override fun getGameEndedText(): String = when {
        livingPlayers<PlayerCharacter>().isEmpty() -> "The dummy wins lol. SAD!!"
        else                                       -> "Congrats on killing a (mostly) helpless, inanimate object. You win."
    }

    override fun startGame(): Collection<Pair<Long, String>> {
        dummy.setHealth(DUMMY_HEALTH + DUMMY_HEALTH_PLAYER_SCALAR * getHumanPlayers().size)
        players[DUMMY_ID] = dummy
        players[BOSCO_ID] = bosco
        startGameStateAndPrepCharacters()
        return players.keys
            .filter { it != DUMMY_ID && it != BOSCO_ID }
            .map { it to GAME_STARTED_MESSAGE }
    }

    private fun characterHasActiveInvigoration(it: PlayerCharacter): Boolean {
        return it.damageTakenScalar.hasNamedModifier(boscoHealModifierName)
    }

    private fun getTargetsForPlayerAction(character: PlayerCharacter): Collection<RPGCharacter> {
        val livingPlayers = getHumanPlayers().values.filter { it.isAlive() }
        return when (character.queuedAction?.action?.targetingType) {
            TargetingType.SINGLE_TARGET -> livingPlayers.filter { it.id != character.id }
            else                        -> livingPlayers
        }
    }

    private fun shouldBonk(): Boolean = dummy.getActualHealth() == DUMMY_HEALTH || Random.nextInt(100) < CHANCE_TO_BONK

}