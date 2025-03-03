package tech.stephenlowery.rpgbot.core.character

import tech.stephenlowery.rpgbot.assets.EquipmentAssets
import tech.stephenlowery.rpgbot.core.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.core.action.TargetingType
import tech.stephenlowery.rpgbot.core.equipment.Equipment
import tech.stephenlowery.rpgbot.core.equipment.EquipmentAction

class PlayerCharacter(userID: Long, name: String) : RPGCharacter(userID, name) {

    var queuedAction: QueuedCharacterAction? = null

    private val equipment: Collection<Equipment> = EquipmentAssets.allEquipment
        .groupBy { it.equipmentRole }
        .flatMap { (_, equipmentList) -> equipmentList.shuffled().take(2) }

    init {
        giveRandomStats()
        applyTraitsFromStats()
        setHealthBounds()
        equipment.forEach { it.equipEffects(this) }
    }

    fun getPreActionText(): String = listOfNotNull(
        getAttributeModifiersAsString(),
        if (cooldowns.isNotEmpty()) getUnavailableAbilitiesText() else null,
        if (getUnusableAbilities().isNotEmpty()) getUnusableAbilitiesText() else null,
        getCharacterSummaryText(),
    ).filter { it.isNotEmpty() }.joinToString("\n\n")

    fun getFirstTimeGameStartedCharacterText(): String {
        return listOfNotNull(
            getEquipmentListString(),
            getCharacterSummaryText(),
            getTraitMessage()
        ).joinToString("\n\n")
    }

    fun getCharacterStatusText(): String {
        return "Your current stats:\n" +
                "Health: ${getHealthMinusDamage()} / ${health.displayValue()} (${getHealthPercent()}%)\n" +
                "Power: ${power.displayValue()}\n" +
                "Precision: ${precision.displayValue()}\n" +
                "Defense: ${defense.displayValue()}"
    }

    fun addTargetToAction(newTarget: RPGCharacter) {
        queuedAction?.target = newTarget
        characterState = CharacterState.WAITING
    }

    fun chooseAction(actionIdentifier: String): QueuedCharacterAction {
        val action = getAvailableActions().find { it.identifier == actionIdentifier }
            ?: throw RuntimeException("Unable to find action for identifier $actionIdentifier")
        val newQueuedCharacterAction = QueuedCharacterAction(action, source = this)
        if (action.characterAction.targetingType == TargetingType.SELF) {
            newQueuedCharacterAction.target = this
        }
        characterState = if (action.characterAction.targetingType.requiresChoosingTarget()) CharacterState.CHOOSING_TARGETS else CharacterState.WAITING
        queuedAction = newQueuedCharacterAction
        return newQueuedCharacterAction
    }

    override fun getUnfilteredActions(): List<EquipmentAction> = equipment.flatMap { it.equipmentActions }

    override fun resetForNextTurnAfterAction() {
        clearQueuedAction()
        cycleEquipmentWithResources()
        super.resetForNextTurnAfterAction()
    }

    private fun cycleEquipmentWithResources() {
        equipment.forEach { it.equipmentActions.forEach(EquipmentAction::cycle) }
    }

    override fun resetCharacter() {
        clearQueuedAction()
        super.resetCharacter()
    }

    private fun getUnavailableAbilitiesText(): String {
        return "*The following abilities are on cooldown:*\n" + getAbilitiesOnCooldown().joinToString("\n") { ability ->
            val turns = cooldowns[ability.identifier]!!
            val turnsText = if (turns == 1) "turn" else "turns"
            "${ability.displayName} ($turns $turnsText remaining)"
        }
    }

    private fun getUnusableAbilitiesText(): String {
        return "*The following abilities are not currently usable:\n*" + getUnusableAbilities().joinToString("\n") { it.getName() }
    }

    private fun getCharacterSummaryText(): String {
        return """
            Health: ${getHealthMinusDamage()} / ${health.value()} (${getHealthPercent()}%)
            Power: ${power.value()}
            Precision: ${precision.value()}
            Defense: ${defense.value()}
        """.trimIndent()
    }

    private fun getEquipmentListString(): String {
        val equipmentAndActionsGrantedText = equipment.map { equipment ->
            val name = equipment.name
            val actionNames = equipment.equipmentActions.map { it.displayName }
            return@map if (actionNames.isEmpty())
                name
            else
                "$name\n${actionNames.joinToString("\n") { actionName -> "   - $actionName" }}"
        }
        return "*--- You have been given the following equipment and actions ---*" +
                equipmentAndActionsGrantedText.joinToString("\n\n", prefix = "\n")
    }

    private fun getAttributeModifiersAsString(): String? {
        val modifiersText = getAllAttributes()
            .map { it to it.getTemporaryAndNamedModifiers() }
            .filter { it.second.isNotEmpty() }
            .joinToString("\n\n") { "${it.first.name}:\n${it.second.joinToString("\n").prependIndent("    ")}" }
        return when (modifiersText.isEmpty()) {
            true  -> null
            false -> "*---Attribute Modifiers---*\n$modifiersText"
        }
    }

    private fun getTraitMessage(): String? {
        val specialMessages = getSpecialMessages()
        return when (specialMessages.isNotEmpty()) {
            true  -> "Additionally, your stats grant you the following properties:\n\n" + specialMessages.joinToString("\n\n") { "- $it" }
            false -> null
        }
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

    override fun toString(): String {
        return "PlayerCharacter(name=$name, id=$id)"
    }

}