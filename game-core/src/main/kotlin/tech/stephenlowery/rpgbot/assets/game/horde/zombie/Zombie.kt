package tech.stephenlowery.rpgbot.assets.game.horde.zombie

import tech.stephenlowery.rpgbot.core.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.core.character.NonPlayerCharacter
import tech.stephenlowery.rpgbot.core.game.Game
import kotlin.random.Random

private fun zombieNameWithId(id: Long) = "Zombie $id"

private fun zombieHealth(wave: Int, numPlayers: Int) = ((wave - 1) * ZombieConstants.ZOMBIE_HEALTH_INCREASE_PER_WAVE) + (ZombieConstants.MIN_ZOMBIE_HEALTH..ZombieConstants.MAX_ZOMBIE_HEALTH).random() + numPlayers * ZombieConstants.ZOMBIE_HEALTH_INCREASE_PER_PLAYER

private fun zombiePower(wave: Int) = (ZombieConstants.MIN_ZOMBIE_STRENGTH..ZombieConstants.MAX_ZOMBIE_STRENGTH).random() + (wave - 1) * ZombieConstants.ZOMBIE_STRENGTH_INCREASE_PER_WAVE

private fun zombieDefense(wave: Int) = ZombieConstants.ZOMBIE_DEFENSE + (wave - 1) / 10

private fun zombiePrecision(wave: Int) = ZombieConstants.ZOMBIE_PRECISION + (wave - 1) / 10

private fun Game.getRandomLivingHumanPlayer() = this.getLiveHumanPlayers().values.random()

private fun basicZombieBehaviorDecider(wave: Int): NonPlayerCharacter.(Game) -> QueuedCharacterAction? = { game ->
    if (wave > 2 && Random.nextInt(100) < 5) {
        QueuedCharacterAction(ZombieDie, this, this)
    } else if (isActionOnCooldown(ZombieBite.identifier) || Random.nextInt(10) < 6) {
        QueuedCharacterAction(ZombieScratch, this, game.getRandomLivingHumanPlayer())
    } else {
        QueuedCharacterAction(ZombieBite, this, game.getRandomLivingHumanPlayer())
    }
}

fun getBasicZombie(id: Long, wave: Int, numPlayers: Int) =
    NonPlayerCharacter(
        id,
        name = zombieNameWithId(id),
        healthValue = zombieHealth(wave, numPlayers),
        powerValue = zombiePower(wave).toInt(),
        defenseValue = zombieDefense(wave),
        precisionValue = zombiePrecision(wave),
        actionDecidingBehavior = basicZombieBehaviorDecider(wave)
    )

