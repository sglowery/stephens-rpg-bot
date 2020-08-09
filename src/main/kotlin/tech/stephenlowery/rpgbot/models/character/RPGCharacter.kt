package tech.stephenlowery.rpgbot.models.character

import tech.stephenlowery.rpgbot.assets.CharacterActionAssets
import tech.stephenlowery.rpgbot.models.GameConstants.Companion.BASE_CRIT_CHANCE
import tech.stephenlowery.rpgbot.models.GameConstants.Companion.STAT_POINTS_TO_DISTRIBUTE
import tech.stephenlowery.rpgbot.models.action.CharacterAction
import tech.stephenlowery.rpgbot.models.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.models.action.TargetingType
import kotlin.random.Random

class RPGCharacter(val userID: Long, val name: String) {

    val health = Attribute("Health", 50.0)
    val damage = Attribute("Damage", 0.0)
    val power = Attribute("Power", 1.0)
    val precision = Attribute("Precision", 1.0)
    val defense = Attribute("Defense", 1.0)

    val damageTaken = Attribute("Damage taken", 100.0)
    val damageGiven = Attribute("Damage given", 100.0)

    val criticalDamage = Attribute("Critical Damage", 100.0)
    val criticalChance = Attribute("Critical Hit Chance", BASE_CRIT_CHANCE)

    var queuedAction: QueuedCharacterAction? = null

    var characterState: UserState = UserState.NONE

    var cooldowns = mutableMapOf<String, Int>()

    val specialMessages = mutableListOf<String>()

    init {
        repeat(STAT_POINTS_TO_DISTRIBUTE) {
            when (Random.nextInt(4)) {
                0 -> health.base += 10
                1 -> power.base += 1
                2 -> precision.base += 1
                3 -> defense.base += 1
            }
        }
        if (power.value() > 10 && precision.value() < 7) {
            specialMessages.add("You are powerful but imprecise; your reckless nature means you do more damage but are more vulnerable to it too")
            damageGiven.additiveModifiers.add(AttributeModifier(20.0))
            damageTaken.additiveModifiers.add(AttributeModifier(20.0))
        }
        if (health.base < 100) {
            specialMessages.add("Your frail nature has forced you to find other means to sustain yourself. You have access to the Life Steal ability and damage taken is slightly reduced")
            damageTaken.additiveModifiers.add(AttributeModifier(-10.0))
        }
        if (precision.value() > 12) {
            specialMessages.add("Your deadly precision taught you how to deliver devastating blows, significantly increasing your chance to deal critical hits and increasing the damage they do")
            criticalChance.additiveModifiers.add(AttributeModifier(30.0))
            criticalDamage.additiveModifiers.add(AttributeModifier(50.0))
        }
        if (defense.value() > 12 && power.value() < 8) {
            specialMessages.add("'Sometimes the best offense is a good defense.' You benefit twice as much from defense and you have an extra powerful defend ability")
            defense.multiplyModifiers.add(AttributeModifier(1.0))
        }
        if (health.base > 150) {
            specialMessages.add("Manipulating the essence of life is trivial to you due to your vitality; you gain access to the Life Swap ability")
        }
    }

    fun getUnfilteredActions(): List<CharacterAction> {
        val actionList = mutableListOf<CharacterAction>()
        actionList.apply {
//            add(if (power.value() < 7) CharacterActionAssets.PatheticSlap else (if (power.value() < 12) CharacterActionAssets.GenericAttack else CharacterActionAssets.Punch))
//            add(CharacterActionAssets.GenericSelfDefend)
//            add(CharacterActionAssets.SelfHeal)
//            add(CharacterActionAssets.NoxiousFart)
            addAll(
                listOf(
                    CharacterActionAssets.GenericAttack,
                    CharacterActionAssets.GenericSelfDefend,
                    CharacterActionAssets.PatheticSlap,
                    CharacterActionAssets.SelfHeal,
                    CharacterActionAssets.Punch,
                    CharacterActionAssets.SuperDefend,
                    CharacterActionAssets.LifeSteal,
                    CharacterActionAssets.LifeSwap,
                    CharacterActionAssets.NoxiousFart
                )
            )
//            if (health.base < 100) add(CharacterActionAssets.LifeSteal)
//            if (health.base > 130) add(CharacterActionAssets.LifeSwap)
//            if (defense.value() > 12 && power.value() < 8) add(CharacterActionAssets.SuperDefend)
        }
        return actionList
    }

    fun getAvailableActions(): List<CharacterAction> {
        return getUnfilteredActions().filter { !cooldowns.containsKey(it.callbackText) }
    }

    fun chooseAction(callbackData: String): QueuedCharacterAction {
        val newQueuedCharacterAction = QueuedCharacterAction(getAvailableActions().find { it.callbackText == callbackData }!!, from = this)
        queuedAction = newQueuedCharacterAction
        if (newQueuedCharacterAction.action.targetingType == TargetingType.SELF) {
            newQueuedCharacterAction.target = this
        }
        if (newQueuedCharacterAction.action.cooldown > 0) {
            cooldowns[newQueuedCharacterAction.action.callbackText] = newQueuedCharacterAction.action.cooldown
        }
        characterState = if (newQueuedCharacterAction.action.targetingType != TargetingType.SELF) UserState.CHOOSING_TARGETS else UserState.WAITING
        return newQueuedCharacterAction
    }

    fun isAlive(): Boolean = getActualHealth() > 0 && characterState != UserState.DEAD

    fun getActualHealth(): Int = health.value() - damage.value()

    fun addTargetToAction(newTarget: RPGCharacter) {
        queuedAction?.target = newTarget
        characterState = UserState.WAITING
    }

    fun clearQueuedAction() {
        queuedAction = null
    }

    fun cycleAttributeModifiers() {
        getListOfAttributes().forEach(Attribute::cycleModifiers)
    }

    fun cycleCooldowns() {
        cooldowns.entries.forEach { it.setValue(it.value - 1) }
        cooldowns.entries.removeIf { it.value <= 0 }
    }

    fun getNameAndHealthPercentLabel(): String = "${name} (${getHealthPercent()}%)"

    fun getAbilitiesOnCooldown() = getUnfilteredActions().filter { cooldowns.containsKey(it.callbackText) }

    fun getHealthPercent(): Int = (100.0 * getActualHealth() / health.value()).toInt()

    override fun toString(): String {
        return "Name: ${name}\n" +
                "User ID: ${userID}\n" +
                getListOfAttributes().map { "${it.name}: ${it.value()}" }.joinToString("\n")
    }

    fun getPreActionText(): String = getCharacterStatusText() + (if (cooldowns.isNotEmpty()) "\n\n" + getUnavailableAbilitiesText() else "")

    fun getCharacterStatusText(): String {
        return "Your current stats:\n" +
                "Health: ${getActualHealth()} / ${health.displayValue()} (${getHealthPercent()}%)\n" +
                "Power: ${power.displayValue()}\n" +
                "Precision: ${precision.displayValue()}\n" +
                "Defense: ${defense.displayValue()}"
    }

    fun getUnavailableAbilitiesText(): String {
        return "The following abilities are on cooldown:\n" + getAbilitiesOnCooldown().map { ability ->
            "${ability.displayName} (${cooldowns[ability.callbackText]} turn(s) remaining)"
        }.joinToString("\n")
    }

    fun getCharacterSummaryText(): String {
        val baseText = """
            Name: ${name}
            Health: ${getActualHealth()} / ${health.value()} (${getHealthPercent()}%)
            Power: ${power.value()}
            Precision: ${precision.value()}
            Defense: ${defense.value()}
        """.trimIndent()
        return baseText + when (specialMessages.isNotEmpty()) {
            true -> "\n\nAdditionally, your stats grant you the following properties:\n\n" + specialMessages.map { "- " + it }.joinToString("\n\n")
            else -> ""
        }
    }

    private fun getListOfAttributes(): List<Attribute> = listOf(health, power, precision, defense)
}