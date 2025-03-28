package tech.stephenlowery.rpgbot.core.character

import tech.stephenlowery.rpgbot.assets.EquipmentAssets
import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.character.attribute.Attribute
import tech.stephenlowery.rpgbot.core.character.trait.impl.CharacterTraits
import tech.stephenlowery.rpgbot.core.equipment.EquipmentAction
import tech.stephenlowery.rpgbot.core.game.GameConstants.BASE_CRIT_CHANCE
import tech.stephenlowery.rpgbot.core.game.GameConstants.BASE_CRIT_EFFECT_MULTIPLIER
import tech.stephenlowery.rpgbot.core.game.GameConstants.DEFAULT_BASE_HEALTH
import tech.stephenlowery.rpgbot.core.game.GameConstants.DEFAULT_BASE_PRIMARY_ATTRIBUTE
import tech.stephenlowery.rpgbot.core.game.GameConstants.STAT_POINTS_TO_DISTRIBUTE
import kotlin.random.Random

private fun asPercentage(num: Int): String = "$num%"

open class RPGCharacter(val id: Long, val name: String) {

    val health = Attribute("Health", DEFAULT_BASE_HEALTH)
    val damage = Attribute("Damage", 0.0, min = 0)
    val power = Attribute("Power", DEFAULT_BASE_PRIMARY_ATTRIBUTE)
    val precision = Attribute("Precision", DEFAULT_BASE_PRIMARY_ATTRIBUTE)
    val defense = Attribute("Defense", DEFAULT_BASE_PRIMARY_ATTRIBUTE)

    val damageTakenScalar = Attribute("Damage taken", 100.0, displayValueFn = ::asPercentage)
    val damageGivenScalar = Attribute("Damage given", 100.0, displayValueFn = ::asPercentage)

    val healingTakenScalar = Attribute("Healing taken", 100.0, displayValueFn = ::asPercentage)
    val healingGivenScalar = Attribute("Healing given", 100.0, displayValueFn = ::asPercentage)

    val criticalEffectScalar = Attribute("Critical Effect", BASE_CRIT_EFFECT_MULTIPLIER * 100, displayValueFn = ::asPercentage)
    val criticalChance = Attribute("Critical Hit Chance", BASE_CRIT_CHANCE, displayValueFn = ::asPercentage)

    var characterState: CharacterState = CharacterState.NONE

    var cooldowns = mutableMapOf<String, Int>()

    fun getSpecialMessages(): List<String> = CharacterTraits.getQualifiedCharacterTraitsFor(this).map { it.description }

    open fun getUnfilteredActions(): List<EquipmentAction> = EquipmentAssets.allEquipment.flatMap { it.equipmentActions } //CharacterActionAssets.allActions

    fun getAvailableActions(): List<EquipmentAction> {
        return getUnfilteredActions().filter { !cooldowns.containsKey(it.identifier) && it.isUsable() }
    }

    fun isAlive(): Boolean = getHealthMinusDamage() > 0 && characterState != CharacterState.DEAD

    fun isDead(): Boolean = !isAlive()

    fun getActualHealth(): Int = health.value() - damage.value()

    fun getNameAndHealthPercentLabel(): String = "$name (${getHealthPercent()}%)"

    fun getAbilitiesOnCooldown() = getUnfilteredActions().filter { isActionOnCooldown(it.identifier) }

    fun getHealthPercent(): Int = (100.0 * getActualHealth() / health.value()).toInt()
    fun getUnusableAbilities() = getUnfilteredActions().filter { !it.isUsable() }

    fun setCooldownForAction(action: CharacterAction) {
        if (action.cooldown > 0) {
            cooldowns[action.identifier] = action.cooldown
        }
    }

    open fun resetCharacter() {
        getAllAttributes().forEach(Attribute::reset)
        characterState = CharacterState.NONE
        cooldowns.clear()
        applyTraitsFromStats()
    }

    open fun resetForNextTurnAfterAction() {
        if (characterState == CharacterState.WAITING) {
            characterState = CharacterState.CHOOSING_ACTION
        }
        cycleAttributeModifiers()
        cycleCooldowns()
    }

    fun getAllAttributes() = getPrimaryAttributes() + getSecondaryAttributes()

    fun isActionOnCooldown(actionIdentifier: String): Boolean = cooldowns[actionIdentifier] != null

    protected fun applyTraitsFromStats() = CharacterTraits.getQualifiedCharacterTraitsFor(this).forEach { it.applyEffects(this) }

    protected fun giveRandomStats() {
        repeat(STAT_POINTS_TO_DISTRIBUTE) {
            val isLucky = Random.nextInt(100) < 20
            val luckModifier = if (isLucky) 2 else 0
            when (Random.nextInt(4)) {
                0 -> health.base += (9..13).random() + luckModifier
                1 -> power.base += 1 + luckModifier
                2 -> precision.base += 1 + luckModifier
                3 -> defense.base += 1 + luckModifier
            }
        }
    }

    protected fun setHealthBounds() {
        health.min = 0
        health.max = health.value()
    }

    private fun getPrimaryAttributes() = listOf(health, power, precision, defense)

    private fun getSecondaryAttributes() = listOf(
        criticalChance,
        criticalEffectScalar,
        damageGivenScalar,
        damageTakenScalar,
        healingGivenScalar,
        healingTakenScalar
    )

    private fun cycleAttributeModifiers() {
        getAllAttributes().forEach(Attribute::cycleClearAndConsolidateModifiers)
    }

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

    override fun toString(): String {
        return "RPGCharacter(name=$name, id=$id)"
    }

}