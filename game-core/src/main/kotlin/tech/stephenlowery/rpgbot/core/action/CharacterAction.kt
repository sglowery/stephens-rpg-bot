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
    val targetIntent: TargetIntent,
    val strings: CharacterActionStrings,
    triggers: (CharacterActionTriggers.Builder.() -> Unit) = { },
) {

    private val triggers: CharacterActionTriggers = CharacterActionTriggers.Builder().apply(triggers).build()

    fun applyEffect(source: RPGCharacter, target: RPGCharacter, cycle: Int): List<EffectResult> {
        return effect.applyEffect(source, target, cycle).getTriggeredEffects(source, target, cycle).run {
            if (this.any { it.triggered })
                listOf(this.first().asTriggeredEffect()) + this.subList(1, this.size)
            else
                this
        }
    }

    fun isExpired(cycle: Int): Boolean = duration?.let { !isPermanent() && cycle >= it } ?: effect.isExpired(cycle)

    private fun isPermanent() = duration == -1

    private fun Collection<EffectResult>.getTriggeredEffects(
        source: RPGCharacter,
        target: RPGCharacter,
        cycle: Int,
    ): List<EffectResult> = this + triggers.getTriggeredEffectsFromEffectResult(this.first(), source, target, cycle)

    private fun CharacterActionTriggers.Builder.build() = CharacterActionTriggers(onSuccess, onMiss, onCrit)
}

class CharacterActionTriggers internal constructor(
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
                .map(EffectResult::asTriggeredEffect)
        }
    }

    class Builder {

        internal var onSuccess: ActionEffect? = null
        internal var onMiss: ActionEffect? = null
        internal var onCrit: ActionEffect? = null

        fun onSuccess(body: () -> ActionEffect) {
            onSuccess = body()
        }

        fun onMiss(body: () -> ActionEffect) {
            onMiss = body()
        }

        fun onCrit(body: () -> ActionEffect) {
            onCrit = body()
        }

    }
}