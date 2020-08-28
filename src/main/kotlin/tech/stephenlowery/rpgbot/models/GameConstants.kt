package tech.stephenlowery.rpgbot.models

class GameConstants {
    companion object {
        const val BASE_HIT_CHANCE = 70
        const val HIT_CHANCE_PRECISION_SCALING = 1.4
        const val BASE_CRIT_CHANCE = 10.0
        const val POWER_SCALING = 1.2
        const val BASE_CRIT_DAMAGE_MULTIPLIER = 1.5
        const val CRIT_DAMAGE_PRECISION_SCALAR = .04 // 1/25
        const val HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR = .5
        const val STAT_POINTS_TO_DISTRIBUTE = 25
        const val DEFENSE_DAMAGE_REDUCTION_FACTOR = .6
        const val DEFENSE_HIT_CHANCE_REDUCTION_FACTOR = .2
    }
}
