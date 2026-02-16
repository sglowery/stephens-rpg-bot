package tech.stephenlowery.rpgbot.assets.game.horde.zombie

import tech.stephenlowery.rpgbot.core.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.core.character.NonPlayerCharacter
import tech.stephenlowery.rpgbot.core.game.Game
import kotlin.random.Random

private fun mutantZombieNameWithId(id: Long) = "Mutant Zombie $id"

private fun mutantZombieHealth(wave: Int, numPlayers: Int) =
    ((wave - 1) * MutantZombieConstants.HEALTH_INCREASE_PER_ENCOUNTER) +
        (MutantZombieConstants.MIN_HEALTH..MutantZombieConstants.MAX_HEALTH).random() +
        (numPlayers - 1) * MutantZombieConstants.HEALTH_INCREASE_PER_PLAYER

private fun mutantZombiePower(wave: Int, numPlayers: Int) =
    (MutantZombieConstants.MIN_STRENGTH..MutantZombieConstants.MAX_STRENGTH).random() +
        (wave - 1) * MutantZombieConstants.STRENGTH_INCREASE_PER_ENCOUNTER +
        numPlayers - 1

private fun mutantZombieDefense(wave: Int, numPlayers: Int) = MutantZombieConstants.DEFENSE + (wave - 1) + numPlayers - 1

private fun mutantZombiePrecision(wave: Int, numPlayers: Int) = MutantZombieConstants.PRECISION + wave + numPlayers - 1

private fun Game.getRandomLivingHumanPlayer() = this.getLiveHumanPlayers().values.random()

private val actionDecidingBehavior: NonPlayerCharacter.(Game) -> QueuedCharacterAction? = { game ->
    if (isActionOnCooldown(ZombieBite.identifier) || Random.nextInt(10) < 6) {
        if (Random.nextInt(10) <= 2 && !isActionOnCooldown(ZombieVomit.identifier)) {
            QueuedCharacterAction(ZombieVomit, this, game.getRandomLivingHumanPlayer())
        } else {
            QueuedCharacterAction(ZombieScratch, this, game.getRandomLivingHumanPlayer())
        }
    } else {
        QueuedCharacterAction(ZombieBite, this, game.getRandomLivingHumanPlayer())
    }
}

fun getMutantZombie(id: Long, encounter: Int, numPlayers: Int) =
    NonPlayerCharacter(
        id = id,
        name = mutantZombieNameWithId(id),
        healthValue = mutantZombieHealth(encounter, numPlayers),
        powerValue = mutantZombiePower(encounter, numPlayers),
        defenseValue = mutantZombieDefense(encounter, numPlayers),
        precisionValue = mutantZombiePrecision(encounter, numPlayers),
        actionDecidingBehavior = actionDecidingBehavior
    )