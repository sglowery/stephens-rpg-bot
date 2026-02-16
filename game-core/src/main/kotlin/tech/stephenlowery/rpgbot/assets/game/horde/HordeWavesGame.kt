package tech.stephenlowery.rpgbot.assets.game.horde

import tech.stephenlowery.rpgbot.assets.game.horde.zombie.ZombieConstants
import tech.stephenlowery.rpgbot.assets.game.horde.zombie.getBasicZombie
import tech.stephenlowery.rpgbot.core.action.*
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.NoOpEffect
import tech.stephenlowery.rpgbot.core.character.CharacterState
import tech.stephenlowery.rpgbot.core.character.NonPlayerCharacter
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.equipment.EquipmentAction
import tech.stephenlowery.rpgbot.core.equipment.toBasicEquipmentAction
import tech.stephenlowery.rpgbot.core.game.Game
import tech.stephenlowery.rpgbot.core.game.PlayerGameStats
import tech.stephenlowery.rpgbot.assets.game.horde.zombie.getMutantZombie
import kotlin.math.floor
import kotlin.math.pow

const val HORDE_GAME_DESCRIPTION = "Fight against increasing hordes"
const val MUTANT_ZOMBIE_SPAWN_RATE = 3

private fun numZombies(wave: Int): Int = (1..5).random() + wave / 5

class HordeWavesGame(
    id: Long,
    initiatorId: Long,
    initiatorName: String,
) : Game(
    id,
    initiatorId,
    initiatorName,
    description = HORDE_GAME_DESCRIPTION
) {

    private var zombieCounter = 0
    private var zombieWave = 1
    private var mutantZombieCounter = 0
    private var mutantZombieEncounter = 0

    private var shouldSpawnZombies = false
    private var shouldSpawnMutantZombie = false

    override fun startGame(): Collection<Pair<Long, String>> {
        val startingZombiesAmount = ZombieConstants.STARTING_NUM_ZOMBIES
        val startingZombies = (1L..startingZombiesAmount).associateWith {
            getBasicZombie(
                it,
                zombieWave,
                getLiveHumanPlayers().size
            )
        }
        players.putAll(startingZombies)
        zombieCounter += startingZombies.size
        return super.startGame()
    }

    override fun getEnemiesForCharacter(character: PlayerCharacter, targetingType: TargetingType): Collection<RPGCharacter> {
        return getLivingZombies()
    }

    override fun resolveActionsAndGetResults(): String {
        val results = mutableListOf(super.resolveActionsAndGetResults())
        players.values.filterIsInstance<NonPlayerCharacter>()
            .filter { !it.isAlive() && it.characterState != CharacterState.DEAD }
            .forEach { it.characterState = CharacterState.DEAD }
        if (shouldSpawnZombies) {
            zombieWave++
            val newBasicZombies = spawnBasicZombies()
            if (shouldSpawnMutantZombie) {
                val newMutantZombies = spawnMutantZombies()
                mutantZombieEncounter++
                if (newMutantZombies == 1) {
                    results.add("A mutant zombie has spawned!")
                } else {
                    results.add("$newMutantZombies mutant zombies have spawned!")
                }
                results.add("Additionally, $newBasicZombies zombies come to life to defend the mutants.")
            } else {
                results.add("$newBasicZombies more zombies have arisen!")
            }
        } else if (getLivingZombies().isEmpty() && !shouldSpawnZombies) {
            shouldSpawnZombies = true
            if (zombieWave > 1 && zombieWave % MUTANT_ZOMBIE_SPAWN_RATE == 0 && !shouldSpawnMutantZombie) {
                shouldSpawnMutantZombie = true
            }
            results.add("Wave $zombieWave complete! More will spawn at the end of next turn.")
        }
        return results.joinToString("\n\n")
    }

    override fun characterShouldBeIncludedInPostGameStats(character: PlayerGameStats): Boolean {
        return !character.isNpc
    }

    override fun isOver(): Boolean {
        return hasStarted && getLiveHumanPlayers().isEmpty()
    }

    override fun numberOfPlayersIsValid(): Boolean {
        return players.isNotEmpty()
    }

    override fun getGameEndedText(): String {
        return "You made it through $zombieWave wave(s)!"
    }

    override fun getGameGrantedActions(): Collection<EquipmentAction> {
        return listOf(
            toBasicEquipmentAction(
                CharacterAction(
                    displayName = "Do Nothing",
                    identifier = "action|donothing",
                    description = "Do nothing.",
                    actionType = CharacterActionType.OTHER,
                    targetingType = TargetingType.SELF,
                    targetIntent = TargetIntent.FRIENDLY,
                    effect = NoOpEffect(),
                    strings = CharacterActionStrings(actionText = "{source} stands around doing nothing.")
                )
            )
        )
    }

    private fun getLivingZombies(): Collection<NonPlayerCharacter> = livingPlayers().filterIsInstance<NonPlayerCharacter>()

    private fun spawnBasicZombies(): Int {
        val numNewZombies = numZombies(zombieWave)
        val newZombies = (1L..numNewZombies).associate { (it + zombieCounter) to getBasicZombie(
            it + zombieCounter,
            zombieWave,
            getLiveHumanPlayers().size
        )
        }
        zombieCounter += newZombies.size
        players.putAll(newZombies)
        shouldSpawnZombies = false
        return numNewZombies
    }

    private fun spawnMutantZombies(): Int {
        val numMutantZombies = 2.0.pow(mutantZombieEncounter).toInt()
        val newMutantZombies =
            (1L..numMutantZombies).associate { (it + mutantZombieCounter) to getMutantZombie(it + mutantZombieCounter, mutantZombieEncounter, getLiveHumanPlayers().size) }
        mutantZombieCounter += newMutantZombies.size
        players.putAll(newMutantZombies)
        shouldSpawnMutantZombie = false
        return newMutantZombies.size
    }

}