package tech.stephenlowery.rpgbot.assets

import tech.stephenlowery.rpgbot.assets.EquipmentAssets.allEquipment
import tech.stephenlowery.rpgbot.core.action.*
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DamageHealthEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DefendEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.HealEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.*
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType
import tech.stephenlowery.rpgbot.core.equipment.Equipment
import tech.stephenlowery.rpgbot.core.equipment.EquipmentAction
import tech.stephenlowery.rpgbot.core.equipment.EquipmentRole

object EquipmentAssets {

    val allEquipment
        get() = listOf(
            Hammer,
            BasicFirstAidKit,
            Knife,
            Steroids,
            TrashCanLid,
            BeerBottle,
            Painkillers,
            Hoodie,
            Glasses,
            FootballHelmet,
            LeftoverLasagna,
            Bong,
            BoxingGloves,
//            DebugPants,
            PocketSand,
            JumpKit,
            Stapler,
            PepperSpray,
            Pillow,
            Sandbags,
        )

    private val BeerBottle: Equipment
        get() = Equipment(
            name = "Beer Bottle",
            equipmentRole = EquipmentRole.OFFENSIVE,
            actions = listOf(
                CharacterAction(
                    displayName = "Beer bottle bash",
                    identifier = "action|beerbottlebash",
                    description = "Bash a foo with a bottle of beer",
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    effect = DamageHealthEffect(17, 23),
                    strings = CharacterActionStrings(
                        actionText = "{source} swings at {target} with an empty beer bottle!",
                        successText = "It nails them in the head and shatters.",
                        missedText = "They dodge out of the way."
                    )
                ),
                CharacterAction(
                    displayName = "Chug beer",
                    identifier = "action|chugbeer",
                    description = "Chug some beer to dull the pain at the cost of some precision",
                    cooldown = 2,
                    actionType = CharacterActionType.BUFF,
                    targetingType = TargetingType.SELF,
                    targetIntent = TargetIntent.FRIENDLY,
                    effect = MultiEffect(
                        StatModEffect(
                            value = -15,
                            modDuration = 5,
                            modifierName = "BEER",
                            statGetter = RPGCharacter::damageTakenScalar,
                            attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
                        ),
                        StatModEffect(
                            value = -10,
                            modDuration = 5,
                            modifierName = "BEER",
                            statGetter = RPGCharacter::precision,
                            attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
                        )
                    ),
                    strings = CharacterActionStrings(
                        actionText = "{source} takes a hefty swig of their shitty beer."
                    )
                )
            )
        )

    private val Glasses: Equipment
        get() = Equipment(
            name = "Glasses",
            equipmentRole = EquipmentRole.UTILITY,
            equipEffects = {
                this.precision.addAdditiveMod(7.0, name = "Glasses Precision Bonus")
                this.criticalEffectScalar.addAdditiveMod(25.0, name = "Glasses Critical Effect Increase")
            }
        )

    private val Painkillers: Equipment
        get() = Equipment(
            name = "Painkillers",
            equipmentRole = EquipmentRole.HEALING,
            actions = listOf(
                CharacterAction(
                    displayName = "Painkillers",
                    identifier = "action|painkillers",
                    actionType = CharacterActionType.HEALING,
                    targetingType = TargetingType.SELF,
                    description = "Something to dull the pain and heal you up. Not addictive at all!*",
                    cooldown = 5,
                    targetIntent = TargetIntent.FRIENDLY,
                    effect = ChainEffect(
                        StatModEffect(
                            value = -20,
                            modDuration = 6,
                            statGetter = RPGCharacter::damageTakenScalar,
                            attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
                            modifierName = "Painkillers"
                        ),
                        HealEffect(10, 16, duration = 6, canCrit = false)
                    ),
                    strings = CharacterActionStrings(
                        actionText = "{source} pops some painkillers in their mouth.",
                        effectContinuedText = "{source} feels healthier from the pills.",
                        effectOverText = "{source}'s painkillers wear off."
                    )
                )
            )
        )

    private val Steroids: Equipment
        get() = Equipment(
            name = "Steroids",
            equipmentRole = EquipmentRole.UTILITY,
            actions = listOf(
                CharacterAction(
                    displayName = "Steroid Injection",
                    description = "Inject steroids into a target to boost their power temporarily.",
                    identifier = "action|steroid",
                    cooldown = 5,
                    targetingType = TargetingType.SINGLE_TARGET_INCLUDING_SELF,
                    targetIntent = TargetIntent.FRIENDLY,
                    actionType = CharacterActionType.BUFF,
                    effect = StatModEffect(
                        value = 8,
                        modDuration = 6,
                        statGetter = RPGCharacter::power,
                        attributeModifierType = AttributeModifierType.ADDITIVE,
                        modifierName = "Steroids"
                    ),
                    strings = CharacterActionStrings(
                        actionText = "{source} injects {target} with steroids and they instantly feel more powerful"
                    )
                )
            )
        )

    private val TrashCanLid: Equipment
        get() = Equipment(
            name = "Trash Can Lid",
            equipmentRole = EquipmentRole.DEFENSIVE,
            equipEffects = {
                this.defense.addAdditiveMod(5.0, name = "Trash Can Lid Defense Bonus")
            },
            actions = listOf(
                CharacterAction(
                    displayName = "Trash can lid defend",
                    description = "Defend yourself with your trash can lid",
                    identifier = "action|trashcanliddefend",
                    effect = DefendEffect(10, modDuration = 3, modName = "Trash Can Lid Defend"),
                    cooldown = 6,
                    targetingType = TargetingType.SELF,
                    targetIntent = TargetIntent.FRIENDLY,
                    actionType = CharacterActionType.DEFENSIVE,
                    strings = CharacterActionStrings(
                        actionText = "{source} shifts into a defensive stance with their trash can lid.",
                    )
                )
            )
        )

    private val Hammer: Equipment
        get() = Equipment(
            name = "Hammer",
            equipmentRole = EquipmentRole.OFFENSIVE,
            actions = listOf(
                CharacterAction(
                    effect = DamageHealthEffect(18, 25),
                    displayName = "Hammer Bash",
                    description = "Bash em in the head m8",
                    identifier = "action|hammerbash",
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    strings = CharacterActionStrings(
                        actionText = "{source} raises their hand and swings their hammer at {target}!",
                        successText = "The swing connects with a fleshy thud.",
                        missedText = "{target} steps away just in time and avoids the hammer.",
                        critText = "The blunt side of the hammer connects directly in the head with a sickening crack."
                    )
                ),
                CharacterAction(
                    effect = ChainEffect(
                        DamageHealthEffect(13, 18),
                        DamageHealthEffect(5, 8, duration = 2, canMiss = false, canCrit = false)
                    ),
                    displayName = "Hammer Gash",
                    description = "Use the claw side of the hammer to draw blood.",
                    identifier = "action|hammergash",
                    cooldown = 3,
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    strings = CharacterActionStrings(
                        actionText = "{source} raises their hand and swings the claw-side of the hammer at {target}!",
                        successText = "The claw embeds itself in {target}. {source} yanks it out with some effort. {target} is bleeding out!",
                        missedText = "They whiff the swing entirely.",
                        critText = "A wet thud is heard as the claw of the hammer firmly embeds itself in {target}. {source} nearly knocks them over ripping the claw out. {target} is bleeding out!",
                        effectContinuedText = "{target}'s wound oozes blood.",
                        effectOverText = "{target}'s hammer wound closes up.",
                    )
                )
            ),
            equipEffects = { this.power.addAdditiveMod(2.0, name = "Hammer Power Increase") }
        )

    private val BasicFirstAidKit: Equipment
        get() = Equipment(
            name = "Basic First Aid Kit",
            equipmentRole = EquipmentRole.HEALING,
            actions = listOf(
                CharacterAction(
                    effect = HealEffect(15, 22),
                    displayName = "Bandage",
                    description = "Apply a bandage to a target for some instant healing.",
                    identifier = "action|bandage",
                    cooldown = 2,
                    actionType = CharacterActionType.HEALING,
                    targetingType = TargetingType.SINGLE_TARGET_INCLUDING_SELF,
                    targetIntent = TargetIntent.FRIENDLY,
                    strings = CharacterActionStrings(
                        actionText = "{source} applies a bandage to {target}'s wounds.",
                        critText = "{source} expertly applies a bandage on one of {target}'s wounds."
                    )
                ),
                CharacterAction(
                    effect = DamageHealthEffect(14, 19, duration = 4),
                    displayName = "Used Syringe",
                    description = "Throw a nasty used syringe at someone. Not cool.",
                    identifier = "action|usedsyringe",
                    cooldown = 5,
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    strings = CharacterActionStrings(
                        actionText = "{source} pulls a used syringe out of their first-aid kit and throws it at {target}!",
                        successText = "The needle sinks into {target} and infects them instantly!",
                        missedText = "The needle misses and lands somewhere on the ground. I'm sure someone else will find it later.",
                        effectContinuedText = "{target}'s sketchy syringe infection wreaks havoc on their body.",
                        effectOverText = "{target}'s infection has cleared."
                    )
                )
            ),
            equipEffects = {
                this.healingGivenScalar.addMultiplicativeMod(20.0, name = "First Aid Kit Healing Given Increase")
                this.healingTakenScalar.addMultiplicativeMod(20.0, name = "First Aid Kit Healing Taken Increase")
            }
        )

    private val Knife: Equipment
        get() = Equipment(
            name = "Knife",
            equipmentRole = EquipmentRole.OFFENSIVE,
            actions = listOf(
                CharacterAction(
                    displayName = "Stab",
                    identifier = "action|knifestab",
                    effect = DamageHealthEffect(12, 23),
                    description = "Stabby stab",
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    strings = CharacterActionStrings(
                        actionText = "{source} thrusts their knife at {target}!",
                        successText = "The knife sinks deep into {target}.",
                        missedText = "{target} deftly dodges the knife."
                    )
                )
            ),
        )

    private val Hoodie: Equipment
        get() = Equipment(
            name = "Hoodie",
            equipmentRole = EquipmentRole.DEFENSIVE,
            equipEffects = {
                this.defense.addAdditiveMod(3.0, name = "Hoodie defense bonus")
            },
            actions = listOf(
                CharacterAction(
                    displayName = "Pull hoodie drawstrings",
                    description = "Protect yourself a little more",
                    identifier = "action|pullhoodie",
                    actionType = CharacterActionType.DEFENSIVE,
                    targetingType = TargetingType.SELF,
                    targetIntent = TargetIntent.FRIENDLY,
                    cooldown = 6,
                    effect = DefendEffect(3, modDuration = 6, modName = "Hoodie drawstrings pulled"),
                    strings = CharacterActionStrings(
                        actionText = "{source} wraps their hands around their hoodie drawstrings and pulls tightly!"
                    )
                )
            )
        )

    private val FootballHelmet: Equipment
        get() = Equipment(
            name = "Football Helmet",
            equipmentRole = EquipmentRole.DEFENSIVE,
            equipEffects = { this.defense.addAdditiveMod(6.0, name = "Football Helmet defense bonus") },
        )

    private val LeftoverLasagna: Equipment
        get() = Equipment(
            name = "Leftover Lasagna",
            equipmentRole = EquipmentRole.HEALING,
            equipmentActions = listOf(
                EquipmentAction(
                    CharacterAction(
                        displayName = "Eat leftover lasagna",
                        identifier = "action|eatleftovers",
                        actionType = CharacterActionType.HEALING,
                        targetingType = TargetingType.SELF,
                        targetIntent = TargetIntent.FRIENDLY,
                        cooldown = 5,
                        duration = 5,
                        description = "Eat some cold leftover lasagna",
                        effect = HealEffect(10, 13),
                        strings = CharacterActionStrings(
                            actionText = "{source} opens a container of cold leftover lasagna and takes a huge bite of it!",
                            effectContinuedText = "{source} feels invigorated by their lasagna leftovers.",
                            effectOverText = "{source} is done digesting their lasagna and might be feeling a bit peckish."
                        )
                    ),
                    ActionResource(
                        maxValue = 4,
                        unitNameSingular = "Bite",
                        unitNamePlural = "Bites",
                    )
                )
            )
        )

    private val Bong: Equipment
        get() = Equipment(
            name = "Bong",
            equipmentRole = EquipmentRole.UTILITY,
            actions = listOf(
                CharacterAction(
                    displayName = "Fat Bong Rip",
                    identifier = "action|fatbongrip",
                    description = "Take a fat rip off your bong boiiiii",
                    cooldown = 5,
                    actionType = CharacterActionType.UTILITY,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    effect = ComposeEffect(
                        outer = StatModEffect(
                            -2,
                            modDuration = 2,
                            statGetter = RPGCharacter::precision,
                            attributeModifierType = AttributeModifierType.ADDITIVE,
                            modifierName = "High as balls"
                        ),
                        inner = StatModEffect(
                            -5,
                            4,
                            statGetter = RPGCharacter::precision,
                            attributeModifierType = AttributeModifierType.ADDITIVE,
                            modifierName = "Second-hand weed smoke"
                        ),
                        compose = Composers.dumbApplyOuterToSelf
                    ),
                    strings = CharacterActionStrings(
                        actionText = "{source} puts a lighter to their bong's bowl, inhales strongly, and cough-blows the smoke at {target}'s face!"
                    )
                ),
            )
        )

    private val BoxingGloves: Equipment
        get() = Equipment(
            name = "Boxing Gloves",
            equipmentRole = EquipmentRole.OFFENSIVE,
            equipEffects = {
                this.precision.addAdditiveMod(5.0, name = "Boxing Prowess")
                this.power.addAdditiveMod(5.0, name = "Boxing Prowess")
            },
            actions = listOf(
                CharacterAction(
                    displayName = "Combo Punch",
                    identifier = "action|combopunch",
                    description = "Try a punch combo",
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    cooldown = 3,
                    effect = ComposeEffect(
                        outer = DamageHealthEffect(7, 10, alwaysCrits = true, canMiss = false),
                        inner = RepeatEffect(4, DamageHealthEffect(2, 4, canCrit = false)),
                        compose = {
                            val sumOfPunches = innerResults.sumOf { it.value }
                            when (innerResults.all { it.isSuccessfulNormalHit() }) {
                                true -> {
                                    val critPunchResults = outer.applyEffect(from, to, cycle)
                                    EffectResult.singleResult(
                                        from,
                                        to,
                                        critPunchResults.sumOf { it.value } + sumOfPunches,
                                        actionType = CharacterActionType.DAMAGE,
                                        crit = true
                                    )
                                }
                                false -> {
                                    EffectResult.singleResult(
                                        from,
                                        to,
                                        value = sumOfPunches,
                                        actionType = CharacterActionType.DAMAGE,
                                        miss = innerResults.all { it.miss },
                                        other = innerResults.filter { it.isNormalAttackMiss() }.size.toString()
                                    )
                                }
                            }
                        }
                    ),
                    strings = CharacterActionStrings(
                        actionText = "{source} attempts to combo punches against {target}!",
                        successText = "{other} of the punches connect(s)!",
                        missedText = "They somehow whiffs four consecutive punches. SAD!!",
                        critText = "They successfully execute a brutal combo of four punches and use their momentum to land a nasty uppercut!"
                    )
                )
            )
        )

    private val DebugPants: Equipment
        get() = Equipment(
            name = "Debug Pants",
            equipmentRole = EquipmentRole.DEBUG,
            actions = listOf(
                CharacterAction(
                    displayName = "Debug Attack",
                    description = "Do a huge attack for the sake of hurrying up finishing a game",
                    identifier = "action|debugattack",
                    effect = DamageHealthEffect(800, 1100, canMiss = false),
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    strings = CharacterActionStrings(
                        actionText = "{source} is debug attacking {target}.",
                        successText = "Owie."
                    )
                )
            )
        )

    private val PocketSand: Equipment
        get() = Equipment(
            name = "Pocket Sand",
            equipmentRole = EquipmentRole.DEFENSIVE,
            actions = listOf(
                CharacterAction(
                    displayName = "Throw Pocket Sand",
                    identifier = "action|throwpocketsand",
                    description = "Sh-sh-sha!",
                    actionType = CharacterActionType.DEFENSIVE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    cooldown = 5,
                    effect = StatModEffect(
                        value = -30,
                        modDuration = 3,
                        statGetter = RPGCharacter::precision,
                        attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
                        modifierName = "Pocket Sand Precision Debuff"
                    ),
                    strings = CharacterActionStrings(
                        actionText = "{source} reaches in their pocket and throws a bunch of sand at {target}! They're blinded!"
                    )
                )
            )
        )

    private val Stapler: Equipment
        get() = Equipment(
            name = "Stapler",
            equipmentRole = EquipmentRole.OFFENSIVE,
            actions = listOf(
                CharacterAction(
                    displayName = "Stapler Bash",
                    description = "Stapler bash.",
                    identifier = "action|stapler_bash",
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    effect = DamageHealthEffect(15, 20),
                    strings = CharacterActionStrings(
                        successText = "{source} bashes {target} with their stapler!",
                        missedText = "{source} grips their stapler and tries to bash {target}, but they nimbly dodge the attack."
                    )
                )
            )
        )

    private val JumpKit
        get() = Equipment(
            name = "Car Battery and Jumper Cables",
            equipmentRole = EquipmentRole.OFFENSIVE,
            actions = listOf(
                CharacterAction(
                    displayName = "Jumper cable whip",
                    identifier = "action|cablewhip",
                    description = "Whip someone with your jumper cables",
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    effect = DamageHealthEffect(19, 25),
                    strings = CharacterActionStrings(
                        actionText = "{source} whips their heavy jumper cables at {target}!",
                        successText = "The clamps brutally connect!",
                        missedText = "Unfortunately the clamps miss their mark.",
                    )
                ),
                CharacterAction(
                    displayName = "Shock",
                    identifier = "action|shock",
                    description = "Shock someone with your jumper cables",
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    cooldown = 3,
                    effect = DamageHealthEffect(25, 35),
                    strings = CharacterActionStrings(
                        actionText = "{source} tries to clamp their jumper cables onto {target}!",
                        successText = "They get zapped.",
                        missedText = "It missed tho.",
                        critText = "They get hella zapped.",
                        triggeredText = "Their brains are scrambled!"
                    ),
                    triggers = {
                        onCrit {
                            StatModEffect(
                                value = 25,
                                modDuration = 3,
                                statGetter = RPGCharacter::damageTakenScalar,
                                attributeModifierType = AttributeModifierType.ADDITIVE,
                                modifierName = "Critically shocked",
                            )
                        }
                    }
                )
            )
        )

    private val PepperSpray
        get() = Equipment(
            name = "Pepper Spray",
            equipmentRole = EquipmentRole.DEFENSIVE,
            equipmentActions = listOf(
                EquipmentAction(
                    CharacterAction(
                        displayName = "Spray pepper spray",
                        identifier = "action|pepperspray",
                        description = "Blind someone with your pepper spray",
                        actionType = CharacterActionType.UTILITY,
                        targetingType = TargetingType.SINGLE_TARGET,
                        targetIntent = TargetIntent.HOSTILE,
                        cooldown = 2,
                        effect = StatModEffect(
                            value = -75,
                            modDuration = 3,
                            statGetter = RPGCharacter::precision,
                            attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
                            modifierName = "Blinded by pepper spray"
                        ),
                        strings = CharacterActionStrings(
                            actionText = "{source} sprays pepper spray into {target}'s eyes and blinds them!",
                            missedText = "{source} tries to spray pepper spray at {target}, but a breeze carries it away.",
                            effectOverText = "{target} can see again."
                        )
                    ),
                    actionResource = ActionResource(
                        maxValue = 3,
                        unitNameSingular = "spray",
                        unitNamePlural = "sprays",
                    )
                )
            )
        )

    val Pillow
        get() = Equipment(
            name = "Pillow",
            equipmentRole = EquipmentRole.DEFENSIVE,
            actions = listOf(
                CharacterAction(
                    displayName = "Pillow Defend",
                    identifier = "action|pillowdefend",
                    description = "Defend yourself with your pillow",
                    actionType = CharacterActionType.DEFENSIVE,
                    targetingType = TargetingType.SELF,
                    targetIntent = TargetIntent.FRIENDLY,
                    cooldown = 3,
                    effect = DefendEffect(
                        value = 15,
                        modDuration = 3,
                        modName = "Pillow defense",
                        attributeModifierType = AttributeModifierType.MULTIPLICATIVE
                    ),
                    strings = CharacterActionStrings(
                        actionText = "{source} holds up their pillow in front of them in a defensive pose.",
                    )
                ),
                CharacterAction(
                    displayName = "Pillow Smash",
                    identifier = "action|pillowsmash",
                    description = "Smack someone with your pillow",
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    targetIntent = TargetIntent.HOSTILE,
                    effect = DamageHealthEffect(7, 11),
                    strings = CharacterActionStrings(
                        successText = "{source} swings their pillow at {target} and smacks them in the face.",
                        missedText = "{source} swings their pillow at {target}, but misses pathetically.",
                        critText = "{source} swings their pillow at {target}, and feathers go flying everywhere, obscuring their vision!"
                    ),
                    triggers = {
                        onCrit {
                            StatModEffect(
                                value = -50,
                                modDuration = 3,
                                statGetter = RPGCharacter::precision,
                                attributeModifierType = AttributeModifierType.MULTIPLICATIVE
                            )
                        }
                    }

                )
            )
        )

    val Sandbags
        get() = Equipment(
            name = "Sandbags",
            equipmentRole = EquipmentRole.DEFENSIVE,
            equipmentActions = listOf(
                EquipmentAction(
                    CharacterAction(
                        displayName = "Fortify",
                        identifier = "action|fortify",
                        description = "Use your sandbags to make a defensive position",
                        cooldown = 6,
                        actionType = CharacterActionType.DEFENSIVE,
                        targetingType = TargetingType.SINGLE_TARGET_INCLUDING_SELF,
                        targetIntent = TargetIntent.FRIENDLY,
                        effect = DefendEffect(35, modDuration = 4, modName = "Sandbag Fortification"),
                        strings = CharacterActionStrings(
                            successText = "{source} places sandbags in a defensive position around {target}.",
                            effectOverText = "{source}'s sandbag fortification falls apart." // TODO allow for this to happen
                        )
                    )
                )
            )
        )


}

internal fun main(args: Array<String>) {
    println("equipment roles")
    println(
        allEquipment
            .groupBy { it.equipmentRole }
            .mapValues { (_, equipmentList) -> equipmentList.size }
    )
    println("\nequipment action types")
    println(
        allEquipment
            .flatMap { equipment -> equipment.equipmentActions.map { it.characterAction } }
            .groupBy { it.actionType }
            .mapValues { (_, types) -> types.size }
    )
}