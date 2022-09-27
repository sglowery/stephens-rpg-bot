package tech.stephenlowery.rpgbot.core.character

import tech.stephenlowery.rpgbot.assets.CharacterActionAssets
import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.core.action.TargetingType
import tech.stephenlowery.rpgbot.core.character.attribute.Attribute
import tech.stephenlowery.rpgbot.core.character.trait.impl.CharacterTraits
import tech.stephenlowery.rpgbot.core.game.Game
import tech.stephenlowery.rpgbot.core.game.GameConstants.BASE_CRIT_CHANCE
import tech.stephenlowery.rpgbot.core.game.GameConstants.DEFAULT_BASE_HEALTH
import tech.stephenlowery.rpgbot.core.game.GameConstants.DEFAULT_BASE_PRIMARY_ATTRIBUTE
import tech.stephenlowery.rpgbot.core.game.GameConstants.STAT_POINTS_TO_DISTRIBUTE
import kotlin.random.Random

sealed class RPGCharacter(val name: String, val id: Long) {

    val health = Attribute("Health", DEFAULT_BASE_HEALTH)
    val damage = Attribute("Damage", 0.0, min = 0)
    val power = Attribute("Power", DEFAULT_BASE_PRIMARY_ATTRIBUTE)
    val precision = Attribute("Precision", DEFAULT_BASE_PRIMARY_ATTRIBUTE)
    val defense = Attribute("Defense", DEFAULT_BASE_PRIMARY_ATTRIBUTE)

    val damageTaken = Attribute("Damage taken", 100.0)
    val damageGiven = Attribute("Damage given", 100.0)

    val healingTaken = Attribute("Damage taken", 100.0)
    val healingGiven = Attribute("Damage given", 100.0)

    val criticalDamage = Attribute("Critical Damage", 100.0)
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
        health.max = health.value()
        health.min = 0
    }

    protected fun getAttributes() = listOf(health, power, precision, defense)

    private fun getSecondaryAttributes() = listOf(criticalChance, criticalDamage, damageGiven, damageTaken)

    private fun cycleAttributeModifiers() {
        getAllAttributes().forEach(Attribute::cycleClearAndConsolidateModifiers)
    }

    private fun getAllAttributes() = getAttributes() + getSecondaryAttributes()

    private fun cycleCooldowns() {
        cooldowns.entries.forEach { it.setValue(it.value - 1) }
        cooldowns.entries.removeIf { it.value <= 0 }
    }
}

class NonPlayerCharacter(
    name: String,
    id: Long,
    healthValue: Int? = null,
    powerValue: Int? = null,
    defenseValue: Int? = null,
    precisionValue: Int? = null,
    private val actionDecidingBehavior: ((Game) -> QueuedCharacterAction)? = null
) : RPGCharacter(name, id) {

    init {
        giveRandomStats()
        initAttributes(healthValue to health, powerValue to power, defenseValue to defense, precisionValue to precision)
    }

    fun getQueuedAction(game: Game) = actionDecidingBehavior?.let { it(game) }

    private fun initAttributes(vararg valueAttributePairs: Pair<Int?, Attribute>) {
        valueAttributePairs.forEach { pair -> pair.first?.let { pair.second.base = it.toDouble() } }
    }
}

class PlayerCharacter(userID: Long, name: String) : RPGCharacter(name, userID) {

    var queuedAction: QueuedCharacterAction? = null

    init {
        giveRandomStats()
        applyTraitsFromStats()
    }

    fun getCharacterSummaryText(): String {
        val baseText = """
            Name: $name
            Health: ${getActualHealth()} / ${health.value()} (${getHealthPercent()}%)
            Power: ${power.value()}
            Precision: ${precision.value()}
            Defense: ${defense.value()}
        """.trimIndent()
        return baseText + when (specialMessages.isNotEmpty()) {
            true  -> "\n\nAdditionally, your stats grant you the following properties:\n\n" + specialMessages.joinToString("\n\n") { "- $it" }
            false -> ""
        }
    }

    fun getPreActionText(): String = getCharacterStatusText() + (if (cooldowns.isNotEmpty()) "\n\n" + getUnavailableAbilitiesText() else "")

    fun getCharacterStatusText(): String {
        return "Your current stats:\n" +
                "Health: ${getActualHealth()} / ${health.displayValue()} (${getHealthPercent()}%)\n" +
                "Power: ${power.displayValue()}\n" +
                "Precision: ${precision.displayValue()}\n" +
                "Defense: ${defense.displayValue()}"
    }

    fun addTargetToAction(newTarget: RPGCharacter) {
        queuedAction?.target = newTarget
        characterState = UserState.WAITING
    }

    fun chooseAction(actionIdentifier: String): QueuedCharacterAction {
        val newQueuedCharacterAction = QueuedCharacterAction(getAvailableActions().find { it.identifier == actionIdentifier }!!, source = this)
        queuedAction = newQueuedCharacterAction
        if (newQueuedCharacterAction.action.targetingType == TargetingType.SELF) {
            newQueuedCharacterAction.target = this
        }
        if (newQueuedCharacterAction.action.cooldown > 0) {
            cooldowns[newQueuedCharacterAction.action.identifier] = newQueuedCharacterAction.action.cooldown
        }
        characterState = if (newQueuedCharacterAction.action.targetingType != TargetingType.SELF) UserState.CHOOSING_TARGETS else UserState.WAITING
        return newQueuedCharacterAction
    }

    override fun resetForNextTurnAfterAction() {
        clearQueuedAction()
        super.resetForNextTurnAfterAction()
    }

    override fun toString(): String {
        return "Name: ${name}\n" +
                "User ID: ${id}\n" +
                getAttributes().joinToString("\n") { "${it.name}: ${it.value()}" }
    }

    override fun resetCharacter() {
        queuedAction = null
        super.resetCharacter()
    }

    private fun clearQueuedAction() {
        queuedAction = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerCharacter

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.toInt().hashCode()
    }

}