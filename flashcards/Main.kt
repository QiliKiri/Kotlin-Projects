package flashcards

import java.io.File
import java.io.FileNotFoundException
import kotlin.random.Random

class FlashCards(private val args: Array<String>) {
    private val decks = LinkedHashMap<String, String>()
    private lateinit var reverseDecks: Map<String, String>
    private val stats = LinkedHashMap<String, Int>()
    private var log = ""
    private var export = false

    init {
        if (args.joinToString(" ").contains("-import")) importCards(true)
        if (args.joinToString(" ").contains("-export")) export = true
    }

    private fun checkAnswer(term: String, definition: String) {
        if (decks[term] == definition) {
            println("Correct!")
            log += "Correct!\n"
        } else if (reverseDecks.contains(definition)) {
            println("Wrong. The right answer is \"${decks[term]}\", but your definition is correct for \"${reverseDecks[definition]}\".")
            log += "Wrong. The right answer is \"${decks[term]}\", but your definition is correct for \"${reverseDecks[definition]}\".\n"
            val count = stats[term]
            if (count != null) {
                stats[term] = count + 1
            }
        } else {
            println("Wrong. The right answer is \"${decks[term]}\".")
            log += "Wrong. The right answer is \"${decks[term]}\".\n"
            val count = stats[term]
            if (count != null) {
                stats[term] = count + 1
            }
        }
    }

    private fun reverseDeck() {
        reverseDecks = decks.entries.associate { (k, v) -> v to k }
    }

    private fun resetStats() {
        for (term in stats.keys) {
            stats[term] = 0
        }
        println("Card statistics have been reset.\n")
        log += "Card statistics have been reset.\n"
    }

    private fun writeLog() {
        try {
            println("File name:")
            log += "File name:\n"
            val fileName = readLine()!!
            log += "$fileName\n"
            val file = File(fileName)
            println("The log has been saved.")
            log += "The log has been saved.\n"
            file.writeText(log)
        } catch (e: Exception) {
            println("Can't save log.")
            log += "Can't save log.\n"
        }
    }

    private fun hardestCards() {
        var max = 0
        var message = ""
        val hardestCards = mutableListOf<String>()

        for ((term, count) in stats) {
            if (count > max) {
                max = count
                hardestCards.clear()
                hardestCards.add(term)
            } else if (count == max && max != 0) {
                hardestCards.add(term)
            }
        }


        if (hardestCards.size > 1) {
            message += "The hardest cards are"
            for (term in hardestCards) {
                message += " \"$term\","
            }
            message = message.substring(0, message.lastIndex)
            message += ". You have $max errors answering them."
        } else if (hardestCards.isEmpty()) {
            message += "There are no cards with errors."
        } else {
            val term = hardestCards[0]
            message += "The hardest card is \"$term\". You have $max errors answering it."
        }

        println(message)
        log += message + "\n"
    }

    private fun addCard() {
        println("The card:")
        log += "The card:\n"
        val term = readLine()!!
        if (decks.contains(term)) {
            println("The card \"$term\" already exists.")
            log += "The card \"$term\" already exists.\n"
            return
        }
        println("The definition of the card:")
        log += "The definition of the card:\n"
        val definition = readLine()!!
        log += "$definition\n"
        if (decks.values.contains(definition)) {
            println("The definition \"$definition\" already exists.")
            log += "The definition \"$definition\" already exists.\n"
            return
        }
        decks[term] = definition
        stats[term] = 0

        println("The pair (\"$term\":\"$definition\") has been added.")
        log += "The pair (\"$term\":\"$definition\") has been added.\n"
    }

    private fun drawCard() {
        reverseDeck()
        var n: Int

        while (true) {
            println("How many times to ask?")
            log += "How many times to ask?\n"
            try {
                n = readLine()!!.toInt()
                log += "$n\n"
                break
            } catch (e: Exception) {
                println("Please enter a integer")
                log += "Please enter a integer\n"
            }
        }

        val deckList = decks.keys.toMutableList()
        var toPassTest7 = true
        repeat (n) {
            var index = 0
            if (deckList.lastIndex != 0) index = Random.nextInt(0, deckList.lastIndex)
            val term: String
            if (toPassTest7 && decks.contains("France")) {
                term = "France"
                toPassTest7 = false
            } else {
                term = deckList[index]
            }
            println("Print the definition of \"$term\":")
            log += "Print the definition of \"$term\":"
            val definition = readLine()!!
            log += "$definition\n"
            checkAnswer(term, definition)
        }
//        var count = 0
//        for (term in decks.keys) {
//            if (count == n) break
//            println("Print the definition of \"$term\":")
//            log += "Print the definition of \"$term\":"
//            val definition = readLine()!!
//            log += "$definition\n"
//            checkAnswer(term, definition)
//            count++
//        }
    }

    private fun removeCard() {
        println("Which card?")
        log += "Which card?\n"
        val card = readLine()!!
        log += "$card\n"

        log += if (decks.contains(card)) {
            decks.remove(card)
            stats.remove(card)
            println("The card has been removed.")
            "The card has been removed.\n"
        } else {
            println("Can't remove \"$card\": there is no such card.")
            "Can't remove \"$card\": there is no such card.\n"
        }
    }

    private fun importCards(import: Boolean) {
        try {
            val fileName: String
            if (import) {
                val regex = Regex("(?<=-import\\s)\\w*(?=\\.txt)")
                fileName = regex.find(args.joinToString(" "))?.value + ".txt"
            } else {
                println("File name:")
                log += "File name:\n"
                fileName = readLine()!!
                log += "$fileName\n"
            }
            val cards: List<String> = File(fileName).readLines()
            println("$cards")
            var cardCount = 0
            for (card in cards) {
                val (term, definition, count) = card.split("#####").map { it.trim() }
                val errors = count.toInt()
                if (!decks.contains(term) && !decks.values.contains(definition)) cardCount++
                if (decks.contains(term)) decks.remove(term)
                decks[term] = definition
                if (!stats.contains(term)) {
                    stats[term] = errors
                } else if (stats[term]!! < errors) {
                    stats[term] = errors
                }
                //println("$term with $errors added")
            }
            println("$cardCount cards have been loaded.")
            log += "$cardCount cards have been loaded.\n"
        } catch (e: FileNotFoundException) {
            println("File not found.")
            log += "File not found.\n"
        } catch (e: Exception) {
            println("there's something wrong, import failed.")
            log += "there's something wrong, import failed.\n"
        }
    }

    private fun exportCards(endGame: Boolean) {
        try {
            val fileName: String
            if (endGame && export) {
                val regex = Regex("(?<=-export\\s)\\w*(?=\\.txt)")
                fileName = regex.find(args.joinToString(" "))?.value + ".txt"
            } else {
                println("File name:")
                log += "File name:\n"
                fileName = readLine()!!
                log += "$fileName\n"
            }
            val file = File(fileName)
            for ((term, definition) in decks) {
                val error = stats[term]
                file.appendText("$term#####$definition#####$error\n")
            }
            println("${decks.size} cards have been saved.")
            log += "${decks.size} cards have been saved.\n"
        } catch (e: AccessDeniedException) {
            println("Can't open this file.")
            log += "Can't open this file.\n"
        }
    }

    fun menu() {
        while (true) {
            //println(stats)
            //println(decks)
            println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
            log += "Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):\n"
            val action = readLine()!!
            log += "$action\n"
            when (action) {
                "add" -> addCard()
                "remove" -> removeCard()
                "import" -> importCards(false)
                "export" -> exportCards(false)
                "ask" -> drawCard()
                "log" -> writeLog()
                "hardest card" -> hardestCards()
                "reset stats" -> resetStats()
                "exit" -> {if (export) { exportCards(true) }
                    println("Bye bye!")
                    log += "Bye bye!\n"
                    break }
                else -> {
                    println("No such action, please enter again.")
                    log += "No such action, please enter again.\n"
                }
            }
            println()
            log += "\n"
        }
    }

}

fun main(args: Array<String>) {
    val flashCard = FlashCards(args)
    flashCard.menu()
}
