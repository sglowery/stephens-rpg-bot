package tech.stephenlowery.rpgbot.models

enum class UserState {
    NONE,
    NEW_CHARACTER,
    IN_LOBBY,
    CHOOSING_ACTION,
    CHOOSING_TARGETS,
    WAITING,
    DEAD
}