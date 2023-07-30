package tech.stephenlowery.rpgbot.core.character.attribute

class Attribute(
    val name: String,
    var base: Double,
    var min: Int? = null,
    var max: Int? = null,
    val displayValueFn: (Int) -> String = Int::toString,
) {
    
    var additiveModifiers = mutableListOf<AttributeModifier>()
    var multiplyModifiers = mutableListOf(AttributeModifier(1.0, -1))
    
    fun cycleClearAndConsolidateModifiers() {
        cycleModifiers()
        clearExpiredModifiers()
        consolidateModifiers()
    }
    
    fun addAdditiveMod(value: Double, duration: Int = -1, name: String? = null) {
        addModifier(additiveModifiers, value, duration, name)
    }
    
    fun addMultiplicativeMod(value: Double, duration: Int = -1, name: String? = null) {
        addModifier(multiplyModifiers, value, duration, name)
    }
    
    fun value(): Int = (multiplyModifiers.sum() * (base + additiveModifiers.sum())).toInt()
    
    fun displayValue(): String = displayValueFn(value())
    
    fun reset() {
        additiveModifiers = mutableListOf()
        multiplyModifiers = mutableListOf(AttributeModifier(1.0, -1))
    }
    
    fun consolidateModifiers() {
        additiveModifiers = consolidateModifierSet(additiveModifiers)
    }
    
    private fun clearExpiredModifiers() {
        additiveModifiers.removeIf { it.isExpired() }
        multiplyModifiers.removeIf { it.isExpired() }
    }

    private fun addModifier(modifierList: MutableList<AttributeModifier>, value: Double, duration: Int, name: String?) {
        modifierList.add(AttributeModifier(value, duration, name))
        consolidateModifiers()
    }
    
    private fun cycleModifiers() {
        listOf(additiveModifiers, multiplyModifiers).flatten().forEach { it.cycle() }
    }
    
    private fun consolidateModifierSet(modifierSet: MutableList<AttributeModifier>): MutableList<AttributeModifier> {
        val (permanentGenericModifiers, temporaryAndNamedModifiers) = modifierSet.partition { it.isPermanent() && it.name == null }
        val consolidatedModifier = AttributeModifier(value = permanentGenericModifiers.sumOf { it.value })
        return if (min != null || max != null)
            bringPermanentModifiersWithinBounds(consolidatedModifier, temporaryAndNamedModifiers)
        else
            mutableListOf(consolidatedModifier).apply { addAll(temporaryAndNamedModifiers) }
    }
    
    private fun bringPermanentModifiersWithinBounds(permanent: AttributeModifier, temporaryAndUnique: List<AttributeModifier>): MutableList<AttributeModifier> {
        val irreconcilable = temporaryAndUnique.sumOf { it.value }
        val bottomBound = min?.minus(irreconcilable + base)
        val topBound = max?.minus(irreconcilable + base)
        return mutableListOf(AttributeModifier(value = permanent.value.coerceIn(bottomBound, topBound))).apply { addAll(temporaryAndUnique) }
    }
}

private fun MutableList<AttributeModifier>.sum(): Double = this.getValuesOfActiveModifiers().sum()

private fun MutableList<AttributeModifier>.getValuesOfActiveModifiers(): List<Double> = this.filter { !it.isExpired() }.map { it.value }