package tech.stephenlowery.rpgbot.models

class CharacterActionAssets {
    companion object {
        val GenericAttack: CharacterAction
            get() = CharacterAction(
                listOf(DamageHealthEffect(8, 17)), "Attack", "action|attack", TargetingType.SINGLE,
                CharacterActionStrings(
                    queuedText = "You ready yourself for a generic attack",
                    actionText = "{source} generically attacks {target}!",
                    successText = "It connects and {target} generically receives {value} damage",
                    missedText = "The attack misses in a pretty generic way"
                )
            )

        val GenericDefend: CharacterAction
            get() = CharacterAction(
                listOf(DefendSelfEffect(min = 50)), "Defend", "action|defend", TargetingType.SELF,
                CharacterActionStrings(
                    queuedText = "You brace yourself for attacks",
                    actionText = "{source} assumes a defensive stance"
                )
            )

        val GenericHeal: CharacterAction
            get() = CharacterAction(
                listOf(HealSelfEffect(9, 13, 3)), "Heal", "action|heal", TargetingType.SELF,
                CharacterActionStrings(
                    queuedText = "You prepare to heal your wounds",
                    actionText = "{source} utters an incantation",
                    successText = "{source} is bathed in golden light as their wounds begin to heal",
                    missedText = "{source} emits a bright but brief flash of light as their healing spell fails",
                    effectContinuedText = "{source}'s generic heal generically heals {value} damage",
                    effectOverText = "{source}'s generic heal effect wears off"
                )
            )

        val PatheticSlap: CharacterAction
            get() = CharacterAction(
                listOf(DamageHealthEffect(1, 12)), "Pathetic Slap", "action|patheticslap",
                TargetingType.SINGLE,
                CharacterActionStrings(
                    queuedText = "You summon what strength you have and get ready to...slap. Really? You're going to slap them? Okay",
                    actionText = "{source} feebly winds up their hand and gets ready to slap {target}",
                    successText = "The slap connects with {target}'s face and they take {value} damage",
                    missedText = "It misses as {source}'s hand sadly whiffs through the air. That's pretty embarrassing",
                    critText = "{source} slapped {target} and actually got a fucking good one. {target} takes {value} damage"
                )
            )
    }
}