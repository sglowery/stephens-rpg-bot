package tech.stephenlowery.rpgbot.assets

import tech.stephenlowery.rpgbot.assets.EquipmentAssets.allEquipment
import tech.stephenlowery.rpgbot.core.Equipment
import tech.stephenlowery.rpgbot.core.EquipmentRole
import tech.stephenlowery.rpgbot.core.action.*
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DamageHealthEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.DefendEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.impl.HealEffect
import tech.stephenlowery.rpgbot.core.action.action_effect.meta.*
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.attribute.AttributeModifierType

object EquipmentAssets {

    val allEquipment = listOf(
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
    )

    private val BeerBottle: Equipment
        get() = Equipment(
            name = "Beer Bottle",
            equipmentRole = EquipmentRole.UTILITY,
            actions = listOf(
                CharacterAction(
                    displayName = "Beer bottle bash",
                    identifier = "action|beerbottlebash",
                    description = "Bash a foo with a bottle of beer",
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    effect = DamageHealthEffect(17, 23),
                    strings = CharacterActionStrings(
                        queuedText = "You grip an empty beer bottle and get ready to crack some heads",
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
                        queuedText = "You crack open a cold one",
                        actionText = "{source} takes a hefty swig of their shitty beer."
                    )
                )
            )
        )

    private val Glasses: Equipment
        get() = Equipment(
            name = "Glasses",
            equipmentRole = EquipmentRole.UTILITY,
            actions = emptyList(),
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
                    effect = ChainEffect(
                        StatModEffect(
                            value = -20,
                            modDuration = 6,
                            statGetter = RPGCharacter::damageTakenScalar,
                            attributeModifierType = AttributeModifierType.MULTIPLICATIVE,
                            modifierName = "Painkillers"
                        ),
                        HealEffect(25, 30, duration = 6)
                    ),
                    strings = CharacterActionStrings(
                        queuedText = "You pop open your bottle of painkillers.",
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
                    actionType = CharacterActionType.BUFF,
                    effect = StatModEffect(
                        value = 8,
                        modDuration = 6,
                        statGetter = RPGCharacter::power,
                        attributeModifierType = AttributeModifierType.ADDITIVE,
                        modifierName = "Steroids"
                    ),
                    strings = CharacterActionStrings(
                        queuedText = "You pull out a needle and do that stupid flicking thing.",
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
                    actionType = CharacterActionType.DEFENSIVE,
                    strings = CharacterActionStrings(
                        queuedText = "You prepare to shift your stance.",
                        actionText = "{source} shifts into a defensive stance with their trash can lid.",
                    )
                ),
                CharacterAction(
                    displayName = "Trash can lid bash",
                    identifier = "action|trashcanlidbash",
                    description = "Bash your opponent with your dirty trash can lid",
                    cooldown = 2,
                    targetingType = TargetingType.SINGLE_TARGET,
                    actionType = CharacterActionType.DAMAGE,
                    effect = DamageHealthEffect(11, 15),
                    strings = CharacterActionStrings(
                        "You get ready to bash {target} with your trash can lid",
                        actionText = "{source} pulls their arm back and swings their metal trash can lid at {target}!",
                        successText = "{target} receives a face-full of lid.",
                        missedText = "They whiff it, but the scent of garbage juice wafts around and makes the area smell a little unpleasant."
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
                    strings = CharacterActionStrings(
                        queuedText = "You get ready to bash {target} with the blunt side of your hammer.",
                        actionText = "{source} raises their hand and swings their hammer at {target}!",
                        successText = "The swing connects with a fleshy thud.",
                        critText = "The blunt side of the hammer connects directly in the head with a sickening crack.",
                        missedText = "{target} steps away just in time and avoids the hammer."
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
                    strings = CharacterActionStrings(
                        queuedText = "You get ready to use the sharp side of the hammer to draw some blood.",
                        actionText = "{source} raises their hand and swings the claw-side of the hammer at {target}!",
                        successText = "The claw embeds itself in {target}. {source} yanks it out with some effort. {target} is bleeding out!",
                        critText = "A wet thud is heard as the claw of the hammer firmly embeds itself in {target}. {source} nearly knocks them over ripping the claw out. {target} is bleeding out!",
                        missedText = "They whiff the swing entirely.",
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
                    effect = HealEffect(17, 25),
                    displayName = "Bandage",
                    description = "Apply a bandage to a target for some instant healing.",
                    identifier = "action|bandage",
                    cooldown = 2,
                    actionType = CharacterActionType.HEALING,
                    targetingType = TargetingType.SINGLE_TARGET_INCLUDING_SELF,
                    strings = CharacterActionStrings(
                        queuedText = "You grab a bandage from your kit.",
                        successText = "{source} applies a bandage to {target}'s wounds.",
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
                    strings = CharacterActionStrings(
                        queuedText = "You grab a used syringe out of your kit. Why is it still in there? Come on.",
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
                    strings = CharacterActionStrings(
                        queuedText = "You grip your knife and get ready to sink it into {target}.",
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
                    cooldown = 5,
                    effect = DefendEffect(3, modDuration = 5, modName = "Hoodie drawstrings pulled"),
                    strings = CharacterActionStrings(
                        queuedText = "You wrap your hands tightly around your hoodie's drawstrings",
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
            actions = listOf(
                CharacterAction(
                    displayName = "Headbutt",
                    identifier = "action|headbutt",
                    effect = DamageHealthEffect(12, 23),
                    description = "Headbutt",
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
                    strings = CharacterActionStrings(
                        queuedText = "You prepare to headbutt {target}.",
                        actionText = "{source} headbutts {target} with their football helmet!",
                        successText = "It brutally connects.",
                        missedText = "It misses and they nearly fall over."
                    )
                )
            )
        )

    private val LeftoverLasagna: Equipment
        get() = Equipment(
            name = "Leftover Lasagna",
            equipmentRole = EquipmentRole.HEALING,
            actions = listOf(
                CharacterAction(
                    displayName = "Eat leftover lasagna",
                    identifier = "action|eatleftovers",
                    actionType = CharacterActionType.HEALING,
                    targetingType = TargetingType.SELF,
                    cooldown = 5,
                    duration = 5,
                    description = "Eat some cold leftover lasagna",
                    effect = HealEffect(13, 19),
                    strings = CharacterActionStrings(
                        queuedText = "You're going to eat some cold leftover lasagna.",
                        actionText = "{source} opens a container of cold leftover lasagna and takes a huge bite of it!",
                        effectContinuedText = "{source} feels invigorated by their lasagna leftovers.",
                        effectOverText = "{source} is done digesting their lasagna and might be feeling a bit peckish."
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
                    actionType = CharacterActionType.DAMAGE,
                    targetingType = TargetingType.SINGLE_TARGET,
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
                        queuedText = "You ready yourself to take a fat rip off your bong.",
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
                    cooldown = 3,
                    effect = ComposeEffect(
                        outer = DamageHealthEffect(10, 15, alwaysCrits = true, canMiss = false),
                        inner = RepeatEffect(4, DamageHealthEffect(7, 11, canCrit = false)),
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
                        queuedText = "You get ready to try to combo a bunch of punches on {target}.",
                        actionText = "{source} attempts to combo punches against {target}!",
                        successText = "They miss {other} punches, but get at least one in.",
                        missedText = "They somehow whiffs four consecutive punches. SAD!!",
                        critText = "They successfully execute a brutal combo of four punches and use their momentum to land a nasty uppercut!"
                    )
                )
            )
        )



}

fun main(args: Array<String>) {
    println("equipment roles")
    println(
        allEquipment
            .groupBy { it.equipmentRole }
            .mapValues { (_, equipmentList) -> equipmentList.size }
    )
    println("equipment action types")
    println(
        allEquipment
            .flatMap { it.actions }
            .groupBy { it.actionType }
            .mapValues { (_, types) -> types.size }
    )
}