package tech.stephenlowery.rpgbot.models.action

import tech.stephenlowery.rpgbot.models.character.RPGCharacter

class EffectResult(
    var source: RPGCharacter? = null,
    var target: RPGCharacter? = null,
    var value: Int = 0,
    var miss: Boolean = false,
    var crit: Boolean = false,
    var continued: Boolean = false,
    var expired: Boolean = false,
    var other: String? = null,
    var chained: Boolean = false
) {
    companion object {
        
        val EMPTY get() = EffectResult()

        fun merge(vararg results: EffectResult): EffectResult {
            return when (results.size) {
                0 -> EMPTY
                1 -> results.first()
                else -> EffectResult(
                    source = results[0].source,
                    target = results[0].target,
                    value = results.sumBy { it.value },
                    miss = results.any { it.miss },
                    crit = results.any { it.crit }
                )
            }
        }

        fun merge(results: List<EffectResult>) = merge(*results.toTypedArray())
    }
}