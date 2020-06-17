package tech.stephenlowery.rpgbot.models

class BaseGameConstants {
    companion object {
        val BASE_HIT_CHANCE = 70
        val HIT_CHANCE_PRECISION_SCALING = 1.4
        val BASE_CRIT_CHANCE = 10.0
        val POWER_SCALING = 1.2
        val BASE_CRIT_DAMAGE_MULTIPLIER = 1.5
        val CRIT_DAMAGE_PRECISION_SCALAR = 1 / 25
    }
}