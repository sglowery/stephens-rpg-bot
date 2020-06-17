package tech.stephenlowery.rpgbot.models

import kotlin.random.Random

private const val STAT_POINTS_TO_DISTRIBUTE = 25

class RPGCharacter(val userID: Long, val name: String) {

    val health = Attribute("Health", 50)
    val damage = Attribute("Damage", 0)
    val power = Attribute("Power", 1)
    val precision = Attribute("Precision", 1)
    val defense = Attribute("Defense", 1)

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
//        if (power.value() > 10 && defense.value() < 6) {
//            power.multiplyModifiers.add(AttributeModifier(1.5, -1))
//            damage.multiplyModifiers.add(AttributeModifier(1.2, -1))
//            specialMessages.add("You are reckless, which makes you more powerful, but damage you take tends to hurt you more")
//        }
    }

    fun getUnfilteredActions(): List<CharacterAction> {
        val actionList = mutableListOf<CharacterAction>()
        actionList.add(if (power.value() < 8) CharacterActionAssets.PatheticSlap else CharacterActionAssets.GenericAttack)
        actionList.add(CharacterActionAssets.GenericDefend)
//        actionList.add(CharacterActionAssets.GenericHeal)
        return actionList
    }

    fun getAvailableActions(): List<CharacterAction> {
        return getUnfilteredActions().filter { !cooldowns.containsKey(it.callbackText) }
    }

    fun chooseAction(callbackData: String): QueuedCharacterAction {
        val newQueuedCharacterAction =
            QueuedCharacterAction(getAvailableActions().find { it.callbackText == callbackData }!!, from = this)
        queuedAction = newQueuedCharacterAction
        if (newQueuedCharacterAction.action.targetingType == TargetingType.SELF) {
            newQueuedCharacterAction.targets.add(this)
        }
        characterState = if (newQueuedCharacterAction.action.targetingType != TargetingType.SELF) UserState.CHOOSING_TARGETS else UserState.WAITING
        return newQueuedCharacterAction
    }

    fun isAlive(): Boolean = getHealth() > 0

    fun getHealth(): Int = health.value() - damage.value()

    fun addTargetToAction(newTarget: RPGCharacter): UserState {
        val targetsList = queuedAction!!.targets
        targetsList.add(newTarget)
        if (targetsList.size == queuedAction!!.action.maxTargets) {
            characterState = UserState.WAITING
        }
        return characterState
    }

    fun clearQueuedAction() {
        queuedAction = null
    }

    fun cycleAttributeModifiers() {
        getListOfAttributes().forEach { attribute ->
            listOf(attribute.additiveModifiers, attribute.multiplyModifiers).forEach { modifierSet ->
                modifierSet.forEach { modifier ->
                    modifier.cycle()
                }
            }
        }
    }

    fun cycleCooldowns() {
        cooldowns.entries.forEach { it.setValue(it.value - 1) }
        cooldowns.entries.removeIf { it.value <= 0 }
    }

    fun getNameAndHealthPercentLabel(): String = "${name} (${getHealthPercent()}%)"

    fun getAbilitiesOnCooldown() = getUnfilteredActions().filter { cooldowns.containsKey(it.callbackText) }

    fun getHealthPercent(): Int = (100.0 * getHealth() / health.value()).toInt()

    override fun toString(): String {
        return "Name: ${name}\n" +
                "User ID: ${userID}\n" +
                getListOfAttributes().map { "${it.name}: ${it.value()}" }.joinToString("\n")
    }

    private fun getListOfAttributes() = listOf(health, power, precision, defense)

    fun getCharacterStatusText(): String {
        return "Your current stats:\n" +
                "Health: ${getHealth()} / ${health.value()} (${getHealthPercent()}%)\n" +
                "Power: ${power.value()}\n" +
                "Precision: ${precision.value()}\n" +
                "Defense: ${defense.value()}"
    }
}