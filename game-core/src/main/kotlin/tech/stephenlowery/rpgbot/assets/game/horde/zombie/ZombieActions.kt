package tech.stephenlowery.rpgbot.assets.game.horde.zombie

import tech.stephenlowery.rpgbot.core.action.*
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DamageHealthEffect
import tech.stephenlowery.util.rpgbot.game.randomFrom

internal val ZombieBite
    get() = CharacterAction(
        displayName = "Zombie Bite",
        description = "Zombie Bite",
        identifier = "action|zombiebite",
        cooldown = 3,
        actionType = CharacterActionType.DAMAGE,
        targetingType = TargetingType.SINGLE_TARGET,
        targetIntent = TargetIntent.HOSTILE,
        effect = DamageHealthEffect(10, 15),
        strings = CharacterActionStrings(
            successText = "{source} bites {target}!",
            missedText = "{source} tries to bite {target}, but stumbles and falls down instead."
        )
    )

internal val ZombieScratch
    get() = CharacterAction(
        displayName = "Zombie Scratch",
        description = "Zombie Scratch",
        identifier = "action|zombiescratch",
        actionType = CharacterActionType.DAMAGE,
        targetingType = TargetingType.SINGLE_TARGET,
        targetIntent = TargetIntent.HOSTILE,
        effect = DamageHealthEffect(3, 7),
        strings = CharacterActionStrings(
            successText = "{source} scratches {target}!",
            missedText = "{source} tries to scratch {target}, but its gnarled hand flails harmlessly through the air."
        )
    )

internal val ZombieVomit
    get() = CharacterAction(
        displayName = "Zombie Vomit",
        description = "Zombie Vomit",
        identifier = "action|zombievomit",
        actionType = CharacterActionType.DAMAGE,
        targetingType = TargetingType.SINGLE_TARGET,
        targetIntent = TargetIntent.HOSTILE,
        cooldown = 3,
        duration = 4,
        effect = DamageHealthEffect(6, 11, canCrit = false),
        strings = CharacterActionStrings(
            successText = "{source} gurgles, burps and some heinous puke spatters all over {target}!",
            missedText = "{source} gurgles and burps and some nasty zombie bile dribbles down its chin.",
            effectContinuedText = "{target} gets burned by {source}'s lingering vomit.",
        )
    )

internal val ZombieDie
    get() = CharacterAction(
        displayName = "Die",
        description = "Die",
        identifier = "action|zombiedie",
        actionType = CharacterActionType.DAMAGE,
        targetingType = TargetingType.SELF,
        targetIntent = TargetIntent.FRIENDLY,
        effect = DamageHealthEffect(9000, 900000, canMiss = false, canCrit = false),
        strings = CharacterActionStrings(
            actionText = randomFrom(
                "{source} stumbles and falls, breaking its own neck.",
                "{source}'s stomach suddenly splits open and all its guts fall out. It dies after falling over.",
                "{source} stops shambling, suddenly vomits all over itself and falls down, dead."
            )
        )
    )