package tech.stephenlowery.rpgbot.assets

import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.action.CharacterActionStrings
import tech.stephenlowery.rpgbot.core.action.CharacterActionType
import tech.stephenlowery.rpgbot.core.action.TargetingType
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DamageHealthEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DefendEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.HealEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.SwapStatsEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.TemporaryStatModEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.VampirismEffect
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType

object CharacterActionAssets {

    val allActions
        get() = listOf(
            GenericAttack,
            GenericSelfDefend,
            SuperDefend,
            SelfHeal,
            PatheticSlap,
            Punch,
            LifeSteal,
            LifeSwap,
            Cringe,
            Focus,
            Meditate
        )

    val GenericAttack: CharacterAction
        get() = CharacterAction(
            effect = DamageHealthEffect(8, 17),
            displayName = "Attack",
            description = "A generic attack that does a little damage because that's how I programmed it, bitch.",
            identifier = "action|attack",
            targetingType = TargetingType.SINGLE,
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
            effect = DefendEffect(50),
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
                missedText = "{source} emits a bright but brief flash of light as their healing spell fails."
            ),
            cooldown = 3,
            triggers = {
                onCrit { DamageHealthEffect(1, 5) }
            }
        )

    val PatheticSlap: CharacterAction
        get() = CharacterAction(
            effect = DamageHealthEffect(5, 14),
            displayName = "Pathetic Slap",
            description = "A bitch-ass slap for a bitch-ass person.",
            identifier = "action|patheticslap",
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE,
            strings = CharacterActionStrings(
                queuedText = "You summon what strength you have and get ready to...slap. Really? You're going to slap them? Okay.",
                actionText = "{source} feebly winds up their hand and gets ready to slap {target}.",
                successText = "The slap connects with {target}'s face and they take {value} damage.",
                missedText = "It misses as {source}'s hand sadly whiffs through the air. That's pretty embarrassing.",
                critText = "{source} slapped {target} and actually got a good one. {target} takes {value} damage!"
            )
        )

    val LifeSteal: CharacterAction
        get() = CharacterAction(
            effect = VampirismEffect(0.8, DamageHealthEffect(11, 25)),
            displayName = "Life Steal",
            description = "You deal damage to a target and heal yourself for a proportion of the damage.",
            identifier = "action|lifesteal",
            cooldown = 4,
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE,
            strings = CharacterActionStrings(
                queuedText = "You prepare to leech the life from {target}.",
                actionText = "A red glow surrounds {target} as {source} tries to sap the life out of them!",
                missedText = "{target} breaks free from {source}'s grasp and their spell fails!",
                successText = "{target} is drained for {value} damage, and {source} feels refreshed, healing for {other}.",
                critText = "{target} is drained for {value} damage, and {source} feels refreshed, healing for {other}."
            )
        )

    val LifeSwap: CharacterAction
        get() = CharacterAction(
            effect = SwapStatsEffect(1, RPGCharacter::health, RPGCharacter::getActualHealth),
            displayName = "Life Swap",
            description = "Swap the values of yours and a target's life.",
            identifier = "action|lifeswap",
            cooldown = 6,
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE,
            strings = CharacterActionStrings(
                queuedText = "You will swap life values with {target} next turn.",
                actionText = "{source} extends their hands out and ghostly red vines explode forth, aimed directly at {target}!",
                successText = "The vines harmlessly pierce {target} and temporarily bind them and {source}, swapping their life forces.",
                failedText = "The vines attempt to pierce {target} but are deflected shortly before reaching them. The spell fails."
            )
        )

    val Punch: CharacterAction
        get() = CharacterAction(
            effect = DamageHealthEffect(9, 19),
            displayName = "Punch",
            description = "Nothin like a hook to ya gabba, m8.",
            identifier = "action|punch",
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE,
            strings = CharacterActionStrings(
                queuedText = "You plant your feet and get ready to punch {target}.",
                actionText = "{source} balls up their hand into a fist and aims a hook at {target}.",
                successText = "The punch connects with {target} right in the gabber, dealing {value} damage.",
                missedText = "{target} steps back and avoids the punch!",
                critText = "It brutally connects and streaks of blood coat {source}'s fist as {target} stumbles back, taking {value} damage!"
            )
        )

    val Cringe: CharacterAction
        get() = CharacterAction(
            effect = DamageHealthEffect(11, 20),
            displayName = "Psychological Destruction",
            description = "Annihilate your opponent with mere words.",
            identifier = "action|cringe",
            actionType = CharacterActionType.DAMAGE,
            targetingType = TargetingType.SINGLE,
            strings = CharacterActionStrings(
                queuedText = "You perform an ocular pat-down on {target} and analyze their weaknesses",
                actionText = "{source} glares at {target} with a dirty look.",
                successText = "{source} opens their mouth and says two words: \"You're cringe.\" {target}'s confidence is shaken and they take {value} psychological damage.",
                critText = "{source} opens their mouth and says two words: \"You're cringe.\" It strikes a nerve, and {target} has a complete meltdown, taking a brutal {value} psychological damage.",
                missedText = "{source} opens their mouth as if to say something to {target}, but chokes on their spit and can't get a word out. Everyone else moves on while they regain their composure."
            )
        )

    val Focus
        get() = CharacterAction(
            effect = TemporaryStatModEffect(
                .5,
                duration = 2,
                statGetter = RPGCharacter::damageGiven,
                attributeModifierType = AttributeModifierType.MULTIPLICATIVE
            ),
            displayName = "Focus",
            description = "Take a round to charge up a devastating attack",
            identifier = "action|focus",
            cooldown = 6,
            actionType = CharacterActionType.BUFF,
            targetingType = TargetingType.SELF,
            strings = CharacterActionStrings(
                queuedText = "You plant your feet and start to sap energy from the earth.",
                actionText = "{source} begins to gather energy..."
            )
        )

    val Meditate
        get() = CharacterAction(
            effect = TemporaryStatModEffect(
                .5,
                duration = 2,
                statGetter = RPGCharacter::healingGiven,
                attributeModifierType = AttributeModifierType.MULTIPLICATIVE
            ),
            displayName = "Meditate",
            description = "Take a moment to assess the pain around you",
            identifier = "action|focus",
            cooldown = 6,
            actionType = CharacterActionType.BUFF,
            targetingType = TargetingType.SELF,
            strings = CharacterActionStrings(
                queuedText = "You close your eyes and, like, become One With All or something.",
                actionText = "{source} closes their eyes and takes a deep, calm breath."
            )
        )
}