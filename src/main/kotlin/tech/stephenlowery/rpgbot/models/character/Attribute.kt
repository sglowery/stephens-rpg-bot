package tech.stephenlowery.rpgbot.models.character

class Attribute(
    val name: String,
    var base: Double,
    val min: Int? = null,
    val max: Int? = null,
    val displayValueFn: (Int) -> String = Int::toString) {

    var additiveModifiers = mutableListOf<AttributeModifier>()
    var multiplyModifiers = mutableListOf(AttributeModifier(1.0, -1))

    fun cycleClearAndConsolidateModifiers() {
        cycleModifiers()
        clearExpiredModifiers()
        consolidateModifiers()
    }

    fun addAdditiveMod(value: Double, duration: Int = -1, name: String? = null) {
        additiveModifiers.add(AttributeModifier(value, duration, name))
        consolidateModifiers()
    }

    fun addMultiplicativeMod(value: Double, duration: Int = -1, name: String? = null) {
        multiplyModifiers.add(AttributeModifier(value, duration, name))
        consolidateModifiers()
    }

    fun value(): Int = (multiplyModifiers.sum() * (base + additiveModifiers.sum())).toInt()

    fun displayValue(): String = displayValueFn(value())

    fun reset() {
        additiveModifiers = mutableListOf()
        multiplyModifiers = mutableListOf(AttributeModifier(1.0, -1))
    }

    internal fun consolidateModifiers() {
        additiveModifiers = consolidateModifierSet(additiveModifiers)
    }

    internal fun clearExpiredModifiers() {
        additiveModifiers.removeIf { it.isExpired() }
        multiplyModifiers.removeIf { it.isExpired() }
    }

    private fun cycleModifiers() {
        listOf(additiveModifiers, multiplyModifiers).forEach { modifierSet ->
            modifierSet.forEach { it.cycle() }
        }
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
        val irreconcilable = temporaryAndUnique.sumByDouble { it.value }
        val bottomBound = min?.minus(irreconcilable + base)
        val topBound = max?.minus(irreconcilable + base)
        return mutableListOf(AttributeModifier(value = permanent.value.coerceIn(bottomBound, topBound))).apply { addAll(temporaryAndUnique) }
    }
}