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
    private val effectChainedText: String = ""
) {
    fun getFormattedEffectResultString(effectResult: EffectResult): String {
        return when {
            effectResult.miss                              -> this.missedText
            effectResult.crit && !effectResult.miss        -> this.critText
            effectResult.continued && effectResult.expired -> this.effectOverText
            effectResult.continued && !effectResult.miss   -> this.effectContinuedText
            effectResult.chained && !effectResult.miss     -> this.effectChainedText
            !effectResult.crit && !effectResult.miss       -> this.successText
            else                                           -> throw UnresolvableEffectResultParameterException("Effect result has unresolvable parameters: ${effectResult}")
        }.formatWithActionText(effectResult, actionText).formatFromEffectResult(effectResult)
    }

    fun getFormattedQueuedText(from: RPGCharacter, to: RPGCharacter?) = queuedText.formatFromEffectResult(EffectResult(source = from, target = to))

    private fun String.formatWithActionText(effectResult: EffectResult, actionText: String): String {
        return (if (!effectResult.continued && !effectResult.expired && !effectResult.chained && actionText.isNotEmpty()) actionText + "\n" else "") + this
    }

    private fun String.formatFromEffectResult(effectResult: EffectResult): String {
        return this.replace("{target}", effectResult.target?.name ?: "")
            .replace("{value}", effectResult.value.toString())
            .replace("{source}", effectResult.source?.name ?: "")
            .replace("{other}", effectResult.other ?: "")
    }
}

class UnresolvableEffectResultParameterException(message: String) : Exception(message)