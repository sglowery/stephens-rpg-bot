package tech.stephenlowery.rpgbot.telegram.handlers.newgame

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.User
import spock.lang.Specification
import tech.stephenlowery.rpgbot.core.game.GameManager

class NewGameCommandHandlerTest extends Specification {

    def setup() {
        GameManager.INSTANCE.games.clear()
    }

    def 'execute returns PrivateChat if command is called in a private chat'() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> 'private'
            }
            getFrom() >> null
        }

        expect:
        PrivateChat.INSTANCE == NewGameCommandHandler.INSTANCE.execute(message)
    }

    def 'execute returns UserIdNull if message is sent by a non-user'() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> 'group'
            }
            getFrom() >> null
        }

        expect:
        UserIdNull.INSTANCE == NewGameCommandHandler.INSTANCE.execute(message)
    }

    def 'execute returns GameCreated if command is called by a regular user in a non-private chat and game does not exist'() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> "group"
                getId() >> 1L
            }
            getFrom() >> Mock(User) {
                getId() >> 2L
                getFirstName() >> 'name'
            }
        }

        expect:
        GameCreated.INSTANCE == NewGameCommandHandler.INSTANCE.execute(message)
    }

    def 'execute returns GameAlreadyStarted if command is called by a regular user in a non-private chat and game exists and already started'() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> 'group'
                getId() >> 1L
            }
            getFrom() >> Mock(User) {
                getId() >> 2L
                getFirstName() >> 'name'
            }
        }

        and:
        GameManager.INSTANCE.createGame(1L, 2L, 'initiator')
        GameManager.INSTANCE.games[1L].startGame()

        expect:
        GameAlreadyStarted.INSTANCE == NewGameCommandHandler.INSTANCE.execute(message)
    }

    def 'execute returns GameAlreadyExists if command is called by a regular user in a non-private chat and game exists but has not started'() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> 'group'
                getId() >> 1L
            }
            getFrom() >> Mock(User) {
                getId() >> 2L
                getFirstName() >> 'name'
            }
        }

        and:
        GameManager.INSTANCE.createGame(1L, 2L, 'initiator')

        expect:
        GameAlreadyExists.INSTANCE == NewGameCommandHandler.INSTANCE.execute(message)

        and:
        GameManager.INSTANCE.findGame(1L) != null
    }
}
