package tech.stephenlowery.rpgbot.core.game

import spock.lang.Specification

class GameManagerTest extends Specification {

    def setup() {
        GameManager.INSTANCE.games.clear()
        GameManager.INSTANCE.userToGameMap.clear()
    }

    def 'findGame takes an id and returns a nullable game'() {
        given:
        GameManager.INSTANCE.createGame(1L, 2L, 'name')

        expect:
        GameManager.INSTANCE.findGame(gameId) == expected

        where:
        gameId || expected
        1L     || new Game(1L, 2L, 'name', '')
        2L     || null
    }

    def 'findGameContainingCharacter returns a game that contains a player with a given id'() {
        given:
        GameManager.INSTANCE.createGame(1L, 2L, 'initiator')
        GameManager.INSTANCE.addPlayerToGame(1L, 3L, 'name')

        GameManager.INSTANCE.createGame(4L, 5L, 'other initiator')

        expect:
        GameManager.INSTANCE.findGameContainingCharacter(playerId) == expected

        where:
        playerId || expected
        2L       || new Game(1L, 2L, 'initiator', '')
        3L       || new Game(1L, 2L, 'initiator', '')
        4L       || null
        5L       || new Game(4L, 5L, 'other initiator', '')

    }

    def 'createGame creates game and adds player to user-to-game map'() {
        given:
        GameManager.INSTANCE.createGame(1L, 2L, 'name')

        expect:
        GameManager.INSTANCE.games == [1L: new Game(1L, 2L, 'name', '')]
        GameManager.INSTANCE.userToGameMap == [2L: 1L]
    }

    def 'addPlayerToGameAndGetReplyText adds player to game and user-to-game map'() {
        given:
        GameManager.INSTANCE.createGame(1L, 2L, 'name')
        GameManager.INSTANCE.addPlayerToGame(1L, 3L, 'name2')

        expect:
        GameManager.INSTANCE.games.values().first().players.keySet() == [2L, 3L] as Set<Long>
        GameManager.INSTANCE.userToGameMap == [2L: 1L, 3L: 1L]
    }

//    def 'CancelGame'() {
//    }
//
//    def 'ChooseActionForCharacter'() {
//    }
//
//    def 'FindCharacter'() {
//    }
}
