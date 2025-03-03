package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.equipment.EquipmentAction
import tech.stephenlowery.rpgbot.core.equipment.toBasicEquipmentAction

class QueuedCharacterAction(
    val equipmentAction: EquipmentAction,
    val source: RPGCharacter,
    var target: RPGCharacter? = null,
) {

    constructor(
        characterAction: CharacterAction,
        source: RPGCharacter,
        target: RPGCharacter? = null,
    ) : this(
        toBasicEquipmentAction(characterAction),
        source,
        target
    )

    var cooldownApplied = false

    private var cycle = 0

    private var previousPrimaryResult: EffectResult? = null

    fun cycleAndResolve(): QueuedCharacterActionResolvedResults {
        val results = equipmentAction.applyEffect(source, target!!, cycle++)
        previousPrimaryResult = results.first()
        return QueuedCharacterActionResolvedResults(equipmentAction, results)
    }

    fun isExpired(): Boolean = equipmentAction.isExpired(cycle) || previousPrimaryResult?.miss == true

    fun isUnresolved(): Boolean = cycle == 0

    fun getQueuedText(): String {
        val targetText = when (target) {
            source -> "yourself"
            else   -> target!!.name
        }
        return "You will use ${equipmentAction.displayName} on $targetText when this turn resolves."
    }
}

class QueuedCharacterActionResolvedResults(
    val action: EquipmentAction,
    val effectResults: List<EffectResult>,
    private val effectResultSeparator: String = "\n\n",
    private val stringResultOverride: String? = null,
) {

    var actionResultedInDeath = false

    val stringResult: String
        get() = stringResultOverride ?: action.characterAction.strings.getFormattedEffectResultString(effectResults.first())
}