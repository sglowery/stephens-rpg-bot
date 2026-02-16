package tech.stephenlowery.rpgbot.core.game

class PlayerGameStats private constructor(
    val name: String,
    val isNpc: Boolean,
    val endingHealth: Int,
    var diedOnRound: Int?,
    var damageDone: Int,
    var damageTaken: Int,
    var healingDone: Int,
    var healingTaken: Int,
    val playersKilled: MutableList<String> = mutableListOf(),
) {

    constructor(name: String, isNpc: Boolean, endingHealth: Int) : this(name, isNpc, endingHealth, null, 0, 0, 0, 0)

    fun toString(endRound: Int): String {
        val actualEndRound = diedOnRound ?: endRound
        val damageDonePerRound = damageDone / actualEndRound
        val damageTakenPerRound = damageTaken / actualEndRound
        val healingDonePerRound = healingDone / actualEndRound
        val healingTakenPerRound = healingTaken / actualEndRound
        val numKills = playersKilled.size
        val killsText = "${playersKilled.takeUnless { it.isEmpty() }?.joinToString(", ", prefix = "($numKills) ") ?: "None"}\n"
        val aliveAtEnd = diedOnRound == null
        return name + "${if (isNpc) " (NPC)" else ""}:\n" +
                (if (aliveAtEnd) "    Ending health: ${endingHealth}\n" else "") +
                "    Damage done: $damageDone ($damageDonePerRound DPR)\n" +
                "    Damage taken: $damageTaken ($damageTakenPerRound DPR)\n" +
                "    Healing done: $healingDone ($healingDonePerRound HPR)\n" +
                "    Healing taken: $healingTaken ($healingTakenPerRound HPR)\n" +
                "    Kills: $killsText" +
                (diedOnRound?.let { "    Died on round $it\n" } ?: "")
    }
}