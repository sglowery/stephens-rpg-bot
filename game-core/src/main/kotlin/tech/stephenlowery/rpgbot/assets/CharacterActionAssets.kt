package tech.stephenlowery.rpgbot.assets

import tech.stephenlowery.rpgbot.core.action.*
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DamageHealthEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DefendEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.HealEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.ComposeEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.DelayedEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.StatModEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.VampirismEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.ChainEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType

object CharacterActionAssets {

    val allActions
        get() = listOf(
            Bleed,
            Renew,
            GenericAttack,
            GenericSelfDefend,
            SuperDefend,
            Heal,
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
                successText = "{target} is generically hurt.",
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

    val Heal: CharacterAction
        get() = CharacterAction(
            effect = HealEffect(16, 23),
            displayName = "Heal",
            description = "A weak spell that heals you for a little. Cooldown of three turns.",
            identifier = "action|heal",
            actionType = CharacterActionType.HEALING,
            targetingType = TargetingType.SINGLE_TARGET_INCLUDING_SELF,
            strings = CharacterActionStrings(
                queuedText = "You prepare to heal some wounds.",
                actionText = "{source} utters an incantation...",
                successText = "{target} is bathed in golden light as their wounds begin to heal.",
            ),
            cooldown = 3,
        )

    val WindupAttack: CharacterAction
        get() = CharacterAction(
            effect = DelayedEffect(
                delayedActionEffect = DamageHealthEffect(20, 50),
                occupySource = true,
                delay = 1
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
                successText = "{source}'s powerful wind-up attack hits {target}!",
                missedText = "{source} misses their wind-up attack. What a waste.",
                critText = "{source} do an big ouchie on {target}!",
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
                effectContinuedText = "{source} is still gathering energy!",
                successText = "{source} blasts {target}!",
                missedText = "{source} wastes 2 turns of waiting and misses their blast on {target}.",
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
            description = "Deal some damage and heal for a proportion of the damage done.",
            identifier = "action|lifesteal",
            cooldown = 4,
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE_TARGET,
            strings = CharacterActionStrings(
                queuedText = "You get ready to steal some life.",
                actionText = "{source} steals {target}'s life!",
                successText = "{target} is sapped of their life force, and {source} feels healthier.",
            )
        )

    val AmpUp: CharacterAction
        get() = CharacterAction(
            effect = ComposeEffect(
                inner = DamageHealthEffect(min = 15, max = 25),
                outer = StatModEffect(statGetter = RPGCharacter::power,
                                      modDuration = 5,
                                      attributeModifierType = AttributeModifierType.ADDITIVE,
                                      modifierName = "Amped Up"),
                compose = {
                    val damageDone = innerResults.filter { !it.miss && it.target == to }.sumOf { it.value }
                    val powerIncrease = (damageDone * 0.75).toInt()
                    outer.applyEffect(from, from, cycle, powerIncrease)
                    EffectResult.singleResult(
                        source = from,
                        target = to,
                        value = damageDone,
                        actionType = CharacterActionType.DAMAGE,
                        miss = innerResults.first().miss,
                        crit = innerResults.first().crit,
                        other = powerIncrease.toString()
                    )
                }
            ),
            displayName = "Amp Up",
            description = "Do damage and increase your power temporarily by a proportion of the damage done.",
            identifier = "action|amp",
            cooldown = 4,
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE_TARGET,
            strings = CharacterActionStrings(
                queuedText = "You start amping yourself up",
                actionText = "{source} is amping up!",
                successText = "They lash out at {target} and feel invigorated!",
                missedText = "Unfortunately, they fizzle out."
            )
        )

    val Bleed: CharacterAction
        get() = CharacterAction(
            effect = ChainEffect(
                DamageHealthEffect(
                    min = 20,
                    max = 32,
                ),
                DamageHealthEffect(
                    min = 5,
                    max = 9,
                    duration = 5,
                    canMiss = false,
                    canCrit = false
                )
            ),
            displayName = "Bleed",
            description = "Bleed your blast life.",
            identifier = "action|bleed",
            cooldown = 4,
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE_TARGET,
            strings = CharacterActionStrings(
                queuedText = "You prepare to bleed {target} dry.",
                actionText = "{source} goes for the jugular!",
                successText = "They tear the skin apart, making {target} bleed out!",
                missedText = "They whiff. No blood will be drawn for now.",
                effectContinuedText = "{target} keeps losing blood.",
                effectOverText = "{target}'s wounds close enough to stop the bleeding."
            )
        )

    val Renew: CharacterAction
        get() = CharacterAction(
            effect = HealEffect(
                min = 12,
                max = 16,
                duration = 5,
                canCrit = true
            ),
            displayName = "Renew",
            description = "Heal over time",
            identifier = "action|renew",
            cooldown = 4,
            actionType = CharacterActionType.HEALING,
            targetingType = TargetingType.SELF,
            strings = CharacterActionStrings(
                queuedText = "You will cast renew on yourself. Totally not a WoW ripoff.",
                actionText = "{source} casts renew on themselves.",
                successText = "{source}'s wounds glow and are slightly healed up.",
                effectOverText = "{source}'s Renew has expired."
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