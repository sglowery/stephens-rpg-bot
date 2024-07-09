package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import kotlin.math.max

typealias ComposeEffectContext<T> = (
    from: RPGCharacter,
    to: RPGCharacter,
    cycle: Int,
    outer: T,
    innerResults: List<EffectResult>,
) -> List<EffectResult>

open class ComposeEffect<T : ActionEffect> internal constructor(
    val outer: T,
    val inner: ActionEffect,
    val compose: ComposeEffectContext<T>,
) : ActionEffect(max(outer.duration, inner.duration)) {

    override fun applyEffect(
        from: RPGCharacter,
        to: RPGCharacter,
        cycle: Int,
    ): List<EffectResult> = compose(from, to, cycle, outer, inner.applyEffect(from, to, cycle))
}