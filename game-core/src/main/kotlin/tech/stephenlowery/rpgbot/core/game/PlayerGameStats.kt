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

    override fun toString(): String {
        val aliveAtEnd = diedOnRound == null
        return name + "${if (isNpc) " (NPC)" else ""}:\n" +
                (if (aliveAtEnd) "    Ending health: ${endingHealth}\n" else "") +
                "    Damage done: $damageDone\n" +
                "    Damage taken: $damageTaken\n" +
                "    Healing done: $healingDone\n" +
                "    Healing taken: $healingTaken\n" +
                "    Players killed: ${playersKilled.takeUnless { it.isEmpty() }?.joinToString(", ") ?: "None"}\n" +
                (diedOnRound?.let { "    Died on round $it\n" } ?: "")
    }
}