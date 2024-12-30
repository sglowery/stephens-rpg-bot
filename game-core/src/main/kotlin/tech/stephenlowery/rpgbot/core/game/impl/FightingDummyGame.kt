package tech.stephenlowery.rpgbot.core.game.impl

import tech.stephenlowery.rpgbot.core.action.*
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DamageHealthEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.HealEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.DelayedEffect
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

private const val CHANCE_TO_BONK = 80
private const val CHANCE_TO_HEAVY_BONK = 100

private const val DUMMY_HEALTH = 1200
private const val DUMMY_HEALTH_PLAYER_SCALAR = 800
private const val DUMMY_HEALTH_PERCENT_BERSERK_THRESHOLD = 50

private val dummyHealStrings = CharacterActionStrings(
    actionText = "The dummy glows softly...",
    successText = "It magically stitches itself up!"
)
private val dummyHeal
    get() = CharacterAction(
        effect = HealEffect(40, 50, canCrit = false, canMiss = false),
        displayName = "Debug Dummy Heal",
        description = "Just making it hard, but not impossible, to kill the dummy",
        identifier = "dummyheal",
        actionType = CharacterActionType.HEALING,
        targetingType = TargetingType.SELF,
        targetIntent = TargetIntent.FRIENDLY,
        strings = dummyHealStrings
    )

private val dummyBonkStrings = CharacterActionStrings(
    actionText = "The dummy wobbles menacingly at {target}...",
    successText = "It gives a solid bonk!",
    missedText = "The dummy falls over harmlessly.",
    critText = "The gods channel their energy and help the dummy deliver a particularly brutal bonk!"
)
private val dummyBonk
    get() = CharacterAction(
        effect = DamageHealthEffect(min = 22, max = 36),
        displayName = "Debug Dummy Bonk",
        description = "Sometimes the dummy strikes back.",
        identifier = "dummybonk",
        actionType = CharacterActionType.DAMAGE,
        targetingType = TargetingType.SINGLE_TARGET,
        targetIntent = TargetIntent.HOSTILE,
        strings = dummyBonkStrings,
    )

private val dummyBerserkStrings = CharacterActionStrings(
    actionText = "The dummy goes berserk!",
)
private val dummyBerserk
    get() = CharacterAction(
        effect = MultiEffect(
            StatModEffect(
                10,
                statGetter = RPGCharacter::power,
                attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
                modifierName = "Dummy Berserk",
            ),
            StatModEffect(
                100,
                statGetter = RPGCharacter::power,
                attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
                modifierName = "Dummy Berserk",
            ),
            StatModEffect(
                value = 5,
                statGetter = RPGCharacter::defense,
                attributeModifierType = AttributeModifierType.ADDITIVE,
                modifierName = "Dummy Berserk",
            ),
            StatModEffect(
                value = 10,
                statGetter = RPGCharacter::defense,
                attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
                modifierName = "Dummy Berserk",
            ),
            StatModEffect(
                value = 25,
                statGetter = RPGCharacter::precision,
                attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
                modifierName = "Dummy Berserk",
            ),
            StatModEffect(
                value = 50,
                statGetter = RPGCharacter::criticalEffectScalar,
                attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
                modifierName = "Dummy Berserk",
            )
        ),
        displayName = "Debug Dummy Berserk",
        description = "Dummy goes wild!",
        identifier = "dummyberserk",
        cooldown = 999,
        actionType = CharacterActionType.BUFF,
        targetingType = TargetingType.SELF,
        targetIntent = TargetIntent.FRIENDLY,
        strings = dummyBerserkStrings
    )

val dummyBigAttackStrings = CharacterActionStrings(
    actionText = "{source} rotates toward {target} and starts glowing.",
    successText = "It bonks them extra hard!",
    missedText = "A small pop is heard and it abruptly stops glowing."
)
val dummyBigAttack
    get() = CharacterAction(
        displayName = "Big Attack",
        description = "Big attack go owie",
        identifier = "dummybigattack",
        cooldown = 4,
        actionType = CharacterActionType.DAMAGE,
        targetingType = TargetingType.SINGLE_TARGET,
        targetIntent = TargetIntent.HOSTILE,
        effect = DelayedEffect(
            delay = 1,
            delayedActionEffect = DamageHealthEffect(min = 51, max = 60),
            occupySource = true
        ),
        strings = dummyBigAttackStrings
    )

private val boscoPelletGunStrings = CharacterActionStrings(
    actionText = "{source} aims its pellet gun at {target} and fires a single shot!",
    successText = "They get pelted in the head. Ow.",
    missedText = "The pellet misses and lands harmlessly on the ground."
)

private val boscoPelletGunAttack
    get() = CharacterAction(
        effect = DamageHealthEffect(9, 13),
        displayName = "Pellet Gun Shot",
        description = "Fires the pellet gun. How are you seeing this anyway?",
        identifier = "boscopelletgun",
        actionType = CharacterActionType.DAMAGE,
        targetingType = TargetingType.SINGLE_TARGET,
        targetIntent = TargetIntent.HOSTILE,
        strings = boscoPelletGunStrings
    )

private const val boscoHealModifierName = "Bosco Invigoration"
private val boscoHealStrings = CharacterActionStrings(
    actionText = "Bosco aims a healing dart at {target} and shoots...",
    successText = "The dart flies true and injects its invigorating goo!",
    missedText = "The dart misses and embeds itself in the ground. Nature is healing.",
)
private val boscoHealDartAction
    get() = CharacterAction(
        effect = MultiEffect(
            HealEffect(
                min = 28,
                max = 37,
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
        targetIntent = TargetIntent.FRIENDLY,
        strings = boscoHealStrings,
    )

class FightingDummyGame(
    id: Long,
    initiatorId: Long,
    initiatorName: String,
) : Game(
    id,
    initiatorId,
    initiatorName,
    description = "Fight against a training dummy, with Bosco to help you out in case you can't handle it."
) {

    private var hasGoneBerserk = false

    private val dummy = NonPlayerCharacter("Debug Dummy", 1, healthValue = DUMMY_HEALTH, defenseValue = 15) {
        if (this.getHealthPercent() <= DUMMY_HEALTH_PERCENT_BERSERK_THRESHOLD && !hasGoneBerserk) {
            hasGoneBerserk = true
            QueuedCharacterAction(dummyBerserk, this, this)
        } else if (shouldBonk(getHumanPlayers().size)) {
            val target = listOf(chooseTargetFromMostDamageDone(), chooseTargetFromMostHealingDone()).randomOrNull() ?: livingPlayers<PlayerCharacter>().random()
            if (shouldHeavyBonk()) {
                QueuedCharacterAction(dummyBigAttack, this, target)
            } else {
                QueuedCharacterAction(dummyBonk, this, target)
            }
        } else {
            QueuedCharacterAction(dummyHeal, this, this)
        }
    }

    override fun getEnemiesForCharacter(character: PlayerCharacter, targetingType: TargetingType): Collection<RPGCharacter> {
        return listOf(dummy)
    }

    private fun shouldHeavyBonk(): Boolean {
        return !dummy.isActionOnCooldown(dummyBigAttack.identifier) && Random.nextInt(100) < CHANCE_TO_HEAVY_BONK
    }

    private fun chooseTargetFromMostDamageDone(): PlayerCharacter? {
        return resultsHistory
            .flatten()
            .flatMap(QueuedCharacterActionResolvedResults::effectResults)
            .filter { it.target == dummy && it.source is PlayerCharacter && it.source.isAlive() }
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
                it.source is PlayerCharacter && it.source.isAlive() &&
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

    private val bosco = NonPlayerCharacter("Bosco", 2, healthValue = 1, powerValue = 1, defenseValue = 1, precisionValue = 1) {
        val target = livingPlayers<PlayerCharacter>().filter { characterCanBeChosenForHeal(it) }.randomOrNull()
        val isOffCooldown = !this.isActionOnCooldown(boscoHealDartAction.identifier)
        when (target != null && isOffCooldown) {
            true  -> QueuedCharacterAction(boscoHealDartAction, this, target)
            false -> QueuedCharacterAction(boscoPelletGunAttack, this, dummy)
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
        dummy.setHealth(dummyBaseHealthForPlayers(players.size))
        players[DUMMY_ID] = dummy
        players[BOSCO_ID] = bosco
        return super.startGame()
    }

    private fun characterCanBeChosenForHeal(player: PlayerCharacter): Boolean {
        return !characterHasActiveInvigoration(player) && player.getHealthPercent() < 100
    }

    private fun characterHasActiveInvigoration(player: PlayerCharacter): Boolean {
        return player.damageTakenScalar.hasNamedModifier(boscoHealModifierName)
    }

    private fun dummyBaseHealthForPlayers(players: Int): Int = DUMMY_HEALTH + DUMMY_HEALTH_PLAYER_SCALAR * (getHumanPlayers().size - 1)

    private fun shouldBonk(players: Int): Boolean =
        dummy.getActualHealth() == dummyBaseHealthForPlayers(players) ||
                Random.nextInt(100) < CHANCE_TO_BONK

}