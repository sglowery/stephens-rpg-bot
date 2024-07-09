package tech.stephenlowery.rpgbot.assets

import tech.stephenlowery.rpgbot.core.action.*
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DamageHealthEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DefendEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.HealEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.ComposeEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.DelayedEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.StatModEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.VampirismEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType

object CharacterActionAssets {

    val allActions
        get() = listOf(
            GenericAttack,
            GenericSelfDefend,
            SuperDefend,
            SelfHeal,
            WindupAttack,
            LongChargeUpAttack,
            LifeSteal,
            AmpUp,
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

    val WindupAttack: CharacterAction
        get() = CharacterAction(
            effect = DelayedEffect(
                delayedActionEffect = DamageHealthEffect(20, 50),
                occupySource = true,
                delay = 2
            ),
            displayName = "Wind-up Attack",
            description = "Gather energy for a turn before attacking.",
            identifier = "action|windup",
            cooldown = 4,
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE_TARGET,
            strings = CharacterActionStrings(
                queuedText = "You wind up a big attack",
                actionText = "{source} winds up for a big attack.",
                effectContinuedText = "{source} is still winding up their attack.",
                successText = "They hit {target} for {value} damage.",
                missedText = "But they missed. What a waste.",
                critText = "{source} do a big ouchie on {target} for {value}.",
            ),
        )

    val LongChargeUpAttack: CharacterAction
        get() = CharacterAction(
            effect = DelayedEffect(
                delay = 2,
                delayedActionEffect = DamageHealthEffect(50, 60),
                occupySource = true
            ),
            displayName = "Charge-Up Attack",
            description = "Take a couple turns to charge up a big attack",
            identifier = "action|chargeup",
            cooldown = 6,
            targetingType = TargetingType.SINGLE_TARGET,
            actionType = CharacterActionType.DAMAGE,
            strings = CharacterActionStrings(
                queuedText = "You start gathering energy for a big attack.",
                actionText = "{source} begins gathering energy for a devastating attack!",
                successText = "{source} blasts {target} for {value} damage.",
                missedText = "{source} wastes 2 turns of waiting and misses their blast on {target}.",
                effectContinuedText = "{source} is still gathering energy!",
                critText = "{source}'s time spent gathering energy results in a devastating blast, hitting {target} for {value}."
            )
        )

    val LifeSteal: CharacterAction
        get() = CharacterAction(
            effect = VampirismEffect(
                proportion = 0.5,
                min = 15,
                max = 25,
                canMiss = false
            ),
            displayName = "Life Steal",
            description = "Life steal your blast life.",
            identifier = "action|lifesteal",
            cooldown = 4,
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE_TARGET,
            strings = CharacterActionStrings(
                queuedText = "You get ready to steal some life.",
                actionText = "{source} steals {target}'s life!",
                successText = "They do {value} damage and heal themself for {other}.",
                critText = "They do {value} damage and heal themself for {other}."
            )
        )

    val AmpUp: CharacterAction
        get() = CharacterAction(
            effect = ComposeEffect(
                inner = DamageHealthEffect(min = 15, max = 25),
                outer = StatModEffect(statGetter = RPGCharacter::power, modDuration = 5, attributeModifierType = AttributeModifierType.ADDITIVE),
                compose = { from, to, cycle, outer, innerResults ->
                    val damageDone = innerResults.filter { !it.miss && it.target == to }.sumOf { it.value }
                    val powerIncrease = (damageDone * 0.75).toInt()
                    outer.applyEffect(from, from, cycle, powerIncrease)
                    EffectResult.singleResult(
                        source = from,
                        target = to,
                        value = damageDone,
                        miss = innerResults.first().miss,
                        crit = innerResults.first().crit,
                        other = powerIncrease.toString()
                    )
                }
            ),
            displayName = "Amp Up",
            description = "Amp up your blast life.",
            identifier = "action|amp",
            cooldown = 4,
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE_TARGET,
            strings = CharacterActionStrings(
                queuedText = "You start amping yourself up",
                actionText = "{source} is amping up!",
                successText = "They hurt {target} for {value} and temporarily increases their power by {other}!",
                critText = "They hurt {target} for {value} and temporarily increases their power by {other}!",
                missedText = "Unfortunately, they fizzle out."
            )
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