package tech.stephenlowery.rpgbot.core.action

enum class TargetingType {
    SELF,
    SINGLE_TARGET,
    SINGLE_TARGET_INCLUDING_SELF,
    MULTI_TARGET;

    fun requiresChoosingTarget() = this != SELF
}