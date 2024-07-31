package tech.stephenlowery.rpgbot.core.action

import tech.stephenlowery.rpgbot.core.character.RPGCharacter

data class CharacterActionStrings(
    private val queuedText: String,
    private val actionText: String = "",
    private val successText: String = "",
    private val missedText: String = "",
    private val failedText: String = "",
    private val effectOverText: String = "",
    private val critText: String = "",
    private val effectContinuedText: String = "",
    private val effectChainedText: String = "",
) {

    fun getFormattedEffectResultString(effectResult: EffectResult): String {
        return listOfNotNull(
            getActionText(effectResult),
            getBaseText(effectResult),
            getExtraText(effectResult)
        ).joinToString("\n")
            .formatFromEffectResult(effectResult)
    }

    private fun getBaseText(effectResult: EffectResult): String? = when {
        effectResult.occupied  -> null
        effectResult.miss      -> this.missedText
        effectResult.continued -> this.effectContinuedText
        effectResult.chained   -> this.effectChainedText
        effectResult.crit      -> this.critText.takeUnless { it.isEmpty() } ?: this.successText
        else                   -> this.successText
    }

    private fun getExtraText(effectResult: EffectResult): String? {
        return when {
            effectResult.continued && effectResult.expired -> this.effectOverText
            else                                           -> null
        }
    }

    fun getFormattedQueuedText(from: RPGCharacter, to: RPGCharacter?) = queuedText.formatFromEffectResult(EffectResult(source = from, target = to))

    private fun getActionText(effectResult: EffectResult): String? =
        if (effectResult.continued || effectResult.expired || effectResult.chained || actionText.isEmpty())
            null
        else
            actionText

    private fun String.formatFromEffectResult(effectResult: EffectResult): String {
        return this.replace("{target}", effectResult.target?.name ?: "")
            .replace("{value}", effectResult.value.toString())
            .replace("{source}", effectResult.source?.name ?: "")
            .replace("{other}", effectResult.other ?: "")
    }
}