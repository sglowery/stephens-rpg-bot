package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.MetaActionEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.UserState

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
        shouldBeApplied(cycle) -> applyDelayedEffect(from, to, cycle)
        else                   -> EffectResult.singleResult(source = from, target = to, continued = true)
    }

    private fun handleFirstApplication(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        occupyUserIfApplicable(from)
        return EffectResult.singleResult(source = from, target = to, value = 0)
    }


    private fun occupyUserIfApplicable(user: RPGCharacter) {
        if (occupySource) {
            user.characterState = UserState.OCCUPIED
        }
    }

    private fun shouldBeApplied(cycle: Int): Boolean = durationIsMet(cycle) && !applied

    private fun durationIsMet(cycle: Int): Boolean = cycle >= delay

    private fun applyDelayedEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        unoccupyUserIfApplicable(from)
        applied = true
        return delayedActionEffect.applyEffect(from, to, cycle)
    }

    private fun unoccupyUserIfApplicable(from: RPGCharacter) {
        if (occupySource) {
            from.characterState = UserState.WAITING
        }
    }

    //    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
//    val result = applyIfDurationMet(from, to, cycle)
//    if (result != null) {
//        applied = true
//        return result
//    }
//
//    if (shouldOccupyUser(from)) {
//        from.characterState = UserState.OCCUPIED
//    }
//    return EffectResult.singleResult(
//        source = from,
//        target = to,
//        value = 0,
//        continued = true
//    )
//    }

}