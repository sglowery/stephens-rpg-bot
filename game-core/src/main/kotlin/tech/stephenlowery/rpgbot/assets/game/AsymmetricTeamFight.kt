package tech.stephenlowery.rpgbot.assets.game

import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.Team
import tech.stephenlowery.rpgbot.core.game.Game

class AsymmetricTeamFight(
    id: Long,
    initiatorId: Long,
    initiatorName: String,
) : Game(
    id,
    initiatorId,
    initiatorName,
    description = "Asymmetric Team Fight"
) {

    private val teamPlayerMap = mutableMapOf<Team, Collection<Long>>()

    override fun startGame(): Collection<Pair<Long, String>> {
        super.startGame()
        assignPlayersToTeams()
        return teamPlayerMap.flatMap { (team, playerIds) ->
            val message = when (team) {
                Team.A -> "You're on Team A."
                Team.B -> "You're on Team B."
            }
            return@flatMap playerIds.map { playerId -> playerId to message }
        }
    }

    override fun isOver(): Boolean {
        val playerIdGroups = teamPlayerMap.values
        return playerIdGroups.map(::getRpgCharactersFromIds).filterNot(::characterGroupIsDead).size <= 1
    }

    override fun getGameEndedText(): String {
        val winningTeam = teamPlayerMap.map(::pairTeamToCharacters)
            .find { (_, players) -> !characterGroupIsDead(players) }?.first
        return when (winningTeam) {
            null -> "All teams died at the same time. Nobody wins lol"
            else -> "${winningTeam.displayName} wins!"
        }
    }

    private fun pairTeamToCharacters(entry: Map.Entry<Team, Collection<Long>>): Pair<Team, Collection<RPGCharacter>> {
        return entry.key to getRpgCharactersFromIds(entry.value)
    }

    private fun getCharactersNotOnTeam(livingCharacters: Collection<RPGCharacter>, characterTeam: Team): Collection<RPGCharacter> {
        return livingCharacters.filterNot(characterIsOnTeam(characterTeam))
    }

    private fun getCharactersOnTeam(livingCharacters: Collection<RPGCharacter>, characterTeam: Team): Collection<RPGCharacter> {
        return livingCharacters.filter(characterIsOnTeam(characterTeam))
    }

    private fun characterIsOnTeam(team: Team): (RPGCharacter) -> Boolean = { teamPlayerMap[team]!!.contains(it.id) }

    private fun getRpgCharactersFromIds(characterIds: Collection<Long>): Collection<RPGCharacter> = characterIds.mapNotNull(::getCharacterFromId)

    private fun characterGroupIsDead(rpgCharacters: Collection<RPGCharacter>) = rpgCharacters.all(RPGCharacter::isDead)

    private fun assignPlayersToTeams() {
        val numPlayers = numberOfPlayers()
        val shuffledPlayers = players.keys.shuffled()
        val numTeamB = determineEnemyTeamSize(numPlayers)
        val numTeamA = numPlayers - numTeamB
        teamPlayerMap[Team.A] = shuffledPlayers.subList(0, numTeamA)
        teamPlayerMap[Team.B] = shuffledPlayers.subList(numTeamA, numPlayers)
    }

    private fun determineEnemyTeamSize(numPlayers: Int): Int = (numPlayers / 2).coerceAtLeast(1)

}