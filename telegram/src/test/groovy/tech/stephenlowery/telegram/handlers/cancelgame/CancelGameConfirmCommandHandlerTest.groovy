package tech.stephenlowery.telegram.handlers.cancelgame

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.User
import spock.lang.Specification
import tech.stephenlowery.rpgbot.core.game.GameManager

class CancelGameConfirmCommandHandlerTest extends Specification {

    def setup() {
        GameManager.INSTANCE.games.clear()
        GameManager.INSTANCE.userToGameMap.clear()
    }

    def chat = Mock(Chat) {
        getId() >> 1L
        getType() >> 'group'
    }

    def message = Mock(Message) {
        getChat() >> chat
    }

    def 'execute returns PrivateChat'() {
        given:
        def privateChatMessage = Mock(Message) {
            getChat() >> Mock(Chat) {
                getId() >> 1L
                getType() >> 'private'
            }
        }

        expect:
        PrivateChat.INSTANCE == CancelGameConfirmCommandHandler.INSTANCE.execute(privateChatMessage)
    }

    def 'execute returns UserIdNull'() {
        given:
        message.getFrom() >> null

        expect:
        UserIdNull.INSTANCE == CancelGameConfirmCommandHandler.INSTANCE.execute(message)
    }

    def 'execute returns NoGameExists'() {
        given:
        message.getFrom() >> Mock(User) {
            getId() >> 2L
        }

        expect:
        NoGameExists.INSTANCE == CancelGameConfirmCommandHandler.INSTANCE.execute(message)
    }

    def 'execute returns UserNotInGame'() {
        given:
        GameManager.INSTANCE.createGame(chat.id, 2L, 'name')

        and:
        def notInGameUserId = 3L
        message.getFrom() >> Mock(User) {
            getId() >> notInGameUserId
        }

        expect:
        UserNotInGame.INSTANCE == CancelGameConfirmCommandHandler.INSTANCE.execute(message)
    }

    def 'execute returns UserNotInitiator'() {
        given:
        def initiatorId = 2L
        def inGameNotInitiatorId = 3L
        GameManager.INSTANCE.createGame(chat.id, initiatorId, 'name')
        GameManager.INSTANCE.addPlayerToGame(chat.id, inGameNotInitiatorId, 'other name')

        and:
        message.getFrom() >> Mock(User) {
            getId() >> inGameNotInitiatorId
        }

        expect:
        UserNotInitiator.INSTANCE == CancelGameConfirmCommandHandler.INSTANCE.execute(message)
    }

    def 'execute returns GameCanBeCanceled'() {
        given:
        def initiatorId = 2L
        def inGameNotTryingToCancelId = 3L
        GameManager.INSTANCE.createGame(chat.id, initiatorId, 'name')
        GameManager.INSTANCE.addPlayerToGame(chat.id, inGameNotTryingToCancelId, 'other name')

        and:
        message.getFrom() >> Mock(User) {
            getId() >> initiatorId
        }

        expect:
        GameCanBeCanceled.INSTANCE == CancelGameConfirmCommandHandler.INSTANCE.execute(message)
    }
}
