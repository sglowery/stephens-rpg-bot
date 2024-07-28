package tech.stephenlowery.rpgbot.core.game

object GameConstants {

    const val BASE_HIT_CHANCE = 70.0
    const val HIT_CHANCE_PRECISION_SCALING = 2.0
    const val BASE_CRIT_CHANCE = 10.0
    const val POWER_DAMAGE_SCALAR = 1.2
    const val DEFENSE_DAMAGE_REDUCTION_FACTOR = .8
    const val BASE_CRIT_EFFECT_MULTIPLIER = 1.5
    const val CRIT_DAMAGE_PRECISION_SCALAR = .4
    const val CRIT_DAMAGE_POWER_SCALAR = .7
    const val CRIT_CHANCE_PRECISION_SCALAR = 1.0
    const val HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR = .5
    const val DEFENSE_CRIT_CHANCE_REDUCTION_FACTOR = .6
    const val DEFENSE_HIT_CHANCE_REDUCTION_FACTOR = .3
    const val HEALING_BASE_HIT_CHANCE = BASE_HIT_CHANCE + 10.0

    // attribute defaults
    const val STAT_POINTS_TO_DISTRIBUTE = 25
    const val DEFAULT_BASE_HEALTH = 70.0
    const val DEFAULT_BASE_PRIMARY_ATTRIBUTE = 1.0
}
