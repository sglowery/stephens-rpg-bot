package tech.stephenlowery.rpgbot.core.character

enum class UserState {
    NONE,
    CHOOSING_ARCHETYPE,
    CHOOSING_SKILLS,
    IN_LOBBY,
    CHOOSING_ACTION,
    CHOOSING_TARGETS,
    WAITING,
    DEAD
}