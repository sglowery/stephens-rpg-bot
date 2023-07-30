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
    triggers: (CharacterActionTriggers.Builder.() -> Unit) = { },
) {

    private val triggers: CharacterActionTriggers

    init {
        this.triggers = CharacterActionTriggers.Builder().apply(triggers).build()
    }

    fun applyEffect(source: RPGCharacter, target: RPGCharacter, cycle: Int): List<EffectResult> {
        return effect.takeUnless { it.isExpired(cycle) }
            ?.applyEffect(source, target, cycle)
            ?.first()
            ?.getTriggeredEffects(source, target, cycle)
            ?: emptyList()
    }
    
    fun isExpired(cycle: Int): Boolean = cycle > duration && !isPermanent()
    
    private fun isPermanent() = duration == -1
    
    private fun EffectResult.getTriggeredEffects(
        source: RPGCharacter,
        target: RPGCharacter,
        cycle: Int,
    ): List<EffectResult> = listOf(this) + triggers.getTriggeredEffectsFromEffectResult(this, source, target, cycle)
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