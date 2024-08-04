package tech.stephenlowery.rpgbot.core.character

import tech.stephenlowery.rpgbot.core.action.QueuedCharacterAction
import tech.stephenlowery.rpgbot.core.action.TargetingType
import tech.stephenlowery.rpgbot.core.game.GameManager

class PlayerCharacter(userID: Long, name: String) : RPGCharacter(userID, name) {

    var queuedAction: QueuedCharacterAction? = null

    init {
        giveRandomStats()
        applyTraitsFromStats()
        setHealthBounds()
    }

    fun getCharacterSummaryText(): String {
        val baseText = """
            Name: $name
            Health: ${getActualHealth()} / ${health.value()} (${getHealthPercent()}%)
            Power: ${power.value()}
            Precision: ${precision.value()}
            Defense: ${defense.value()}
        """.trimIndent()
        val specialMessages = getSpecialMessages()
        return baseText + when (specialMessages.isNotEmpty()) {
            true  -> "\n\nAdditionally, your stats grant you the following properties:\n\n" + specialMessages.joinToString("\n\n") { "- $it" }
            false -> ""
        }
    }

    fun getPreActionText(): String = listOfNotNull(
        getAttributeModifiersAsString(),
        if (cooldowns.isNotEmpty()) getUnavailableAbilitiesText() else null,
        getCharacterSummaryText(),
    ).filter { it.isNotEmpty() }.joinToString("\n\n")

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
        val action = getAvailableActions().find { it.identifier == actionIdentifier }
            ?: throw RuntimeException("Unable to find action for identifier $actionIdentifier")
        val newQueuedCharacterAction = QueuedCharacterAction(action, source = this)
        queuedAction = newQueuedCharacterAction
        if (action.targetingType == TargetingType.SELF) {
            newQueuedCharacterAction.target = this
        }
        characterState = if (action.targetingType.requiresChoosingTarget()) UserState.CHOOSING_TARGETS else UserState.WAITING
        return newQueuedCharacterAction
    }

    override fun resetForNextTurnAfterAction() {
        clearQueuedAction()
        super.resetForNextTurnAfterAction()
    }

    override fun resetCharacter() {
        clearQueuedAction()
        super.resetCharacter()
    }

    private fun getAttributeModifiersAsString(): String? {
        val modifiersText = getAllAttributes()
            .map { it to it.getTemporaryAndNamedModifiers() }
            .filter { it.second.isNotEmpty() }
            .joinToString("\n\n") { "${it.first.name}: ${it.second.joinToString("\n")}" }
        return when (modifiersText.isEmpty()) {
            true  -> null
            false -> "*---Attribute Modifiers---*\n$modifiersText"
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