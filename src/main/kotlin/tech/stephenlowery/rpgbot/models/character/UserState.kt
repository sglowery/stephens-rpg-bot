package tech.stephenlowery.rpgbot.models.character

enum class UserState {
    NONE,
    IN_LOBBY,
    CHOOSING_ACTION,
    CHOOSING_TARGETS,
    WAITING,
    DEAD
}