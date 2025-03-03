package tech.stephenlowery.rpgbot.core.action

import kotlin.math.abs

class ActionResource(
    private val minValue: Int = 0,
    private val maxValue: Int,
    private val regenPerCycle: Int = 0,
    private val cyclesToRegenerate: Int = 0,
    private val canRegenerateFromMinimum: Boolean = false,
    private val canExceedMinimum: Boolean = false,
    private val canExceedMax: Boolean = false,
    private val unitNameSingular: String,
    private val unitNamePlural: String?,
    private val valuePerUse: Int = -1,
    private val valueDisplayFn: (Int) -> String = Int::toString,
    private val includeMaxValueInString: Boolean = false,
    startingValue: Int = maxValue,
    private val usablePredicate: (ActionResource) -> Boolean = { it.value >= abs(valuePerUse) },
) {

    var value: Int = startingValue
        private set

    private var cycle: Int = 0

    fun cycle() {
        cycle++
        if (value >= minValue && canRegenerateFromMinimum) {
            if (cycle > minValue && cycle % cyclesToRegenerate == 0) {
                value += regenPerCycle
            }
            if (!canExceedMinimum && value < minValue) {
                value = minValue
            }
            if (!canExceedMax && value > maxValue) {
                value = maxValue
            }
        }
    }

    fun useResource() {
        value += valuePerUse
        if (!canExceedMinimum && value < minValue) {
            value = minValue
        }
        if (!canExceedMax && value > maxValue) {
            value = maxValue
        }
    }

    fun isUsable(): Boolean {
        return usablePredicate(this)
    }

    fun unitString(): String {
        val currentValue = valueDisplayFn(value)
        val maxDisplayValue = if (includeMaxValueInString) " / ${valueDisplayFn(maxValue)}" else ""
        val unitName = if (value != 1) unitNamePlural else unitNameSingular
        return "$currentValue$maxDisplayValue $unitName"
    }
}