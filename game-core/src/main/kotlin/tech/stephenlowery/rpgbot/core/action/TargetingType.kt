package tech.stephenlowery.rpgbot.core.action

enum class TargetingType {
    SELF,
    SINGLE_TARGET,
    MULTI_TARGET;

    fun requiresChoosingTarget() = this != SELF
}