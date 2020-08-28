package tech.stephenlowery.rpgbot.models.character

import tech.stephenlowery.util.takeIfNotNull

class Attribute(val name: String, var base: Double, val min: Int? = null, val max: Int? = null, val displayValueFn: (Int) -> String = Int::toString) {

    var additiveModifiers = mutableListOf<AttributeModifier>()
    var multiplyModifiers = mutableListOf(AttributeModifier(1.0, -1))

    fun cycleClearAndConsolidateModifiers() {
        cycleModifiers()
        clearExpiredModifiers()
        consolidateModifiers()
    }

    fun addAdditiveMod(value: Double, duration: Int = -1, name: String? = null) {
        additiveModifiers.add(AttributeModifier(value, duration, name))
    }

    fun addMultiplicativeMod(value: Double, duration: Int = -1, name: String? = null) {
        multiplyModifiers.add(AttributeModifier(value, duration, name))
    }

    fun value(): Int = (multiplyModifiers.sum() * (base + additiveModifiers.sum())).toInt().coerceIn(min, max)

    fun displayValue(): String = displayValueFn(value())

    internal fun cycleModifiers() {
        listOf(additiveModifiers, multiplyModifiers).forEach { modifierSet ->
            modifierSet.forEach { it.cycle() }
        }
    }

    internal fun consolidateModifiers() {
        additiveModifiers = consolidateModifierSet(additiveModifiers)
    }

    internal fun clearExpiredModifiers() {
        additiveModifiers.removeIf { it.isExpired() }
        multiplyModifiers.removeIf { it.isExpired() }
    }

    private fun consolidateModifierSet(modifierSet: MutableList<AttributeModifier>): MutableList<AttributeModifier> {
        val permanentGenericModifiers = modifierSet.filter { it.isPermanent() && it.name == null }
        val temporaryAndNamedModifiers = modifierSet.filter { !it.isPermanent() || it.name != null }
        val consolidatedModifier = AttributeModifier(value = permanentGenericModifiers.sumByDouble { it.value })
        return if (min != null || max != null)
            bringPermanentModifiersWithinBounds(consolidatedModifier, temporaryAndNamedModifiers) else
            mutableListOf(consolidatedModifier).apply { addAll(temporaryAndNamedModifiers) }
    }

    private fun bringPermanentModifiersWithinBounds(permanent: AttributeModifier, temporaryAndUnique: List<AttributeModifier>): MutableList<AttributeModifier> {
        val unreconcilable = temporaryAndUnique.sumByDouble { it.value }
        val bottomBound = min.takeIfNotNull()?.minus(unreconcilable + base)
        val topBound = max.takeIfNotNull()?.minus(unreconcilable + base)
        return mutableListOf(AttributeModifier(value = permanent.value.coerceIn(bottomBound, topBound))).apply { addAll(temporaryAndUnique) }
    }
}