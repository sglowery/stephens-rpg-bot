package tech.stephenlowery.rpgbot.models

data class CharacterActionStrings(
    val queuedText: String = "",
    val actionText: String = "",
    val successText: String = "",
    val missedText: String = "",
    val failedText: String = "",
    val effectOverText: String = "",
    val critText: String = "",
    val effectContinuedText: String = ""
)

fun CharacterActionStrings.getFormattedEffectResultString(effectResult: EffectResult): String {
    return actionText.plus("\n").plus(
        when {
            effectResult.miss -> this.missedText
            effectResult.crit && !effectResult.miss -> this.critText
            !effectResult.crit && !effectResult.miss -> this.successText
            else -> "Error: could not resolve action effect result from {source} against {target}"
        }
    ).formatFromEffectResult(effectResult)
}

fun String.formatFromEffectResult(effectResult: EffectResult): String {
    return this.replace("{target}", effectResult.target?.name ?: "")
        .replace("{value}", effectResult.value.toString())
        .replace("{source}", effectResult.source?.name ?: "")
        .replace("{other}", effectResult.text ?: "")
}