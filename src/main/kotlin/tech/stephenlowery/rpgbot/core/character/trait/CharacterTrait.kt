package tech.stephenlowery.rpgbot.core.character.trait

import tech.stephenlowery.rpgbot.core.action.CharacterAction
import tech.stephenlowery.rpgbot.core.character.RPGCharacter

class CharacterTrait(
    val name: String,
    val description: String,
    val technicalDescription: String,
    val criteria: RPGCharacter.() -> Boolean,
    traitEffects: TraitEffectBuilder.() -> Unit
) {

    private var abilitiesGranted: List<CharacterAction>
    private var attributeEffects: RPGCharacter.() -> Unit

    init {
        TraitEffectBuilder().apply(traitEffects).also {
            abilitiesGranted = it.abilities
            attributeEffects = it.effects
        }
    }

    fun applyEffects(character: RPGCharacter) = character.apply(attributeEffects)

    class TraitEffectBuilder {

        internal var abilities: List<CharacterAction> = mutableListOf()
        internal var effects: RPGCharacter.() -> Unit = {}

        fun withEffects(effects: RPGCharacter.() -> Unit) {
            this.effects = effects
        }

        fun givesAbility(body: () -> CharacterAction) {
            this.abilities += body()
        }

        fun givesAbilities(body: () -> List<CharacterAction>) {
            this.abilities += body()
        }

        infix fun CharacterAction.and(other: CharacterAction): List<CharacterAction> {
            return listOf(this, other)
        }

        infix fun List<CharacterAction>.and(other: CharacterAction): List<CharacterAction> {
            return this + other
        }

    }
}