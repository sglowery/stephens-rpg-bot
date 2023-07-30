package tech.stephenlowery.telegram.handlers.joingame

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.User
import spock.lang.Specification
import tech.stephenlowery.rpgbot.core.game.GameManager

class JoinGameCommandHandlerTest extends Specification {

    def setup() {
        GameManager.INSTANCE.games.clear()
    }

    def "execute returns PrivateChat when message is sent from a private chat"() {
        given:
        def message = Mock(Message) {
            getChat() >> Mock(Chat) {
                getId() >> 1
                getType() >> "private"
            }
        }

        expect:
        PrivateChat.INSTANCE == JoinGameCommandHandler.INSTANCE.execute(message)
    }

    def "execute returns UserIdNull when message is sent from a non-private chat but from is null"() {
        given:
        def message = Mock(Message) {
            getFrom() >> null
            getChat() >> Mock(Chat) {
                getType() >> "group"
                getId() >> 1L
            }
        }

        expect:
        UserIdNull.INSTANCE == JoinGameCommandHandler.INSTANCE.execute(message)
    }

    def "execute returns NoGameExists when message is sent from a non-private chat and is from a user, but no game was created before"() {
        given:
        def message = Mock(Message) {
            getFrom() >> Mock(User) {
                getId() >> 1L
            }
            getChat() >> Mock(Chat) {
                getType() >> "group"
                getId() >> 1L
            }
        }

        expect:
        NoGameExists.INSTANCE == JoinGameCommandHandler.INSTANCE.execute(message)
    }

    def "execute returns PlayerAlreadyInGame when initiator tries to join game"() {
        given:
        def message = Mock(Message) {
            getFrom() >> Mock(User) {
                getId() >> 2L
            }
            getChat() >> Mock(Chat) {
                getType() >> "group"
                getId() >> 1L
            }
        }

        and:
        GameManager.INSTANCE.createGame(1L, 2L, 'name')

        expect:
        PlayerAlreadyInGame.INSTANCE == JoinGameCommandHandler.INSTANCE.execute(message)
    }

    def "execute returns PlayerAlreadyInGame when another player tries to join game after joining previously"() {
        given:
        def message = Mock(Message) {
            getFrom() >> Mock(User) {
                getId() >> 3L
            }
            getChat() >> Mock(Chat) {
                getType() >> "group"
                getId() >> 1L
            }
        }

        and:
        GameManager.INSTANCE.createGame(1L, 2L, 'name')
        GameManager.INSTANCE.addPlayerToGame(1L, 3L, 'other name')

        expect:
        PlayerAlreadyInGame.INSTANCE == JoinGameCommandHandler.INSTANCE.execute(message)
    }

    def "execute returns JoinedGame when command sent by regular user in a non-private chat if a game exists and hasn't started yet"() {
        given:
        def message = Mock(Message) {
            getFrom() >> Mock(User) {
                getFirstName() >> "name"
                getId() >> 3L
            }
            getChat() >> Mock(Chat) {
                getType() >> "group"
                getId() >> 1L
            }
        }

        and:
        GameManager.INSTANCE.createGame(1L, 2L, 'name')

        expect:
        JoinedGame.class == JoinGameCommandHandler.INSTANCE.execute(message).class

        and:
        GameManager.INSTANCE.findGame(1L).players.keySet().last() == 3L
    }
}
