package tech.stephenlowery.rpgbot.core.character

import tech.stephenlowery.rpgbot.assets.CharacterActionAssets
import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.character.attribute.Attribute
import tech.stephenlowery.rpgbot.core.character.trait.impl.CharacterTraits
import tech.stephenlowery.rpgbot.core.game.GameConstants.BASE_CRIT_CHANCE
import tech.stephenlowery.rpgbot.core.game.GameConstants.BASE_CRIT_EFFECT_MULTIPLIER
import tech.stephenlowery.rpgbot.core.game.GameConstants.DEFAULT_BASE_HEALTH
import tech.stephenlowery.rpgbot.core.game.GameConstants.DEFAULT_BASE_PRIMARY_ATTRIBUTE
import tech.stephenlowery.rpgbot.core.game.GameConstants.STAT_POINTS_TO_DISTRIBUTE
import kotlin.random.Random

open class RPGCharacter(val name: String, val id: Long) {

    val health = Attribute("Health", DEFAULT_BASE_HEALTH)
    val damage = Attribute("Damage", 0.0, min = 0)
    val power = Attribute("Power", DEFAULT_BASE_PRIMARY_ATTRIBUTE)
    val precision = Attribute("Precision", DEFAULT_BASE_PRIMARY_ATTRIBUTE)
    val defense = Attribute("Defense", DEFAULT_BASE_PRIMARY_ATTRIBUTE)

    val damageTaken = Attribute("Damage taken", 100.0)
    val damageGiven = Attribute("Damage given", 100.0)

    val healingTaken = Attribute("Damage taken", 100.0)
    val healingGiven = Attribute("Damage given", 100.0)

    val criticalDamage = Attribute("Critical Damage", BASE_CRIT_EFFECT_MULTIPLIER * 100)
    val criticalChance = Attribute("Critical Hit Chance", BASE_CRIT_CHANCE)

    var characterState: UserState = UserState.NONE

    var cooldowns = mutableMapOf<String, Int>()

    val specialMessages
        get() = CharacterTraits.getQualifiedCharacterTraitsFor(this).map { it.description }

    fun getUnfilteredActions(): List<CharacterAction> = CharacterActionAssets.allActions

    fun getAvailableActions(): List<CharacterAction> {
        return getUnfilteredActions().filter { !cooldowns.containsKey(it.identifier) }
    }

    fun isAlive(): Boolean = getActualHealth() > 0 && characterState != UserState.DEAD
    
    fun isDead(): Boolean = !isAlive()

    fun getActualHealth(): Int = health.value() - damage.value()

    fun getNameAndHealthPercentLabel(): String = "$name (${getHealthPercent()}%)"

    fun getAbilitiesOnCooldown() = getUnfilteredActions().filter { cooldowns.containsKey(it.identifier) }

    fun getHealthPercent(): Int = (100.0 * getActualHealth() / health.value()).toInt()

    fun getUnavailableAbilitiesText(): String {
        return "The following abilities are on cooldown:\n" + getAbilitiesOnCooldown().joinToString("\n") { ability ->
            "${ability.displayName} (${cooldowns[ability.identifier]} turn(s) remaining)"
        }
    }

    open fun resetCharacter() {
        getAllAttributes().forEach(Attribute::reset)
        characterState = UserState.NONE
        cooldowns.clear()
        applyTraitsFromStats()
    }

    open fun resetForNextTurnAfterAction() {
        characterState = UserState.CHOOSING_ACTION
        cycleAttributeModifiers()
        cycleCooldowns()
    }

    protected fun applyTraitsFromStats() = CharacterTraits.getQualifiedCharacterTraitsFor(this).forEach { it.applyEffects(this) }

    protected fun giveRandomStats() {
        repeat(STAT_POINTS_TO_DISTRIBUTE) {
            when (Random.nextInt(4)) {
                0 -> health.base += 10
                1 -> power.base += 1
                2 -> precision.base += 1
                3 -> defense.base += 1
            }
        }
    }

    protected fun getAttributes() = listOf(health, power, precision, defense)

    protected fun setHealthBounds() {
        health.min = 0
        health.max = health.value()
    }

    private fun getSecondaryAttributes() = listOf(criticalChance, criticalDamage, damageGiven, damageTaken)

    private fun cycleAttributeModifiers() {
        getAllAttributes().forEach(Attribute::cycleClearAndConsolidateModifiers)
    }

    private fun getAllAttributes() = getAttributes() + getSecondaryAttributes()

    private fun cycleCooldowns() {
        cooldowns.replaceAll { _, turnsLeft -> turnsLeft - 1 }
        cooldowns.entries.removeIf { it.value <= 0 }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RPGCharacter

        if (id != other.id) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

}