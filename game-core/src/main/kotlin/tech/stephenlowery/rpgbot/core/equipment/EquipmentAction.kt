package tech.stephenlowery.rpgbot.core.equipment

import tech.stephenlowery.rpgbot.core.action.EffectApplier
import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.action.EffectResult
import tech.stephenlowery.rpgbot.core.action.ActionResource
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class EquipmentAction(
    val characterAction: CharacterAction,
    private val actionResource: ActionResource? = null,
) : EffectApplier by characterAction {

    private var applications = 0

    val displayName: String
        get() = characterAction.displayName

    val identifier: String
        get() = characterAction.identifier

    override fun applyEffect(source: RPGCharacter, target: RPGCharacter, cycle: Int): List<EffectResult> {
        if (actionResource != null && cycle == 0) {
            applications += 1
            actionResource.useResource()
        }
        return characterAction.applyEffect(source, target, cycle)
    }

    fun cycle() {
        actionResource?.cycle()
    }

    fun isUsable(): Boolean {
        return actionResource?.isUsable() ?: true
    }

    fun getName(): String {
        return "${characterAction.displayName}${actionResource?.let { " (${it.unitString()})" } ?: ""}"
    }
}