package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.action.CharacterActionType
import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.CharacterState

class DelayedEffect(
    private val delay: Int = 1,
    private val delayedActionEffect: ActionEffect,
    private val occupySource: Boolean,
) : MetaActionEffect(delayedActionEffect, delay + 1) {

    private var applied: Boolean = false

    init {
        if (duration <= 0) {
            throw IllegalArgumentException("DelayedEffect cannot apply instantaneously or be permanent")
        }
    }

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> = when {
        cycle == 0             -> handleFirstApplication(from, to, cycle)
        shouldBeApplied(cycle) -> applyDelayedEffect(from, to, cycle - delay)
        else                   -> EffectResult.singleResult(source = from, target = to, actionType = CharacterActionType.OTHER, continued = true)
    }

    private fun handleFirstApplication(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        occupyUserIfApplicable(from)
        return EffectResult.singleResult(source = from, target = to, value = 0, actionType = CharacterActionType.OTHER, occupied = occupySource)
    }


    private fun occupyUserIfApplicable(user: RPGCharacter) {
        if (occupySource) {
            user.characterState = CharacterState.OCCUPIED
        }
    }

    private fun shouldBeApplied(cycle: Int): Boolean = durationIsMet(cycle) && !applied

    private fun durationIsMet(cycle: Int): Boolean = cycle == delay

    private fun applyDelayedEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        unoccupyUserIfApplicable(from)
        applied = true
        val delayedActionEffectResult = delayedActionEffect.applyEffect(from, to, cycle).first()
        return EffectResult.singleResult(
            source = from,
            target = to,
            value = delayedActionEffectResult.value,
            actionType = delayedActionEffectResult.actionType,
            crit = delayedActionEffectResult.crit,
            miss = delayedActionEffectResult.miss,
            expired = true,
            chained = delayedActionEffectResult.chained,
            occupied = false,
            continued = false,
            other = delayedActionEffectResult.other,
        )
    }

    private fun unoccupyUserIfApplicable(from: RPGCharacter) {
        if (occupySource) {
            from.characterState = CharacterState.WAITING
        }
    }

}