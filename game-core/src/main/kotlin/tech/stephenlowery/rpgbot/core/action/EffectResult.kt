package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import java.util.Collections.singletonList

class EffectResult(
    val source: RPGCharacter,
    val target: RPGCharacter,
    val value: Int = 0,
    val actionType: CharacterActionType = CharacterActionType.OTHER,
    val miss: Boolean = false,
    val crit: Boolean = false,
    var continued: Boolean = false,
    var expired: Boolean = false,
    val chained: Boolean = false,
    val occupied: Boolean = false,
    val triggered: Boolean = false,
    val other: String? = null,
) {

    fun isNormalAttack(): Boolean = !continued && !expired && !chained

    fun isSuccessfulNormalHit(): Boolean = !miss && !crit && isNormalAttack()

    fun isNormalAttackMiss(): Boolean = miss && isNormalAttack()

    fun isNormalAttackCritical(): Boolean = crit && !miss && isNormalAttack()

    fun asTriggeredEffect(): EffectResult =
        EffectResult(
            source,
            target,
            value,
            actionType,
            miss,
            crit,
            continued,
            expired,
            chained,
            occupied,
            triggered = true,
            other
        )

    companion object {

        fun singleResult(
            source: RPGCharacter,
            target: RPGCharacter,
            value: Int = 0,
            actionType: CharacterActionType,
            miss: Boolean = false,
            crit: Boolean = false,
            continued: Boolean = false,
            expired: Boolean = false,
            chained: Boolean = false,
            occupied: Boolean = false,
            triggered: Boolean = false,
            other: String? = null,
        ): List<EffectResult> = singletonList(
            EffectResult(
                source,
                target,
                value,
                actionType,
                miss,
                crit,
                continued,
                expired,
                chained,
                occupied,
                triggered,
                other,
            )
        )
    }
}