package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import java.util.Collections.singletonList

class EffectResult(
    var source: RPGCharacter? = null,
    var target: RPGCharacter? = null,
    var value: Int = 0,
    var miss: Boolean = false,
    var crit: Boolean = false,
    var continued: Boolean = false,
    var expired: Boolean = false,
    var chained: Boolean = false,
    var other: String? = null,
) {
    
    fun isNormalAttack(): Boolean = !continued && !expired && !chained
    
    fun isSuccessfulNormalHit(): Boolean = !miss && !crit && isNormalAttack()
    
    fun isNormalAttackMiss(): Boolean = miss && isNormalAttack()
    
    fun isNormalAttackCritical(): Boolean = crit && !miss && isNormalAttack()
    
    companion object {
        
        val EMPTY get() = EffectResult()
        
        fun singleResult(
            source: RPGCharacter? = null,
            target: RPGCharacter? = null,
            value: Int = 0,
            miss: Boolean = false,
            crit: Boolean = false,
            continued: Boolean = false,
            expired: Boolean = false,
            chained: Boolean = false,
            other: String? = null,
        ): List<EffectResult> = singletonList(
            EffectResult(
                source,
                target,
                value,
                miss,
                crit,
                continued,
                expired,
                chained,
                other,
            )
        )
        
        fun merge(results: Collection<EffectResult>) = merge(*results.toTypedArray())
        
        fun merge(vararg results: EffectResult): EffectResult = merge(allMustMiss = false, allMustCrit = false, results = results)
        
        private fun merge(allMustMiss: Boolean = false, allMustCrit: Boolean = false, vararg results: EffectResult): EffectResult {
            return when (results.size) {
                0    -> EMPTY
                1    -> results.first()
                else -> mergeMultipleResults(allMustMiss, allMustCrit, results)
            }
        }
        
        private fun mergeMultipleResults(allMustMiss: Boolean, allMustCrit: Boolean, results: Array<out EffectResult>): EffectResult {
            return EffectResult(
                source = results[0].source,
                target = results[0].target,
                value = results.sumOf { it.value },
                miss = when (allMustMiss) {
                    true  -> results.all { it.miss }
                    false -> results.any { it.miss }
                },
                crit = when (allMustCrit) {
                    true  -> results.all { it.crit }
                    false -> results.any { it.crit }
                }
            )
        }
        
    }
}