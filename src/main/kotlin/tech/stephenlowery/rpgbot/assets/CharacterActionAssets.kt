package tech.stephenlowery.rpgbot.assets

import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.action.CharacterActionStrings
import tech.stephenlowery.rpgbot.core.action.CharacterActionType
import tech.stephenlowery.rpgbot.core.action.TargetingType
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DamageHealthEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DefendEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.HealEffect

object CharacterActionAssets {
    
    val allActions
        get() = listOf(
            GenericAttack,
            GenericSelfDefend,
            SuperDefend,
            SelfHeal,
        )
    
    val GenericAttack: CharacterAction
        get() = CharacterAction(
            effect = DamageHealthEffect(8, 17),
            displayName = "Attack",
            description = "A generic attack that does a little damage because that's how I programmed it, bitch.",
            identifier = "action|attack",
            targetingType = TargetingType.SINGLE_TARGET,
            actionType = CharacterActionType.DAMAGE,
            strings = CharacterActionStrings(
                queuedText = "You ready yourself for a generic attack.",
                actionText = "{source} generically attacks {target}!",
                successText = "It connects and {target} generically receives {value} damage.",
                missedText = "The attack misses in a pretty generic way.",
                critText = "The generic attack is somehow more generically powerful and does a YUGE {value} damage to {target}!"
            )
        )
    
    val GenericSelfDefend: CharacterAction
        get() = CharacterAction(
            effect = DefendEffect(75),
            displayName = "Defend",
            description = "Temporarily boost your defense for the duration of the round. Best used before you're attacked, obviously.",
            identifier = "action|defend",
            actionType = CharacterActionType.DEFENSIVE,
            targetingType = TargetingType.SELF,
            strings = CharacterActionStrings(
                queuedText = "You brace yourself for attacks.",
                actionText = "{source} assumes a defensive stance."
            )
        )
    
    val SuperDefend: CharacterAction
        get() = CharacterAction(
            effect = DefendEffect(80),
            displayName = "Super Defend",
            description = "Temporarily gives a significant defensive boost with a long cooldown.",
            identifier = "action|superdefend",
            actionType = CharacterActionType.DEFENSIVE,
            targetingType = TargetingType.SELF,
            strings = CharacterActionStrings(
                queuedText = "You brace yourself for attacks.",
                actionText = "{source} assumes an intimidating defensive stance."
            ),
            cooldown = 3
        )
    
    val SelfHeal: CharacterAction
        get() = CharacterAction(
            effect = HealEffect(9, 23),
            displayName = "Heal",
            description = "A weak spell that heals you for a little. Cooldown of three turns.",
            identifier = "action|heal",
            actionType = CharacterActionType.HEALING,
            targetingType = TargetingType.SELF,
            strings = CharacterActionStrings(
                queuedText = "You prepare to heal your wounds.",
                actionText = "{source} utters an incantation...",
                successText = "They are bathed in golden light as their wounds begin to heal, negating {value} damage.",
                missedText = "{source} emits a bright but brief flash of light as their healing spell fails.",
            ),
            cooldown = 3,
        )
    
    val BBEGGenericAttack: CharacterAction
        get() = CharacterAction(
            effect = DamageHealthEffect(13, 30),
            displayName = "BBEGGenericAttack",
            description = "BBEGGenericAttackDescription",
            identifier = "action|bbeggenericattack",
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE_TARGET,
            strings = CharacterActionStrings(
                queuedText = "BBEGGenericAttack queued",
                actionText = "BBEGGenericAttack action",
                successText = "BBEGGenericAttack success",
                missedText = "BBEGGenericAttack missed",
                critText = "BBEGGenericAttack crit",
            )
        )
    
    val BBEGGenericHeal: CharacterAction
        get() = CharacterAction(
            effect = HealEffect(24, 30),
            displayName = "BBEGGenericHeal",
            description = "BBEGGenericHealDescription",
            identifier = "action|bbeggenericheal",
            actionType = CharacterActionType.HEALING,
            targetingType = TargetingType.SELF,
            cooldown = 3,
            strings = CharacterActionStrings(
                queuedText = "BBEGGenericHeal queued",
                actionText = "BBEGGenericHeal action",
                successText = "BBEGGenericHeal success",
                missedText = "BBEGGenericHeal missed",
                critText = "BBEGGenericHeal crit",
            )
        )
}