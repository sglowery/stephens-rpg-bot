package tech.stephenlowery.rpgbot.assets

import tech.stephenlowery.rpgbot.models.action.CharacterAction
import tech.stephenlowery.rpgbot.models.action.CharacterActionStrings
import tech.stephenlowery.rpgbot.models.action.TargetingType
import tech.stephenlowery.rpgbot.models.character.RPGCharacter

class CharacterActionAssets {
    companion object {
        val GenericAttack: CharacterAction
            get() = CharacterAction(
                listOf(DamageHealthEffect(8, 17)), "Attack", "action|attack",
                "A generic attack that does a little damage because that's how I programmed it, bitch",
                TargetingType.SINGLE,
                CharacterActionStrings(
                    queuedText = "You ready yourself for a generic attack",
                    actionText = "{source} generically attacks {target}!",
                    successText = "It connects and {target} generically receives {value} damage",
                    missedText = "The attack misses in a pretty generic way",
                    critText = "The generic attack is somehow more generically powerful and does a YUGE {value} damage to {target}"
                )
            )

        val GenericSelfDefend: CharacterAction
            get() = CharacterAction(
                listOf(DefendEffect(50)), "Defend", "action|defend",
                "Temporarily boost your defense for the duration of the round. Best used before you're attacked, obviously",
                TargetingType.SELF,
                CharacterActionStrings(
                    queuedText = "You brace yourself for attacks",
                    actionText = "{source} assumes a defensive stance"
                )
            )

        val SuperDefend: CharacterAction
            get() = CharacterAction(
                listOf(DefendEffect(80)), "Super Defend", "action|superdefend",
                "Temporarily gives a significant defensive boost with a long cooldown",
                TargetingType.SELF,
                CharacterActionStrings(
                    queuedText = "You brace yourself for attacks",
                    actionText = "{source} assumes a defensive stance"
                ),
                cooldown = 3
            )

        val SelfHeal: CharacterAction
            get() = CharacterAction(
                listOf(HealEffect(9, 23)), "Heal", "action|heal",
                "A weak spell that heals you for a little. Cooldown of three turns",
                TargetingType.SELF,
                CharacterActionStrings(
                    queuedText = "You prepare to heal your wounds",
                    actionText = "{source} utters an incantation...",
                    successText = "They are bathed in golden light as their wounds begin to heal, negating {value} damage",
                    missedText = "{source} emits a bright but brief flash of light as their healing spell fails"
                ),
                cooldown = 3
            )

        val PatheticSlap: CharacterAction
            get() = CharacterAction(
                listOf(DamageHealthEffect(5, 14)), "Pathetic Slap", "action|patheticslap",
                "A bitch-ass slap for a bitch-ass person",
                TargetingType.SINGLE,
                CharacterActionStrings(
                    queuedText = "You summon what strength you have and get ready to...slap. Really? You're going to slap them? Okay",
                    actionText = "{source} feebly winds up their hand and gets ready to slap {target}",
                    successText = "The slap connects with {target}'s face and they take {value} damage",
                    missedText = "It misses as {source}'s hand sadly whiffs through the air. That's pretty embarrassing",
                    critText = "{source} slapped {target} and actually got a fucking good one. {target} takes {value} damage"
                )
            )

        val NoxiousFart: CharacterAction
            get() = CharacterAction(
                effects = listOf(DamageHealthEffect(8, 13)),
                displayName = "Noxious Fart",
                callbackText = "action|fart",
                description = "Releases a horrifying cloud of gas that deals damage over time",
                targetingType = TargetingType.SINGLE,
                cooldown = 7,
                strings = CharacterActionStrings(
                    queuedText = "You feel an uncomfortable rumbling in your stomach and start turning to aim your rumpus at {target}",
                    actionText = "{source} doubles over and pops a blood vessel in an eye trying to push out a fart",
                    successText = "A horrible smell invades {target}'s nostrils and assaults their senses, taking {value} damage",
                    missedText = "{source} burps and feels better. It kinda stinks, but {target} seems unaffected",
                    effectContinuedText = "{source}'s fart continues to assault {target}'s senses, dealing {value} damage",
                    effectOverText = "{source}'s fart dissipates"
                )
            )

        val LifeSteal: CharacterAction
            get() = CharacterAction(
                effects = listOf(VampirismEffect(0.8, DamageHealthEffect(11, 25))),
                displayName = "Life Steal",
                callbackText = "action|lifesteal",
                description = "You deal damage to a target and heal yourself for a proportion of the damage",
                cooldown = 4,
                targetingType = TargetingType.SINGLE,
                strings = CharacterActionStrings(
                    queuedText = "You prepare to leech the life from {target}",
                    actionText = "A red glow surrounds {target} as {source} tries to sap the life out of them!",
                    missedText = "{target} breaks free from {source}'s grasp and their spell fails!",
                    successText = "{target} is drained for {value} damage, and {source} feels refreshed, healing for {other}",
                    critText = "{target} is drained for {value} damage, and {source} feels refreshed, healing for {other}"
                )
            )

        val LifeSwap: CharacterAction
            get() = CharacterAction(
                effects = listOf(SwapStatsEffect(1, RPGCharacter::health)),
                displayName = "Life Swap",
                targetingType = TargetingType.SINGLE,
                cooldown = 6,
                description = "Swap the values of yours and a target's life",
                callbackText = "action|lifeswap",
                strings = CharacterActionStrings(
                    queuedText = "You will swap life values with {target} next turn",
                    actionText = "{source} extends their hands out and ghostly red vines explode forth, aimed directly at {target}",
                    successText = "The vines harmlessly pierce {target} and temporarily bind them and {source}, swapping their life forces.",
                    failedText = "The vines attempt to pierce {target} but are deflected shortly before reaching them. The spell fails."
                )
            )

        val Punch: CharacterAction
            get() = CharacterAction(
                effects = listOf(DamageHealthEffect(9, 19)),
                displayName = "Punch",
                callbackText = "action|punch",
                targetingType = TargetingType.SINGLE,
                description = "Nothin like a hook to ya gabba, m8",
                strings = CharacterActionStrings(
                    queuedText = "You plant your feet and get ready to punch {target}",
                    actionText = "{source} balls up their hand into a fist and aims a hook at {target}",
                    successText = "The punch connects with {target} right in the gabber, dealing {value} damage",
                    missedText = "{target} steps back and avoids the punch",
                    critText = "It brutally connects and specks of blood coat {source}'s fist as {target} stumbles back, taking {value} damage"
                )
            )
    }
}