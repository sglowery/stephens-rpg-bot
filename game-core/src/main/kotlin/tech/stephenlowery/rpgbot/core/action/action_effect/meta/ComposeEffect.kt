package tech.stephenlowery.rpgbot.core.action.action_effect.meta

import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.action_effect.ActionEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import kotlin.math.max

data class ComposeEffectContext<T : ActionEffect>(
    val from: RPGCharacter,
    val to: RPGCharacter,
    val cycle: Int,
    val outer: T,
    val innerResults: List<EffectResult>,
)

open class ComposeEffect<T : ActionEffect> internal constructor(
    val outer: T,
    val inner: ActionEffect,
    val compose: ComposeEffectContext<T>.() -> List<EffectResult> = ComposeEffectContext<T>::defaultCompose,
) : ActionEffect(max(outer.duration, inner.duration)) {

    override fun applyEffect(
        from: RPGCharacter,
        to: RPGCharacter,
        cycle: Int,
    ): List<EffectResult> = ComposeEffectContext(from, to, cycle, outer, inner.applyEffect(from, to, cycle)).compose()
}

private fun <T : ActionEffect> ComposeEffectContext<T>.defaultCompose(): List<EffectResult> =
    outer.applyEffect(from, to, cycle) + innerResults

object Composers {
    val dumbApplyOuterToSelf: ComposeEffectContext<StatModEffect>.() -> List<EffectResult> = {
        outer.applyEffect(from, from, cycle) + innerResults
    }
}