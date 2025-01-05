package tech.stephenlowery.rpgbot.core.game


import spock.lang.Specification
import tech.stephenlowery.rpgbot.core.action.CharacterActionType
import tech.stephenlowery.rpgbot.core.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.core.character.PlayerCharacter
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.CharacterState
import tech.stephenlowery.rpgbot.utils.TestUtils

class GameTest extends Specification {

    def "when new game is created, initiator is automatically added to list of players"() {
        when:
        def game = new Game(1L, 2L, "initiator", '')

        then:
        game.players.size() == 1

        and:
        game.players.values().first() == new PlayerCharacter(2L, "initiator")
    }

    def "can add players to game after creation"() {
        given:
        def game = new Game(1L, 2L, 'initiator', '')

        when:
        game.addPlayerToGame(3L, 'name')

        then:
        game.numberOfPlayers() == 2

        and:
        game.players.values().first() == new PlayerCharacter(2L, 'initiator')

        and:
        game.players.values().last() == new PlayerCharacter(3L, 'name')
    }

    def "when game starts, state of all players is set to 'choosing action'"() {
        given:
        def characters = (1..5).collect { new PlayerCharacter(it.toLong(), "") }
        def game = new Game(1L, characters.get(0).id, 'initiator', '')
        characters.forEach(game.&addCharacter)

        when:
        game.startGame()

        then:
        game.players.values().each { it.characterState == CharacterState.CHOOSING_ACTION }
    }

    def "waitingOn() correctly returns players who are still choosing an action or targets"() {
        given:
        def character = TestUtils.getTestCharacter().tap { it.characterState = CharacterState.WAITING }
        def game = new Game(1L, character.id, character.name, '')
        def userStates = [CharacterState.WAITING, CharacterState.WAITING, CharacterState.CHOOSING_ACTION, CharacterState.CHOOSING_TARGETS]
        def characters = userStates.collect { userState -> TestUtils.getTestCharacter().tap { it.characterState = userState } }
        characters.forEach(game.&addCharacter)

        expect:
        game.waitingOn().size() == 2
    }

    // TODO update these tests once actions are set up differently (may need to just mock out getAvailableActions())
    def "queueActionFromCharacter adds chosen action to action queue"() {
        given:
        def character = TestUtils.getTestCharacter()
        def id = character.id
        def game = new Game(1L, id, character.name, '')
        game.addCharacter(character)

        when:
        game.queueActionFromCharacter("action|attack", id)

        then:
        game.actionQueue.size() == 1
    }

    def "addTargetToQueuedCharacterAction correctly adds target to queued action"() {
        given:
        def characters = (1..4).collect { TestUtils.getTestCharacter() }
        def firstCharacter = characters.first()
        def firstCharacterId = firstCharacter.id
        def targetCharacterId = characters[2].id
        def game = new Game(1L, firstCharacterId, firstCharacter.name, '')
        characters.forEach(game.&addCharacter)
        game.queueActionFromCharacter("action|attack", firstCharacterId)

        when:
        game.addTargetToQueuedCharacterAction(firstCharacterId, targetCharacterId)

        then:
        game.actionQueue.first().target == game.players[targetCharacterId]
    }

    def "containsPlayerWithID returns true if a character with the given ID exists in the game"() {
        given:
        def game = new Game(1L, TestUtils.getTestCharacter(1L).id, 'name', '')

        expect:
        shouldBeInGame == game.containsPlayerWithID(id)

        where:
        id || shouldBeInGame
        1L || true
        2L || false
    }

    def "getSortedActionsToResolve returns a list of queued character actions where defensive actions come first"() {
        given:
        def offensiveAction = TestUtils.getTestCharacterAction(CharacterActionType.DAMAGE)
        def offensiveQueuedAction = new QueuedCharacterAction(offensiveAction, Mock(RPGCharacter), null)
        def defensiveAction = TestUtils.getTestCharacterAction(CharacterActionType.DEFENSIVE)
        def defensiveQueuedAction = new QueuedCharacterAction(defensiveAction, Mock(RPGCharacter), null)
        def actionsList = [offensiveQueuedAction, offensiveQueuedAction, offensiveQueuedAction, defensiveQueuedAction, offensiveQueuedAction, defensiveQueuedAction]
        def game = new Game(1L, 1L, 'name', '').tap { actionQueue.addAll(actionsList) }

        when:
        def sortedList = game.partitionAndShuffleActionQueue(game.actionQueue)

        then:
        sortedList.take(2).forEach { assert it.action.actionType == CharacterActionType.DEFENSIVE }

        and:
        sortedList.takeRight(4).forEach { assert it.action.actionType == CharacterActionType.DAMAGE }
    }
}
