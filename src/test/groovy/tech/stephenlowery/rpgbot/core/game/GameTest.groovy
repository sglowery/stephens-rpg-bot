package tech.stephenlowery.rpgbot.core.game

import spock.lang.Specification
import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.action.CharacterActionType
import tech.stephenlowery.rpgbot.core.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.UserState
import tech.stephenlowery.rpgbot.core.game.Game
import tech.stephenlowery.rpgbot.utils.TestUtils

class GameTest extends Specification {
    def "when new game is created, initiator is automatically added to list of players"() {
        when:
        def game = new Game(1L, GroovyMock(PlayerCharacter))

        then:
        game.playerList.size() == 1
    }

    def "can add players to game after creation"() {
        when:
        Game game = new Game(1L, GroovyMock(PlayerCharacter)).tap { it.playerList.add(GroovyMock(PlayerCharacter)) }

        then:
        game.playerList.size() == 2
    }

    def "when game starts, state of all players is set to 'choosing action'"() {
        given:
        def characters = (1..5).collect { new PlayerCharacter(it.toLong(), "") }
        def game = new Game(1L, characters.get(0))
        game.addPlayers(characters.subList(1, 4))

        when:
        game.startGame()

        then:
        game.playerList.each { it.characterState == UserState.CHOOSING_ACTION }
    }

    def "waitingOn() correctly returns players who are still choosing an action or targets"() {
        given:
        def game = new Game(1L, TestUtils.getTestCharacter().tap { it.characterState = UserState.WAITING })
        def userStates = [UserState.WAITING, UserState.WAITING, UserState.CHOOSING_ACTION, UserState.CHOOSING_TARGETS]
        def characters = userStates.collect { userState -> TestUtils.getTestCharacter().tap { it.characterState = userState } }
        game.addPlayers(characters)

        expect:
        game.waitingOn().size() == 2
    }

    def "queueActionFromCharacter adds chosen action to action queue"() {
        given:
        def game = new Game(1L, TestUtils.getTestCharacter())
        game.addPlayer(TestUtils.getTestCharacter())
        def userIDs = game.playerList.collect { it.id }

        when:
        game.queueActionFromCharacter("action|attack", userIDs.first())

        then:
        game.actionQueue.size() == 1
    }

    def "addTargetToQueuedCharacterAction correctly adds target to queued action"() {
        given:
        def game = new Game(1L, TestUtils.getTestCharacter())
        game.addPlayer(TestUtils.getTestCharacter())
        def userIDs = game.playerList.collect { it.id }
        game.queueActionFromCharacter("action|attack", userIDs.first())

        when:
        game.addTargetToQueuedCharacterAction(userIDs.first(), userIDs.last())

        then:
        game.actionQueue.first().target == game.playerList.last()
    }

    def "playerIsInGame returns true if a character with the given ID exists in the game"() {
        given:
        def game = new Game(1L, TestUtils.getTestCharacter(1L))

        expect:
        assert shouldBeInGame == game.containsPlayerWithID(id)

        where:
        id || shouldBeInGame
        1L || true
        2L || false
    }

    def "getSortedActionsToResolve returns a list of queued character actions where defensive actions come first"() {
        given:
        def defensiveAction = TestUtils.getTestCharacterAction(CharacterActionType.DEFENSIVE)
        def defensiveQueuedAction = new QueuedCharacterAction(defensiveAction, GroovyMock(RPGCharacter))
        def offensiveAction = TestUtils.getTestCharacterAction(CharacterActionType.DAMAGE)
        def offensiveQueuedAction = new QueuedCharacterAction(offensiveAction, GroovyMock(RPGCharacter))
        def actionsList = [offensiveQueuedAction, offensiveQueuedAction, offensiveQueuedAction, defensiveQueuedAction, offensiveQueuedAction, defensiveQueuedAction]
        def game = new Game(1L, GroovyMock(PlayerCharacter)).tap { actionQueue.addAll(actionsList) }

        when:
        def sortedList = game.getSortedActionsToResolve()

        then:
        sortedList.take(2).forEach {assert it.action.actionType == CharacterActionType.DEFENSIVE }

        and:
        sortedList.takeRight(4).forEach {assert it.action.actionType == CharacterActionType.DAMAGE }
    }
}
