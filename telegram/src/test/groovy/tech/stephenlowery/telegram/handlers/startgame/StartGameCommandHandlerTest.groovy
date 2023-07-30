package tech.stephenlowery.telegram.handlers.startgame

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.User
import spock.lang.Specification
import tech.stephenlowery.rpgbot.core.game.Game
import tech.stephenlowery.rpgbot.core.game.GameManager

class StartGameCommandHandlerTest extends Specification {

    def setup() {
        GameManager.INSTANCE.games.clear()
    }

    def "execute returns PrivateChat if command is sent from a private chat"() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> "private"
            }
        }

        expect:
        PrivateChat.INSTANCE == StartGameCommandHandler.INSTANCE.execute(message)
    }

    def "execute returns UserIdNull if command sent in non-private chat but user is null"() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> "group"
            }
            getFrom() >> null
        }

        expect:
        UserIdNull.INSTANCE == StartGameCommandHandler.INSTANCE.execute(message)
    }

    def "execute returns NoGameExists if command is sent in non-private chat from a user but there is no game"() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> "group"
            }
            getFrom() >> Mock(User) {
                getId() >> 1L
            }
        }

        expect:
        NoGameExists.INSTANCE == StartGameCommandHandler.INSTANCE.execute(message)
    }

    def "execute returns UserNotInGame if command is sent in non-private chat from a user and a game exists but user is not in game"() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> "group"
                getId() >> 1L
            }
            getFrom() >> Mock(User) {
                getId() >> 3L
            }
        }

        and:
        GameManager.INSTANCE.createGame(1L, 2L, 'name')

        expect:
        UserNotInGame.INSTANCE == StartGameCommandHandler.INSTANCE.execute(message)
    }

    // TODO and block is a code smell; not right for GameManager to pick what kind of game is made
    def "execute returns IllegalNumberOfUsers if user attempts to start a game when there is an invalid number of users in it"() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> "group"
                getId() >> 1L
            }
            getFrom() >> Mock(User) {
                getId() >> 2L
            }
        }

        and:
        GameManager.INSTANCE.games[1L] = new Game(message.chat.id, message.from.id, 'name')

        expect:
        IllegalNumberOfUsers.INSTANCE == StartGameCommandHandler.INSTANCE.execute(message)
    }

    def "execute returns UserNotInitiator if command is sent in non-private chat by someone other than initiator while a game exists with more than one person in it"() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> "group"
                getId() >> 1L
            }
            getFrom() >> Mock(User) {
                getId() >> 3L
            }
        }

        and:
        GameManager.INSTANCE.createGame(1L, 2L, 'name')
        GameManager.INSTANCE.addPlayerToGame(1L, 3L, 'other name')

        expect:
        UserNotInitiator.INSTANCE == StartGameCommandHandler.INSTANCE.execute(message)
    }

    def "execute returns GameStarting if command is sent in non-private chat by the initiator while a game exists with more than one person in it"() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getType() >> "group"
                getId() >> 1L
            }
            getFrom() >> Mock(User) {
                getId() >> 2L
            }
        }

        and:
        GameManager.INSTANCE.createGame(1L, 2L, 'name')
        GameManager.INSTANCE.addPlayerToGame(1L, 4L, 'other name')

        expect:
        GameStarting.class == StartGameCommandHandler.INSTANCE.execute(message).class

        and:
        GameManager.INSTANCE.findGame(1L).hasStarted
    }
}
