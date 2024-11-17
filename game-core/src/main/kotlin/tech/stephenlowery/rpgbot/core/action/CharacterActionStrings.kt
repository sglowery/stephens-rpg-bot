package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.character.RPGCharacter

data class CharacterActionStrings(
    private val queuedText: String,
    private val actionText: String,
    private val successText: String? = null,
    private val missedText: String? = null,
    private val critText: String? = null,
    private val effectContinuedText: String? = null,
    private val effectOverText: String? = null,
    private val effectChainedText: String? = null,
) {

    fun getFormattedEffectResultString(effectResult: EffectResult): String {
        return listOfNotNullOrEmpty(
            getActionText(effectResult),
            getEffectResultText(effectResult),
            getExtraText(effectResult)
        ).joinToString("\n")
            .addDamageAndHealing(effectResult)
            .formatFromEffectResult(effectResult)
    }

    private fun getEffectResultText(effectResult: EffectResult): String? = when {
        effectResult.occupied  -> null
        effectResult.miss      -> this.missedText
        effectResult.continued -> this.effectContinuedText ?: this.successText
        effectResult.chained   -> this.effectChainedText
        effectResult.crit      -> this.critText ?: this.successText
        else                   -> this.successText
    }

    private fun getExtraText(effectResult: EffectResult): String? {
        return when {
            effectResult.continued && effectResult.expired -> this.effectOverText
            else                                           -> null
        }
    }

    fun getFormattedQueuedText(from: RPGCharacter, to: RPGCharacter) =
        queuedText.formatCharacterNames(from.name, to.name)

    private fun getActionText(effectResult: EffectResult): String? =
        if (effectResult.continued || effectResult.expired || effectResult.chained)
            null
        else
            actionText

    private fun String.formatFromEffectResult(effectResult: EffectResult): String {
        return this.formatEffectResultString(
            target = effectResult.target.name,
            value = effectResult.value.toString(),
            source = effectResult.source.name,
            other = effectResult.other
        )
    }

    private fun String.formatEffectResultString(target: String, value: String, source: String, other: String?): String {
        return this.formatCharacterNames(source, target)
            .replace("{value}", value)
            .replace("{other}", other ?: "")
    }

    private fun String.formatCharacterNames(source: String, target: String): String {
        return this.replace("{target}", target)
            .replace("{source}", source)

    }

    private fun String.addDamageAndHealing(effectResult: EffectResult): String {
        if (effectResult.actionType == CharacterActionType.OTHER || effectResult.miss) {
            return this
        }
        val critText = if (effectResult.crit) "critical " else ""
        return "$this (" +
                when (effectResult.actionType) {
                    CharacterActionType.DAMAGE      -> "{value} ${critText}damage"
                    CharacterActionType.HEALING     -> "{value} ${critText}healing"
                    CharacterActionType.DAMAGE_HEAL -> "{value} ${critText}damage, {other} healing"
                    else                            -> (if (effectResult.value >= 0) "+" else "-") + "{value}"
                } + ")"
    }

    @Suppress("UNCHECKED_CAST")
    private fun listOfNotNullOrEmpty(vararg possiblyNullOrEmptyStrings: String?): List<String> {
        return possiblyNullOrEmptyStrings.filter { !it.isNullOrEmpty() }.toList() as List<String>
    }
}