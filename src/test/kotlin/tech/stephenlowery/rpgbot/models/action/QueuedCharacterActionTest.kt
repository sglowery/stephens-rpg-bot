package tech.stephenlowery.rpgbot.models.action//import org.junit.Test
//import tech.stephenlowery.rpgbot.models.action.EffectResult
//import tech.stephenlowery.rpgbot.models.character.RPGCharacter
//import tech.stephenlowery.rpgbot.models.action.*
//
//class QueuedCharacterActionTest {
//
//    class TestEffect: ActionEffect() {
//        override fun resolve(from: RPGCharacter, to: RPGCharacter, cycle: Int): List<EffectResult> {
//            return listOf(EffectResult(source = from, target = to, value = 1))
//        }
//    }
//
//    @Test
//    fun `cycleAndResolve executes character action's effects, increases the internal cycle field and returns the results of the effects`() {
//        val characterOne = RPGCharacter(1L, "Stephen")
//        val characterTwo = RPGCharacter(2L, "Ashley")
//        val queuedAction = QueuedCharacterAction(
//            CharacterAction(
//                listOf(TestEffect()),
//                "Test Action",
//                "testaction",
//                "This action is a test",
//                TargetingType.SINGLE,
//                CharacterActionStrings(
//                    "Queued",
//                    "Action",
//                    "Success",
//                    "Missed",
//                    "Failed",
//                    "Effect over",
//                    "Critical",
//                    "Effect continues"
//                )
//            ),
//            characterOne,
//            mutableListOf(characterTwo)
//        )
//    }
//}