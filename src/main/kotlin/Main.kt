import tech.stephenlowery.rpgbot.RPGBot

fun main() {
    val rpgBot = RPGBot(System.getenv("token"))
    rpgBot.start()
}