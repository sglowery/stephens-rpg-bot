package tech.stephenlowery.rpgbot.models

class GameConstants {
    companion object {
        val BASE_HIT_CHANCE = 70
        val HIT_CHANCE_PRECISION_SCALING = 1.4
        val BASE_CRIT_CHANCE = 10.0
        val POWER_SCALING = 1.2
        val BASE_CRIT_DAMAGE_MULTIPLIER = 1.5
        val CRIT_DAMAGE_PRECISION_SCALAR = .04 // 1/25
        val HEALING_SCALING_FROM_POWER_DEFENSE_SCALAR = .5
        val STAT_POINTS_TO_DISTRIBUTE = 25
        val DEFENSE_DAMAGE_REDUCTION_FACTOR = .6
        val DEFENSE_HIT_CHANCE_REDUCTION_FACTOR = .2
    }
}
