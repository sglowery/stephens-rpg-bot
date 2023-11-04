package tech.stephenlowery.rpgbot.core.character.trait.impl

import tech.stephenlowery.rpgbot.assets.CharacterActionAssets
import tech.stephenlowery.rpgbot.core.character.RPGCharacter
import tech.stephenlowery.rpgbot.core.character.trait.CharacterTrait

object CharacterTraits {

    fun getQualifiedCharacterTraitsFor(character: RPGCharacter) = allTraits.filter { character qualifiesFor it }

    private val allTraits
        get() = listOf(reckless, lifestealer, precise, brutish)

    private val reckless
        get() = CharacterTrait(
            name = "Reckless",
            description = "You are powerful but imprecise; your reckless nature means you do more damage but are more vulnerable to it too.",
            technicalDescription = "Increases damage dealt and taken by 20%",
            criteria = { power.value() > 10 && precision.value() < 7 },
        ) {
            withEffects {
                damageGivenScalar.addAdditiveMod(20.0, name = "reckless")
                damageTakenScalar.addAdditiveMod(20.0, name = "reckless")
            }

        }

    private val lifestealer
        get() = CharacterTrait(
            name = "Lifestealer",
            description = "Your frail nature has forced you to find other means to sustain yourself. You have access to the Life Steal ability and damage taken is slightly reduced.",
            technicalDescription = "Reduces damage taken by 10%",
            criteria = { health.base < 100 },
        ) {
            withEffects {
                damageTakenScalar.addAdditiveMod(-10.0, name = "lifestealer")
            }

        }

    private val precise
        get() = CharacterTrait(
            name = "Precise",
            description = "Your deadly precision taught you how to deliver devastating blows, significantly increasing your chance to deal critical hits and increasing the damage they do.",
            technicalDescription = "Critical change increased by 30% and critical damage dealt increased by 50%",
            criteria = { precision.value() > 12 },
        ) {
            withEffects {
                criticalChance.addAdditiveMod(30.0, name = "precise")
                criticalDamage.addAdditiveMod(50.0, name = "precise")
            }
        }

    private val brutish
        get() = CharacterTrait(
            name = "Brutish",
            description = "'Sometimes the best offense is a good defense.' You benefit twice as much from defense and you have an extra powerful defend ability.",
            technicalDescription = "Defense multiplier increased by 100%",
            criteria = { defense.value() > 12 && power.value() < 8 },
        ) {
            withEffects {
                defense.addMultiplicativeMod(1.0, name = "brutish")
            }
            givesAbility { CharacterActionAssets.SuperDefend }
        }

}

private infix fun RPGCharacter.qualifiesFor(trait: CharacterTrait): Boolean = trait.criteria(this)