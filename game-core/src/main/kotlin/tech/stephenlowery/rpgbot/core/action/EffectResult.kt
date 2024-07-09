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
    var occupied: Boolean = false,
    var other: String? = null,
) {

    fun isNormalAttack(): Boolean = !continued && !expired && !chained

    fun isSuccessfulNormalHit(): Boolean = !miss && !crit && isNormalAttack()

    fun isNormalAttackMiss(): Boolean = miss && isNormalAttack()

    fun isNormalAttackCritical(): Boolean = crit && !miss && isNormalAttack()

    companion object {

        fun singleResult(
            source: RPGCharacter? = null,
            target: RPGCharacter? = null,
            value: Int = 0,
            miss: Boolean = false,
            crit: Boolean = false,
            continued: Boolean = false,
            expired: Boolean = false,
            chained: Boolean = false,
            occupied: Boolean = false,
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
                occupied,
                other,
            )
        )
    }
}