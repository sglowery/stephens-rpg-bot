package tech.stephenlowery.rpgbot.core.equipment

import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class Equipment(
    val name: String,
    val equipmentRole: EquipmentRole,
    val equipmentActions: List<EquipmentAction>,
    val equipEffects: RPGCharacter.() -> Unit = { },
) {

    constructor(
        name: String,
        equipmentRole: EquipmentRole,
        actions: Collection<CharacterAction> = emptyList(),
        equipEffects: RPGCharacter.() -> Unit = {},
    ) : this(
        name,
        equipmentRole,
        toBasicEquipmentActions(actions).toList(),
        equipEffects
    )
}

fun toBasicEquipmentActions(actions: Collection<CharacterAction>): Collection<EquipmentAction> {
    return actions.map(::toBasicEquipmentAction)
}

fun toBasicEquipmentAction(action: CharacterAction): EquipmentAction {
    return EquipmentAction(action)
}