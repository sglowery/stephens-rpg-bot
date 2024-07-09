package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class CharacterAction(
    val effect: ActionEffect,
    val displayName: String,
    val description: String,
    val identifier: String,
    val cooldown: Int = 0,
    val duration: Int? = null,
    val actionType: CharacterActionType,
    val targetingType: TargetingType,
    val strings: CharacterActionStrings,
    triggers: (CharacterActionTriggers.Builder.() -> Unit) = { },
) {

    private val triggers: CharacterActionTriggers = CharacterActionTriggers.Builder().apply(triggers).build()

    fun applyEffect(source: RPGCharacter, target: RPGCharacter, cycle: Int): List<EffectResult> {
        return effect.applyEffect(source, target, cycle).getTriggeredEffects(source, target, cycle)
    }

    fun isExpired(cycle: Int): Boolean = duration?.let { !isPermanent() && cycle >= it } ?: effect.isExpired(cycle)

    private fun isPermanent() = duration == -1

    private fun Collection<EffectResult>.getTriggeredEffects(
        source: RPGCharacter,
        target: RPGCharacter,
        cycle: Int,
    ): List<EffectResult> = this + triggers.getTriggeredEffectsFromEffectResult(this.first(), source, target, cycle)
}

class CharacterActionTriggers private constructor(
    private val onSuccess: ActionEffect? = null,
    private val onMiss: ActionEffect? = null,
    private val onCritical: ActionEffect? = null,
) {

    fun getTriggeredEffectsFromEffectResult(effectResult: EffectResult, source: RPGCharacter, target: RPGCharacter, cycle: Int): List<EffectResult> {
        effectResult.run {
            return listOfNotNull(
                if (isSuccessfulNormalHit()) onSuccess else null,
                if (isNormalAttackMiss()) onMiss else null,
                if (isNormalAttackCritical()) onCritical else null
            ).flatMap { it.applyEffect(source, target, cycle) }
        }
    }

    class Builder {

        private var onSuccess: ActionEffect? = null
        private var onMiss: ActionEffect? = null
        private var onCrit: ActionEffect? = null

        fun onSuccess(body: () -> ActionEffect) {
            onSuccess = body()
        }

        fun onMiss(body: () -> ActionEffect) {
            onMiss = body()
        }

        fun onCrit(body: () -> ActionEffect) {
            onCrit = body()
        }

        fun build() = CharacterActionTriggers(onSuccess, onMiss, onCrit)

    }
}