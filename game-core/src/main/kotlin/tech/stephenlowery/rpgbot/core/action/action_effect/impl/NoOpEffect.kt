package tech.stephenlowery.rpgbot.core.action.action_effect.impl

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class NoOpEffect : ActionEffect() {

    override fun applyEffect(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
        // setting occupied is a hack to ensure there isn't an extra line break if its action is done in the middle of the action queue
        // need to figure out the best way to determine if just action text should be sent (not expecting resolving text) from effect result
        return EffectResult.singleResult(source = from, occupied = true)
    }
}
