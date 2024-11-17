package tech.stephenlowery.rpgbot.core.character.attribute

class Attribute(
    val name: String,
    var base: Double,
    var min: Int? = null,
    var max: Int? = null,
    val displayValueFn: (Int) -> String = Int::toString,
) {

    private var additiveModifiers = mutableListOf<AttributeModifier>()
    private var multiplyModifiers = mutableListOf<AttributeModifier>()

    fun cycleClearAndConsolidateModifiers() {
        cycleModifiers()
        clearExpiredModifiers()
        consolidateModifiers()
    }

    fun addAdditiveMod(value: Double, duration: Int = -1, name: String? = null) {
        addModifier(additiveModifiers, value, duration, name, AttributeModifierType.ADDITIVE)
    }

    fun addMultiplicativeMod(value: Double, duration: Int = -1, name: String? = null) {
        addModifier(multiplyModifiers, value, duration, name, AttributeModifierType.MULTIPLICATIVE)
    }

    fun value(): Int = ((1.0 + multiplyModifiers.sum() / 100.0) * (base + additiveModifiers.sum())).toInt()

    fun displayValue(): String = displayValueFn(value())

    fun reset() {
        additiveModifiers = mutableListOf()
        multiplyModifiers = mutableListOf()
    }

    fun consolidateModifiers() {
        additiveModifiers = consolidateModifierSet(additiveModifiers)
    }

    fun getTemporaryAndNamedModifiers(): List<String> {
        return (additiveModifiers + multiplyModifiers).filter { (!it.isPermanent() || it.name != null) && it.value != 0.0 }
            .map {
                val baseText = "${it.name} -- ${it.displayValue(displayValueFn)}"
                val turnsLeft = it.duration - it.turnsActive
                val turnsText = if (turnsLeft == 1) "turn" else "turns"
                if (it.isPermanent())
                    baseText
                else
                    "$baseText ($turnsLeft $turnsText remaining)"
            }
    }

    fun hasNamedModifier(name: String): Boolean {
        return additiveModifiers.any { it.name == name } || multiplyModifiers.any { it.name == name }
    }

    private fun clearExpiredModifiers() {
        additiveModifiers.removeIf { it.isExpired() }
        multiplyModifiers.removeIf { it.isExpired() }
    }

    private fun addModifier(modifierList: MutableList<AttributeModifier>, value: Double, duration: Int, name: String?, attributeModifierType: AttributeModifierType) {
        modifierList.add(AttributeModifier(value, duration, name, attributeModifierType))
        consolidateModifiers()
    }

    private fun cycleModifiers() {
        listOf(additiveModifiers, multiplyModifiers).flatten().forEach { it.cycle() }
    }

    private fun consolidateModifierSet(modifierSet: MutableList<AttributeModifier>): MutableList<AttributeModifier> {
        val (permanentGenericModifiers, temporaryAndNamedModifiers) = modifierSet.partition { it.isPermanent() && it.name == null }
        val consolidatedModifier = AttributeModifier(value = permanentGenericModifiers.sumOf { it.value }, modifierType = AttributeModifierType.ADDITIVE)
        return if (min != null || max != null)
            bringPermanentModifiersWithinBounds(consolidatedModifier, temporaryAndNamedModifiers)
        else
            (listOf(consolidatedModifier) + temporaryAndNamedModifiers).toMutableList()
    }

    private fun bringPermanentModifiersWithinBounds(permanent: AttributeModifier, temporaryAndUnique: List<AttributeModifier>): MutableList<AttributeModifier> {
        val irreconcilable = temporaryAndUnique.sumOf { it.value }
        val bottomBound = min?.minus(irreconcilable + base)
        val topBound = max?.minus(irreconcilable + base)
        val value = permanent.value.coerceIn(bottomBound, topBound)
        return mutableListOf(AttributeModifier(value = value, modifierType = AttributeModifierType.ADDITIVE)).apply { addAll(temporaryAndUnique) }
    }
}

private fun MutableList<AttributeModifier>.sum(): Double = this.getValuesOfActiveModifiers().sum()

private fun MutableList<AttributeModifier>.getValuesOfActiveModifiers(): List<Double> = this.filterNot { it.isExpired() }.map { it.value }