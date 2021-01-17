package tech.stephenlowery.rpgbot.models.action

data class CharacterActionStrings(
    val queuedText: String = "",
    val actionText: String = "",
    val successText: String = "",
    val missedText: String = "",
    val failedText: String = "",
    val effectOverText: String = "",
    val critText: String = "",
    val effectContinuedText: String = "",
    val effectChainedText: String = ""
)

fun CharacterActionStrings.getFormattedEffectResultString(effectResult: EffectResult): String {
    return when {
        effectResult.miss -> this.missedText
        effectResult.crit && !effectResult.miss -> this.critText
        effectResult.continued && !effectResult.miss -> this.effectContinuedText
        effectResult.continued && effectResult.expired -> this.effectOverText
        effectResult.chained && !effectResult.miss -> this.effectChainedText
        !effectResult.crit && !effectResult.miss -> this.successText
        else -> "Error formatting effect result."
    }.formatWithActionText(effectResult, actionText).formatFromEffectResult(effectResult)
}

fun String.formatWithActionText(effectResult: EffectResult, actionText: String): String {
    return (if (!effectResult.continued && !effectResult.expired && !effectResult.chained && actionText.isNotEmpty()) actionText + "\n" else "") + this
}

fun String.formatFromEffectResult(effectResult: EffectResult): String {
    return this.replace("{target}", effectResult.target?.name ?: "")
        .replace("{value}", effectResult.value.toString())
        .replace("{source}", effectResult.source?.name ?: "")
        .replace("{other}", effectResult.other ?: "")
}