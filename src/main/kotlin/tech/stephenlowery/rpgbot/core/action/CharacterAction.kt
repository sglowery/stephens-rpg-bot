package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class CharacterAction(
    val effect: ActionEffect,
    val displayName: String,
    val description: String,
    val identifier: String,
    val cooldown: Int = 0,
    val duration: Int = 0,
    val actionType: CharacterActionType,
    val targetingType: TargetingType,
    val strings: CharacterActionStrings,
    val triggers: (CharacterActionTriggers.Builder.() -> Unit)? = null
) {

    var lastActionResult: EffectResult? = null

    fun applyEffect(source: RPGCharacter, target: RPGCharacter, cycle: Int): List<EffectResult>? {
        return effect.takeUnless { it.isExpired(cycle) }
            ?.applyEffect(source, target, cycle)
            ?.let { mutableListOf(it) }
            ?.apply { triggers?.let {
                addAll(CharacterActionTriggers.Builder().apply(it).build().getTriggeredEffectsFromEffectResult(this.first(), source, target, cycle))
            } }
            ?.also { lastActionResult = it.first() }
    }

    fun isExpired(cycle: Int): Boolean = cycle > duration && !isPermanent()

    fun isPermanent() = duration == -1
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
                    ).map { it.applyEffect(source, target, cycle) }
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