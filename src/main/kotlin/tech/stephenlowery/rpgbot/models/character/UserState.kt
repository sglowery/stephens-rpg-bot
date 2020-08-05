package tech.stephenlowery.rpgbot.models.character

enum class UserState {
    NONE,
    NEW_CHARACTER,
    IN_LOBBY,
    CHOOSING_ACTION,
    CHOOSING_TARGETS,
    WAITING,
    INCAPACITATED,
    DEAD
}