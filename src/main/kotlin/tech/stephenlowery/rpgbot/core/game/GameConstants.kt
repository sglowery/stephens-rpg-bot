package tech.stephenlowery.rpgbot.core.game

object GameConstants {
    const val BASE_HIT_CHANCE = 70.0
    const val HIT_CHANCE_PRECISION_SCALING = 1.4
    const val BASE_CRIT_CHANCE = 10.0
    const val POWER_SCALING = 1.2
    const val BASE_CRIT_EFFECT_MULTIPLIER = 1.5
    const val CRIT_EFFECT_PRECISION_SCALAR = .04 // 1/25
    const val HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR = .5
    const val STAT_POINTS_TO_DISTRIBUTE = 25
    const val DEFENSE_CRIT_CHANCE_REDUCTION_FACTOR = .6
    const val DEFENSE_HIT_CHANCE_REDUCTION_FACTOR = .2

    // attribute defaults
    const val DEFAULT_BASE_HEALTH = 75.0
    const val DEFAULT_BASE_PRIMARY_ATTRIBUTE = 1.0
}
