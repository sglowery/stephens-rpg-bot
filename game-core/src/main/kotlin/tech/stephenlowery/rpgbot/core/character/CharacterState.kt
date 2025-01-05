package tech.stephenlowery.rpgbot.core.character

enum class CharacterState {
    NONE,
    IN_LOBBY,
    CHOOSING_ACTION,
    CHOOSING_TARGETS,
    WAITING,
    OCCUPIED,
    DEAD
}

// TODO come up with a better name than "occupied"